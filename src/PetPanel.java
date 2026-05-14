import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PetPanel extends JPanel {

    private static final int BUTTON_SIZE = 30;
    private static final int SNAP_MARGIN = 60;

    // Must match your other layout assumptions
    private static final int BASE_Y = 80;
    private static final int DRAG_EXTRA_Y = 100;

    // Hat
    private static final int HAT_BASE_SIZE = 26; // base hat size at 100%

    private final PetStats stats;

    private BufferedImage normalImage;
    private BufferedImage dragImage;
    private BufferedImage bedImage;
    private BufferedImage bedAlone;
    private BufferedImage currentImage;

    private final JButton menuToggleButton;
    private final PetMenu menu;
    private boolean menuVisible = false;

    private final JDialog dialog;
    private BedDialog bed;

    private boolean isDragging = false;
    private boolean isInBed = true;
    private int preDragHeight = -1;

    // proportional scaling
    private double baseScale = -1;
    private int lastBoxW = -1, lastBoxH = -1;

    // last drawn sprite rect (panel coords; used for petting hit-test)
    private Rectangle lastDrawRect = new Rectangle(0, BASE_Y, 1, 1);

    // spriteBox used for hat anchor fractions (panel coords)
    private Rectangle lastSpriteBox = new Rectangle(0, BASE_Y, 1, 1);

    // ── Hat placement preview (temporarily swap rendered pose) ────────────────
    private boolean hatPlacementPreviewActive = false;
    private AccessoryPlacementOverlay.Pose previewPose = AccessoryPlacementOverlay.Pose.SITTING;

    // ── Petting strokes ──────────────────────────────────────────────────────
    private Boolean pettingSideLeft = null;
    private int pettingCrosses = 0;
    private long lastPetAwardMs = 0;

    // ── Sparkles ─────────────────────────────────────────────────────────────
    private static final String SPARKLE_GLYPH = "✨";
    private static final int SPARKLE_LIFE_MS = 700;
    private static final int SPARKLES_PER_PET = 7;

    private final Random sparkleRand = new Random();
    private final List<Sparkle> sparkles = new ArrayList<>();
    private Timer sparkleTimer;

    private static class Sparkle {
        double x, y, vx, vy;
        long bornMs;
        int lifeMs, size;
        Color color;

        Sparkle(double x, double y, double vx, double vy, long bornMs, int lifeMs, int size, Color color) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.bornMs = bornMs;
            this.lifeMs = lifeMs;
            this.size = size;
            this.color = color;
        }
    }

    public PetPanel(JDialog dialog) {
        this.dialog = dialog;
        setLayout(null);

        this.stats = SaveManager.load();
        setimages(stats.getSpriteColor());

        currentImage = bedImage; // first image on launch: cat in bed
        menu = new PetMenu(stats, dialog, () -> applyAppearanceFromStats(stats));

        menuToggleButton = buildToggleButton();
        menuToggleButton.setBounds(5, 85, BUTTON_SIZE, BUTTON_SIZE);
        add(menuToggleButton);

        // DEAD: never render/show the menu toggle button (user requested)
        if (stats.isDead()) {
            menuToggleButton.setVisible(false);
            menuToggleButton.setEnabled(false);
            SwingUtilities.invokeLater(this::ensureMenuOpen);
        }

        // Petting strokes over sprite
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                handlePetting(e.getPoint());
            }
        });

        // Sparkle animation timer (runs only while needed)
        sparkleTimer = new Timer(33, e -> {
            if (sparkles.isEmpty()) {
                sparkleTimer.stop();
                return;
            }
            long now = System.currentTimeMillis();
            synchronized (sparkles) {
                Iterator<Sparkle> it = sparkles.iterator();
                while (it.hasNext()) {
                    Sparkle s = it.next();
                    long age = now - s.bornMs;
                    if (age >= s.lifeMs) { it.remove(); continue; }

                    s.x += s.vx;
                    s.y += s.vy;
                    s.vy -= 0.02; // slight upward acceleration feel
                }
            }
            repaint();
        });
    }

    /** Used by Main (e.g., on death boot) to force menu open. */
    public void ensureMenuOpen() {
        if (!menuVisible) openMenu();
    }

    // ── Hat placement preview API ────────────────────────────────────────────

    /** Temporarily show the pose sprite used by the hat placement dropdown. */
    public void beginHatPlacementPreview(AccessoryPlacementOverlay.Pose pose) {
        if (stats.isDead()) return;
        hatPlacementPreviewActive = true;
        previewPose = (pose != null) ? pose : AccessoryPlacementOverlay.Pose.SITTING;
        repaint();
    }

    /** Restore normal rendering after placement overlay closes. */
    public void endHatPlacementPreview() {
        hatPlacementPreviewActive = false;
        repaint();
    }

    /**
     * Returns the exact spriteBox used for hat placement fractions, in WINDOW coordinates,
     * so it stays correct even if the menu is open (pet panel is shifted).
     */
    public Rectangle getSpriteBoxInWindowCoords(AccessoryPlacementOverlay.Pose pose) {
        int y = BASE_Y + ((pose == AccessoryPlacementOverlay.Pose.DRAG) ? DRAG_EXTRA_Y : 0);
        Rectangle rPanel = new Rectangle(0, y, getWidth(), BedDialog.BED_HEIGHT);

        Window w = SwingUtilities.getWindowAncestor(this);
        if (w == null) return rPanel;
        return SwingUtilities.convertRectangle(this, rPanel, w);
    }

    // ── Bed dialog hookup ────────────────────────────────────────────────────

    public void setBedDialog(BedDialog bed) {
        this.bed = bed;

        // dead = never show bed
        if (stats.isDead()) {
            try { bed.setVisible(false); } catch (Exception ignored) {}
            return;
        }

        bed.setBedSprite(bedAlone);
        bed.positionAtBottom();
        bed.setVisible(!isInBed);

        SwingUtilities.invokeLater(() -> {
            dialog.setAlwaysOnTop(true);
            dialog.toFront();
        });

        repaint();
    }

    public void applyAppearanceFromStats(PetStats s) {
        // DEAD: hide menu button + ensure dead menu is visible; no sprite rendering.
        if (stats.isDead()) {
            menuToggleButton.setVisible(false);
            menuToggleButton.setEnabled(false);
            SwingUtilities.invokeLater(this::ensureMenuOpen);
            repaint();
            return;
        }

        setimages(s.getSpriteColor());
        if (bed != null) bed.setBedSprite(bedAlone);

        baseScale = -1;

        if (isDragging) currentImage = dragImage;
        else if (isInBed) currentImage = bedImage;
        else currentImage = normalImage;

        repaint();
    }

    public void setimages(String color) {
        switch (color) {
            case "Void (Black)" -> {
                normalImage = loadImage("/dingus - Copy/blacksitting.png");
                dragImage   = loadImage("/dingus - Copy/blackscruff.png");
                bedImage    = loadImage("/dingus - Copy/blackinbed.png");
                bedAlone    = loadImage("/dingus - Copy/blackbed.png");
            }
            case "Ghost (White)" -> {
                normalImage = loadImage("/dingus - Copy/whitesitting.png");
                dragImage   = loadImage("/dingus - Copy/whitescruff.png");
                bedImage    = loadImage("/dingus - Copy/whiteinbed.png");
                bedAlone    = loadImage("/dingus - Copy/whitebed.png");
            }
            default -> {
                normalImage = loadImage("/dingus - Copy/orangesitting.png");
                dragImage   = loadImage("/dingus - Copy/orangescruff.png");
                bedImage    = loadImage("/dingus - Copy/orangeinbed.png");
                bedAlone    = loadImage("/dingus - Copy/orangebed.png");
            }
        }

        if (normalImage == null) normalImage = dragImage;
        if (dragImage == null) dragImage = normalImage;
        if (bedImage == null) bedImage = normalImage;
        if (bedAlone == null) bedAlone = loadImage("/dingus - Copy/orangebed.png");

        baseScale = -1;
    }

    // ── Dragging ─────────────────────────────────────────────────────────────

    public void setDragging(boolean dragging) {
        if (stats.isDead()) return;

        if (dragging) {
            if (menuVisible) closeMenu();
            menuToggleButton.setVisible(false);
        }

        isDragging = dragging;

        if (dragging) {
            preDragHeight = dialog.getHeight();
            isInBed = false;

            if (bed != null) {
                bed.setVisible(true);
                SwingUtilities.invokeLater(() -> {
                    bed.bumpTopmost();
                    dialog.setAlwaysOnTop(true);
                    dialog.toFront();
                });
            }

            currentImage = dragImage;

            dialog.setResizable(true);
            dialog.setSize(dialog.getWidth(), preDragHeight + 100);
            dialog.setLocation(dialog.getX(), dialog.getY() - 100);
            dialog.setResizable(false);

        } else {
            if (preDragHeight != -1) {
                dialog.setResizable(true);
                dialog.setSize(dialog.getWidth(), preDragHeight);
                dialog.setLocation(dialog.getX(), dialog.getY() + 100);
                dialog.setResizable(false);
                preDragHeight = -1;
            }

            // grass mini-game updates ONLY on release if intersecting
            try { menu.checkGrassDrop(dialog.getBounds()); } catch (Throwable ignored) {}

            if (isNearBed()) {
                snapToBed();
                isInBed = true;
                currentImage = bedImage;
                if (bed != null) bed.setVisible(false);
            } else {
                isInBed = false;
                currentImage = normalImage;
                if (bed != null) bed.setVisible(true);
            }

            menuToggleButton.setVisible(true);
        }

        repaint();
    }

    public boolean isOverMenuButton(Point p) {
        // dead: button is hidden; never treat as menu hit
        if (stats.isDead()) return false;
        if (isDragging) return false;
        return menuToggleButton.isVisible() && menuToggleButton.getBounds().contains(p);
    }

    private boolean isNearBed() {
        if (bed == null) return false;
        Rectangle bedBounds = bed.getBounds();
        Rectangle snapZone = new Rectangle(
                bedBounds.x - SNAP_MARGIN,
                bedBounds.y - SNAP_MARGIN,
                bedBounds.width + SNAP_MARGIN * 2,
                bedBounds.height + SNAP_MARGIN * 2
        );
        return dialog.getBounds().intersects(snapZone);
    }

    private void snapToBed() {
        dialog.setLocation(BedDialog.getCatSnapPosition());
    }

    // ── Menu toggle ──────────────────────────────────────────────────────────

    private void toggleMenu() {
        if (stats.isDead()) return; // dead menu is forced open
        if (isDragging) return;
        if (menuVisible) closeMenu();
        else openMenu();
    }

    private void openMenu() {
        if (menuVisible) return;
        menuVisible = true;

        if (menu.getPanel().getParent() != dialog) {
            dialog.add(menu.getPanel(), BorderLayout.WEST);
        }
        dialog.setSize(Main.PET_WIDTH + Theme.MENU_WIDTH, Main.PET_HEIGHT);
        dialog.setLocation(dialog.getX() - Theme.MENU_WIDTH, dialog.getY());
        dialog.revalidate();
        dialog.repaint();
    }

    private void closeMenu() {
        if (!menuVisible) return;
        menuVisible = false;

        dialog.remove(menu.getPanel());
        dialog.setSize(Main.PET_WIDTH, Main.PET_HEIGHT);
        dialog.setLocation(dialog.getX() + Theme.MENU_WIDTH, dialog.getY());
        dialog.revalidate();
        dialog.repaint();
    }

    private JButton buildToggleButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = Theme.BTN_DEFAULT;
                if (getModel().isPressed()) bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-2, getHeight()-2, 10, 10);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int pad = 7, cx = getWidth()/2, cy = getHeight()/2;

                if (!menuVisible) {
                    int rr = (Math.min(getWidth(), getHeight()) - pad*2)/2;
                    int d = rr*2;
                    g2.drawArc(cx-rr, cy-rr+1, d, d, 35, 290);
                    g2.drawLine(cx, cy-rr-1, cx, cy-2);
                } else {
                    g2.drawLine(pad, pad, getWidth()-pad, getHeight()-pad);
                    g2.drawLine(getWidth()-pad, pad, pad, getHeight()-pad);
                }

                g2.dispose();
            }
        };
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> toggleMenu());
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { e.consume(); }
        });
        return btn;
    }

    private BufferedImage loadImage(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("Resource not found: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
    }

    // ── Petting + sparkles ───────────────────────────────────────────────────

    private void handlePetting(Point p) {
        if (stats.isDead()) return;
        if (isDragging) return;
        if (hatPlacementPreviewActive) return;

        // Only pet when over the actual drawn sprite area (not transparent space)
        if (!lastDrawRect.contains(p)) {
            pettingSideLeft = null;
            pettingCrosses = 0;
            return;
        }

        int midX = lastDrawRect.x + lastDrawRect.width / 2;
        boolean left = p.x < midX;

        if (pettingSideLeft == null) {
            pettingSideLeft = left;
            return;
        }

        if (left != pettingSideLeft) {
            pettingSideLeft = left;
            pettingCrosses++;

            if (pettingCrosses >= 5) {
                long now = System.currentTimeMillis();
                if (now - lastPetAwardMs > 800) { // small cooldown
                    lastPetAwardMs = now;
                    pettingCrosses = 0;

                    stats.addHappiness(1);
                    SaveManager.save(stats);

                    spawnSparkles(p);

                    try { menu.refreshFromStats(); } catch (Exception ignored) {}
                }
            }
        }
    }

    private void spawnSparkles(Point origin) {
        long now = System.currentTimeMillis();
        synchronized (sparkles) {
            for (int i = 0; i < SPARKLES_PER_PET; i++) {
                double ox = origin.x + sparkleRand.nextInt(25) - 12;
                double oy = origin.y + sparkleRand.nextInt(17) - 8;

                double vx = (sparkleRand.nextDouble() - 0.5) * 1.2;
                double vy = -1.2 - sparkleRand.nextDouble() * 1.4;

                int size = 14 + sparkleRand.nextInt(10);
                Color[] palette = {
                        new Color(0xF2,0xD0,0x74), // yellow
                        new Color(0xC9,0xE3,0xE7), // pale blue
                        new Color(0xB9,0xE0,0xB3)  // soft green
                };
                Color c = palette[sparkleRand.nextInt(palette.length)];

                sparkles.add(new Sparkle(ox, oy, vx, vy, now, SPARKLE_LIFE_MS, size, c));
            }
        }
        ensureSparkleTimer();
        if (!sparkleTimer.isRunning()) sparkleTimer.start();
        repaint();
    }

    private void drawSparkles(Graphics2D g2) {
        long now = System.currentTimeMillis();

        List<Sparkle> copy;
        synchronized (sparkles) { copy = new ArrayList<>(sparkles); }

        for (Sparkle s : copy) {
            float t = (float)((now - s.bornMs) / (double)s.lifeMs);
            if (t < 0f) t = 0f;
            if (t > 1f) t = 1f;
            float alpha = 1.0f - t;

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g2.setFont(Theme.emojiFont(s.size));
            FontMetrics fm = g2.getFontMetrics();
            int x = (int) Math.round(s.x - fm.stringWidth(SPARKLE_GLYPH) / 2.0);
            int y = (int) Math.round(s.y + (fm.getAscent() - fm.getDescent()) / 2.0);
            g2.setColor(Theme.ACCENT_COINS);
            g2.setColor(s.color);
            g2.drawString(SPARKLE_GLYPH, x, y);
            g2.setComposite(old);
        }
    }

    // ── Rendering helpers ────────────────────────────────────────────────────

    private void recomputeBaseScaleIfNeeded(Rectangle box) {
        if (normalImage == null) return;
        if (baseScale > 0 && box.width == lastBoxW && box.height == lastBoxH) return;
        lastBoxW = box.width; lastBoxH = box.height;
        baseScale = Math.min(box.width / (double) normalImage.getWidth(),
                box.height / (double) normalImage.getHeight());
    }

    private static double maxScaleToFit(BufferedImage img, Rectangle box) {
        if (img == null) return 1.0;
        return Math.min(box.width / (double) img.getWidth(), box.height / (double) img.getHeight());
    }

    private static Rectangle computeDrawRectBottomAligned(BufferedImage img, Rectangle box, double scale) {
        int dw = (int) Math.round(img.getWidth() * scale);
        int dh = (int) Math.round(img.getHeight() * scale);
        int dx = box.x + (box.width - dw) / 2;
        int dy = box.y + (box.height - dh);
        return new Rectangle(dx, dy, dw, dh);
    }

    private static void drawWithScaleBottomAligned(Graphics2D g2, BufferedImage img, Rectangle box, double scale) {
        Rectangle r = computeDrawRectBottomAligned(img, box, scale);
        g2.drawImage(img, r.x, r.y, r.width, r.height, null);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp01(double v) {
        return clamp(v, 0.0, 1.0);
    }

    private static int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private BufferedImage imageForPose(AccessoryPlacementOverlay.Pose pose) {
        return switch (pose) {
            case SITTING -> normalImage;
            case BED     -> bedImage;
            case DRAG    -> dragImage;
        };
    }

    private void drawHat(Graphics2D g2) {
        StoreItem it = AccessoryCatalog.byId(stats.getEquippedHatId());
        if (it == null || it.glyph == null) return;

        // per-pose saved anchors
        double xf, yf;
        if (isDragging) {
            xf = stats.getHeadDragXFrac();
            yf = stats.getHeadDragYFrac();
        } else if (isInBed) {
            xf = stats.getHeadBedXFrac();
            yf = stats.getHeadBedYFrac();
        } else {
            xf = stats.getHeadNormalXFrac();
            yf = stats.getHeadNormalYFrac();
        }

        xf = clamp01(xf);
        yf = clamp01(yf);

        // consistent size across poses (use normal scale)
        double scale = clamp(stats.getHatScaleNormal(), 0.50, 2.00);

        int cx = (int) Math.round(lastSpriteBox.x + lastSpriteBox.width  * xf);
        int cy = (int) Math.round(lastSpriteBox.y + lastSpriteBox.height * yf);

        cx = clampInt(cx, lastSpriteBox.x, lastSpriteBox.x + lastSpriteBox.width - 1);
        cy = clampInt(cy, lastSpriteBox.y, lastSpriteBox.y + lastSpriteBox.height - 1);

        int sizePx = (int) Math.round(HAT_BASE_SIZE * scale);
        sizePx = clampInt(sizePx, 12, 72);

        Color fill = new Color(stats.getHatColorRGB(), true);
        Color outline = Theme.BG_INPUT_BORDER;

        drawOutlinedGlyph(g2, it.glyph, cx, cy, sizePx, fill, outline);
    }

    private void drawOutlinedGlyph(Graphics2D g2, String glyph, int cx, int cy, int size,
                                   Color fill, Color outline) {
        Font symbol = new Font("Segoe UI Symbol", Font.PLAIN, size);
        Font emoji  = Theme.emojiFont(size);
        Font f = (symbol.canDisplayUpTo(glyph) == -1) ? symbol : emoji;

        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        int x = cx - fm.stringWidth(glyph) / 2;
        int y = cy + (fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(outline);
        for (int ox = -1; ox <= 1; ox++) {
            for (int oy = -1; oy <= 1; oy++) {
                if (ox == 0 && oy == 0) continue;
                g2.drawString(glyph, x + ox, y + oy);
            }
        }

        g2.setColor(fill);
        g2.drawString(glyph, x, y);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // DEAD: do not render sprite (or hat). Only allow sparkles to finish.
        if (stats.isDead()) {
            drawSparkles(g2);
            g2.dispose();
            return;
        }

        // Choose which pose to render (for placement preview)
        AccessoryPlacementOverlay.Pose pose;
        if (hatPlacementPreviewActive) {
            pose = previewPose;
        } else if (isDragging) {
            pose = AccessoryPlacementOverlay.Pose.DRAG;
        } else if (isInBed) {
            pose = AccessoryPlacementOverlay.Pose.BED;
        } else {
            pose = AccessoryPlacementOverlay.Pose.SITTING;
        }

        BufferedImage img = hatPlacementPreviewActive ? imageForPose(pose) : currentImage;
        if (img == null) img = currentImage;

        Rectangle spriteBox = new Rectangle(
                0,
                BASE_Y + ((pose == AccessoryPlacementOverlay.Pose.DRAG) ? DRAG_EXTRA_Y : 0),
                getWidth(),
                BedDialog.BED_HEIGHT
        );
        lastSpriteBox = spriteBox;

        recomputeBaseScaleIfNeeded(spriteBox);

        double clampS = maxScaleToFit(img, spriteBox);
        double s = (baseScale > 0) ? Math.min(baseScale, clampS) : clampS;

        lastDrawRect = computeDrawRectBottomAligned(img, spriteBox, s);

        drawWithScaleBottomAligned(g2, img, spriteBox, s);

        // During placement preview, overlay draws the moving hat preview -> avoid double hat
        if (!hatPlacementPreviewActive) {
            drawHat(g2);
        }

        drawSparkles(g2);

        g2.dispose();
    }

    private void ensureSparkleTimer() {
        if (sparkleTimer != null) return;

        sparkleTimer = new Timer(33, e -> {
            if (sparkles.isEmpty()) {
                sparkleTimer.stop();
                return;
            }
            long now = System.currentTimeMillis();
            synchronized (sparkles) {
                Iterator<Sparkle> it = sparkles.iterator();
                while (it.hasNext()) {
                    Sparkle s = it.next();
                    long age = now - s.bornMs;
                    if (age >= s.lifeMs) { it.remove(); continue; }

                    s.x += s.vx;
                    s.y += s.vy;
                    s.vy -= 0.02;
                }
            }
            repaint();
        });
    }
}