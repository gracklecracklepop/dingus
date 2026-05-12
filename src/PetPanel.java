import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PetPanel extends JPanel {

    private static final int BUTTON_SIZE = 30;
    private static final int SNAP_MARGIN = 60;

    private static final int BASE_Y = 80;
    private static final int DRAG_EXTRA_Y = 100;

    private final PetStats stats;
    // CODE-ONLY anchors (not saved)
    private static final double HEAD_NORMAL_XFRAC = 0.29;
    private static final double HEAD_NORMAL_YFRAC = 0.139;

    private static final double HEAD_BED_XFRAC    = 0.67; // use 0..1 (avoid negatives)
    private static final double HEAD_BED_YFRAC    = 0.110;

    private static final double HEAD_DRAG_XFRAC   = 0.52;
    private static final double HEAD_DRAG_YFRAC   = 0.1199;
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

    // last drawn sprite rect (panel coords)
    private Rectangle lastDrawRect = new Rectangle(0, BASE_Y, 1, 1);

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
    }

    public void setBedDialog(BedDialog bed) {
        this.bed = bed;

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
                normalImage = loadImage("dingus - Copy/blacksitting.png");
                dragImage   = loadImage("dingus - Copy/blackscruff.png");
                bedImage    = loadImage("dingus - Copy/blackinbed.png");
                bedAlone    = loadImage("dingus - Copy/blackbed.png");
            }
            case "Ghost (White)" -> {
                normalImage = loadImage("dingus - Copy/whitesitting.png");
                dragImage   = loadImage("dingus - Copy/whitescruff.png");
                bedImage    = loadImage("dingus - Copy/whiteinbed.png");
                bedAlone    = loadImage("dingus - Copy/whitebed.png");
            }
            default -> {
                normalImage = loadImage("dingus - Copy/orangesitting.png");
                dragImage   = loadImage("dingus - Copy/orangescruff.png");
                bedImage    = loadImage("dingus - Copy/orangeinbed.png");
                bedAlone    = loadImage("dingus - Copy/orangebed.png");
            }
        }

        if (normalImage == null) normalImage = dragImage;
        if (dragImage == null) dragImage = normalImage;
        if (bedImage == null) bedImage = normalImage;
        if (bedAlone == null) bedAlone = loadImage("dingus - Copy/orangebed.png");

        baseScale = -1;
    }

    // ── Dragging (closes menu on drag start) ─────────────────────

    public void setDragging(boolean dragging) {
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

    // ── Menu toggle ──────────────────────────────────────────────

    private void toggleMenu() {
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
        btn.addMouseListener(new MouseAdapter() { @Override public void mousePressed(java.awt.event.MouseEvent e) { e.consume(); }});
        return btn;
    }

    private static BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { return null; }
    }

    // ── Rendering (sprite + hat) ──────────────────────────────────

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

    private void drawHat(Graphics2D g2) {
        StoreItem it = AccessoryCatalog.byId(stats.getEquippedHatId());
        if (it == null || it.glyph == null) return;

        // pick correct anchor based on state
        double xf, yf;
        if (isDragging) {
            xf = HEAD_DRAG_XFRAC;
            yf = HEAD_DRAG_YFRAC;
        } else if (isInBed) {
            xf = HEAD_BED_XFRAC;
            yf = HEAD_BED_YFRAC;
        } else {
            xf = HEAD_NORMAL_XFRAC;
            yf = HEAD_NORMAL_YFRAC;
        }

        int cx = (int) Math.round(lastDrawRect.x + lastDrawRect.width  * xf);
        int cy = (int) Math.round(lastDrawRect.y + lastDrawRect.height * yf);

        Color fill = new Color(stats.getHatColorRGB(), true);
        Color outline = Theme.BG_INPUT_BORDER;

        drawOutlinedGlyph(g2, it.glyph, cx, cy, 26, fill, outline);
    }

    private void drawOutlinedGlyph(Graphics2D g2, String glyph, int cx, int cy, int size,
                                   Color fill, Color outline) {
        Font symbol = new Font("Segoe UI Symbol", Font.PLAIN, size);
        Font emoji  = Theme.emojiFont(size);
        Font f = (symbol.canDisplayUpTo(glyph) == -1) ? symbol : emoji;

        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        int x = cx - fm.stringWidth(glyph)/2;
        int y = cy + (fm.getAscent() - fm.getDescent())/2;

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

        Rectangle spriteBox = new Rectangle(
                0,
                BASE_Y + (isDragging ? DRAG_EXTRA_Y : 0),
                getWidth(),
                BedDialog.BED_HEIGHT
        );

        recomputeBaseScaleIfNeeded(spriteBox);

        double clamp = maxScaleToFit(currentImage, spriteBox);
        double s = (baseScale > 0) ? Math.min(baseScale, clamp) : clamp;

        lastDrawRect = computeDrawRectBottomAligned(currentImage, spriteBox, s);

        drawWithScaleBottomAligned(g2, currentImage, spriteBox, s);

        // draw hat on top of sprite (no UI button, just the emoji)
        drawHat(g2);

        g2.dispose();
    }
}