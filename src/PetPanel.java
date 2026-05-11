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

    private BufferedImage normalImage;
    private BufferedImage dragImage;
    private BufferedImage bedImage;   // cat in bed sprite
    private BufferedImage bedAlone;   // bed only sprite
    private BufferedImage currentImage;

    private final JButton menuToggleButton;
    private final PetMenu menu;
    private boolean menuVisible = false;

    private final JDialog dialog;
    private BedDialog bed;

    private boolean isDragging = false;
    private boolean isInBed = true; // start in bed
    private int preDragHeight = -1;

    private static final Cursor CURSOR_DEFAULT = Cursor.getDefaultCursor();

    // proportional scaling based on sitting sprite
    private double baseScale = -1;
    private int lastBoxW = -1, lastBoxH = -1;

    public PetPanel(JDialog dialog) {
        this.dialog = dialog;
        setLayout(null);

        this.stats = SaveManager.load();
        setimages(stats.getSpriteColor());

        currentImage = bedImage; // first image is cat-in-bed

        menu = new PetMenu(stats, dialog, () -> applyAppearanceFromStats(stats));

        menuToggleButton = buildToggleButton();
        menuToggleButton.setBounds(5, 85, BUTTON_SIZE, BUTTON_SIZE);
        add(menuToggleButton);
    }

    public void setBedDialog(BedDialog bed) {
        this.bed = bed;

        bed.setBedSprite(bedAlone);
        bed.positionAtBottom();

        // start in bed => hide bed-only window
        bed.setVisible(!isInBed);

        repaint();
    }

    public void applyAppearanceFromStats(PetStats s) {
        setimages(s.getSpriteColor());
        if (bed != null) bed.setBedSprite(bedAlone);

        baseScale = -1;

        if (isDragging) currentImage = (dragImage != null) ? dragImage : normalImage;
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
            case "Default (Orange)" -> {
                normalImage = loadImage("dingus - Copy/orangesitting.png");
                dragImage   = loadImage("dingus - Copy/orangescruff.png");
                bedImage    = loadImage("dingus - Copy/orangeinbed.png");
                bedAlone    = loadImage("dingus - Copy/orangebed.png");
            }
            default -> {
                normalImage = loadImage("dingus - Copy/orangesitting.png");
                dragImage   = loadImage("dingus - Copy/orangescruff.png");
                bedImage    = loadImage("dingus - Copy/orangeinbed.png");
                bedAlone    = loadImage("dingus - Copy/orangebed.png");
            }
        }

        if (normalImage == null && dragImage != null) normalImage = dragImage;
        if (dragImage == null) dragImage = normalImage;
        if (bedImage == null) bedImage = normalImage;
        if (bedAlone == null) bedAlone = loadImage("dingus - Copy/orangebed.png");

        baseScale = -1;
    }

    public void setDragging(boolean dragging) {
        this.isDragging = dragging;

        if (dragging) {
            preDragHeight = dialog.getHeight();

            isInBed = false;
            if (bed != null) {
                bed.setVisible(true);
                bed.toBack();
            }

            currentImage = (dragImage != null) ? dragImage : normalImage;

            dialog.setResizable(true);
            dialog.setSize(dialog.getWidth(), preDragHeight + 100);
            dialog.setLocation(dialog.getX(), dialog.getY() - 100);
            dialog.setResizable(false);

        } else {
            setCursor(CURSOR_DEFAULT);

            if (preDragHeight != -1) {
                dialog.setResizable(true);
                dialog.setSize(dialog.getWidth(), preDragHeight);
                dialog.setLocation(dialog.getX(), dialog.getY() + 100);
                dialog.setResizable(false);
                preDragHeight = -1;
            }

            // ONLY place where stats update for grass: on drag release
            try { menu.checkGrassDrop(dialog.getBounds()); } catch (Throwable ignored) {}

            if (isNearBed()) {
                snapToBed();
                isInBed = true;
                currentImage = bedImage;
                if (bed != null) bed.setVisible(false);
            } else {
                isInBed = false;
                currentImage = normalImage;
                if (bed != null) {
                    bed.setVisible(true);
                    bed.toBack();
                }
            }

            menuToggleButton.setVisible(true);
        }

        repaint();
    }

    public boolean isOverMenuButton(Point p) {
        if (!menuToggleButton.isVisible()) return false;
        return p.x >= 5 && p.x <= 5 + BUTTON_SIZE
                && p.y >= 85 && p.y <= 85 + BUTTON_SIZE;
    }

    private boolean isNearBed() {
        if (bed == null) return false;
        Rectangle bedBounds = bed.getBounds();
        Rectangle snapZone = new Rectangle(
                bedBounds.x      - SNAP_MARGIN,
                bedBounds.y      - SNAP_MARGIN,
                bedBounds.width  + SNAP_MARGIN * 2,
                bedBounds.height + SNAP_MARGIN * 2
        );
        return dialog.getBounds().intersects(snapZone);
    }

    private void snapToBed() {
        dialog.setLocation(BedDialog.getCatSnapPosition());
    }

    private void toggleMenu() {
        menuVisible = !menuVisible;

        if (menuVisible) {
            dialog.setSize(Main.PET_WIDTH + Theme.MENU_WIDTH, Main.PET_HEIGHT);
            dialog.setLocation(dialog.getX() - Theme.MENU_WIDTH, dialog.getY());
            dialog.add(menu.getPanel(), BorderLayout.WEST);
            menuToggleButton.setToolTipText("Close menu");
        } else {
            dialog.remove(menu.getPanel());
            dialog.setSize(Main.PET_WIDTH, Main.PET_HEIGHT);
            dialog.setLocation(dialog.getX() + Theme.MENU_WIDTH, dialog.getY());
            menuToggleButton.setToolTipText("Open menu");
        }

        menuToggleButton.repaint();
        dialog.revalidate();
        dialog.repaint();
    }

    private JButton buildToggleButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = Theme.BTN_DEFAULT;
                if (!isEnabled()) bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed()) bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                int r = 10;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, r, r);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int pad = 7;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                if (!menuVisible) {
                    int rr = (Math.min(getWidth(), getHeight()) - pad * 2) / 2;
                    int d  = rr * 2;
                    g2.drawArc(cx - rr, cy - rr + 1, d, d, 35, 290);
                    g2.drawLine(cx, cy - rr - 1, cx, cy - 2);
                } else {
                    int x1 = pad, y1 = pad;
                    int x2 = getWidth() - pad;
                    int y2 = getHeight() - pad;
                    g2.drawLine(x1, y1, x2, y2);
                    g2.drawLine(x2, y1, x1, y2);
                }

                g2.dispose();
            }
        };

        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Open menu");
        btn.addActionListener(e -> toggleMenu());
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { e.consume(); }
        });
        return btn;
    }

    private static BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { System.err.println("Missing image: " + path); return null; }
    }

    private void recomputeBaseScaleIfNeeded(Rectangle box) {
        if (normalImage == null) return;
        if (baseScale > 0 && box.width == lastBoxW && box.height == lastBoxH) return;

        lastBoxW = box.width;
        lastBoxH = box.height;

        double sx = box.width  / (double) normalImage.getWidth();
        double sy = box.height / (double) normalImage.getHeight();
        baseScale = Math.min(sx, sy);
    }

    private static double maxScaleToFit(BufferedImage img, Rectangle box) {
        if (img == null) return 1.0;
        return Math.min(box.width / (double) img.getWidth(), box.height / (double) img.getHeight());
    }

    private static void drawWithScaleBottomAligned(Graphics2D g2, BufferedImage img, Rectangle box, double scale) {
        if (img == null || scale <= 0) return;

        int dw = (int) Math.round(img.getWidth() * scale);
        int dh = (int) Math.round(img.getHeight() * scale);

        int dx = box.x + (box.width - dw) / 2;
        int dy = box.y + (box.height - dh); // bottom aligned

        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Rectangle spriteBox = new Rectangle(
                0,
                BASE_Y + (isDragging ? DRAG_EXTRA_Y : 0),
                getWidth(),
                BedDialog.BED_HEIGHT
        );

        recomputeBaseScaleIfNeeded(spriteBox);

        if (isInBed && !isDragging) {
            // Bed sprite: aspect preserved within the same bed-area box
            double s = maxScaleToFit(currentImage, new Rectangle(0, BASE_Y, BedDialog.BED_WIDTH, BedDialog.BED_HEIGHT));
            drawWithScaleBottomAligned(g2, currentImage,
                    new Rectangle(0, BASE_Y, BedDialog.BED_WIDTH, BedDialog.BED_HEIGHT), s);

        } else if (isDragging) {
            double clamp = maxScaleToFit(currentImage, spriteBox);
            double s = (baseScale > 0) ? Math.min(baseScale, clamp) : clamp;
            drawWithScaleBottomAligned(g2, currentImage, spriteBox, s);

        } else {
            double clamp = maxScaleToFit(currentImage, spriteBox);
            double s = (baseScale > 0) ? Math.min(baseScale, clamp) : clamp;
            drawWithScaleBottomAligned(g2, currentImage, spriteBox, s);
        }

        g2.dispose();
    }
}