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
    PetStats petstat = SaveManager.load();


    private  BufferedImage normalImage;
    private  BufferedImage dragImage;
    private  BufferedImage bedImage;
    private BufferedImage currentImage;

    private final JButton menuToggleButton;
    private final PetMenu menu;
    private boolean menuVisible = false;

    private final JDialog dialog;
    private BedDialog bed;

    private boolean isDragging = false;

    private static final Cursor CURSOR_DEFAULT = Cursor.getDefaultCursor();
    private static final Cursor CURSOR_GRAB    = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

    static Toolkit toolkit = Toolkit.getDefaultToolkit();
    static Dimension screenSize = toolkit.getScreenSize();
    double dpiScale = Theme.getDpiScale();

    public PetPanel(JDialog dialog) {
        this.dialog = dialog;
        setLayout(null);

        setimages(petstat.getSpriteColor());
        currentImage = normalImage;

        PetStats stats = SaveManager.load();
        menu = new PetMenu(stats, dialog);

        menuToggleButton = buildToggleButton();
        menuToggleButton.setBounds(5, 85, BUTTON_SIZE, BUTTON_SIZE);
        add(menuToggleButton);
    }

    // ── Public API ──────────────────────────────────────────────

    public void setBedDialog(BedDialog bed) {
        this.bed = bed;
        bed.setVisible(false);
        currentImage = bedImage;
        repaint();
    }
    public void setimages(String color){
        System.out.println(color);
        switch (color){
            case "Void (Black)":
                normalImage  = loadImage("images/blacksitting.png");
                dragImage    = loadImage("images/catscruff.jpg");
                bedImage     = loadImage("images/blackinbed.png");
                break;
            case "Default (Orange)":
                normalImage  = loadImage("images/orangesitting.png");
                dragImage    = loadImage("images/catscruff.jpg");
                bedImage     = loadImage("images/orangeinbed.png");
                break;
            case "Ghost (White)":
                normalImage  = loadImage("images/whitesitting.png");
                dragImage    = loadImage("images/catscruff.jpg");
                bedImage     = loadImage("images/whiteinbed.png");
                break;


        }
    }

    public void setDragging(boolean dragging) {
        this.isDragging = dragging;

        if (dragging) {
            //setCursor(CURSOR_GRAB);
            currentImage = dragImage;
            menuToggleButton.setVisible(false);
            if (menuVisible) toggleMenu();
            if (bed != null) bed.setVisible(true);

        } else {
            setCursor(CURSOR_DEFAULT);

            if (isNearBed()) {
                snapToBed();
                currentImage = bedImage;
                if (bed != null) bed.setVisible(false);
            } else {
                currentImage = normalImage;
                if (bed != null) bed.setVisible(true);
            }

            menuToggleButton.setVisible(true);
        }
        repaint();
    }

    public boolean isOverMenuButton(Point p) {
        if (!menuToggleButton.isVisible()) return false;
        return p.x >= 5 && p.x <= 5 + BUTTON_SIZE
                && p.y >= 5 && p.y <= 5 + BUTTON_SIZE;
    }

    // ── Bed Detection & Snapping ─────────────────────────────────

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

    // ── Private helpers ──────────────────────────────────────────

    private void toggleMenu() {
        menuVisible = !menuVisible;
        if (menuVisible) {
            menuToggleButton.setFont(Theme.emojiFont(40));
            menuToggleButton.setText("❌");
            dialog.setSize(Main.PET_WIDTH + Theme.MENU_WIDTH, Main.PET_HEIGHT);
            dialog.setLocation(dialog.getX() - Theme.MENU_WIDTH, dialog.getY());
            dialog.add(menu.getPanel(), BorderLayout.WEST);
        } else {
            menuToggleButton.setFont(Theme.emojiFont(40));
            menuToggleButton.setText("🤍");
            dialog.remove(menu.getPanel());
            dialog.setSize(Main.PET_WIDTH, Main.PET_HEIGHT);
            dialog.setLocation(dialog.getX() + Theme.MENU_WIDTH, dialog.getY());
        }
        dialog.revalidate();
        dialog.repaint();
    }

    private JButton buildToggleButton() {
        JButton btn = new JButton("🤍") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Background
                Color bg = getModel().isPressed()  ? new Color(60, 60, 60, 220)
                        : getModel().isRollover() ? new Color(80, 80, 80, 220)
                        :                           new Color(50, 50, 50, 200);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(100, 100, 100));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Use emoji font for drawing the text
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.emojiFont(16)); // ← emoji font here instead of Arial
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> toggleMenu());
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { e.consume(); }
        });
        return btn;
    }

    private static BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { e.printStackTrace(); return null; }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);



        if (currentImage != null) {
            g2.drawImage(currentImage, 0, 80, BedDialog.imgW, BedDialog.imgH, this);
        }

        g2.dispose();
    }
}