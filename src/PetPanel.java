import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PetPanel extends JPanel {

    private static final int BUTTON_SIZE = 30;

    private final BufferedImage normalImage;   // catsitting.png  — on desktop
    private final BufferedImage dragImage;     // catscruff.jpg   — being dragged
    private final BufferedImage bedImage;      // catinbed.png    — resting in bed
    private BufferedImage currentImage;

    private final JButton menuToggleButton;
    private final PetMenu menu;
    private boolean menuVisible = false;

    private final JDialog dialog;
    private BedDialog bed; // set after construction via setBedDialog()

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();

    public PetPanel(JDialog dialog) {
        this.dialog = dialog;
        setLayout(null);

        normalImage  = loadImage("images/catsitting.png");
        dragImage    = loadImage("images/catscruff.jpg");
        bedImage     = loadImage("images/catinbed.jpg");
        currentImage = normalImage;

        PetStats stats = SaveManager.load();
        menu = new PetMenu(stats, dialog);

        menuToggleButton = buildToggleButton();
        menuToggleButton.setBounds(5, 5, BUTTON_SIZE, BUTTON_SIZE);
        add(menuToggleButton);
    }

    // ── Public API ──────────────────────────────────────────────

    /** Called from Main after BedDialog is constructed. */
    public void setBedDialog(BedDialog bed) {
        this.bed = bed;
    }

    public void setDragging(boolean dragging) {
        if (dragging) {
            currentImage = dragImage;
            menuToggleButton.setVisible(false);
            if (menuVisible) toggleMenu();
        } else {
            // Released — check whether we landed on the bed
            currentImage = isOverBed() ? bedImage : normalImage;
            menuToggleButton.setVisible(true);
        }
        repaint();
    }

    public boolean isOverMenuButton(Point p) {
        if (!menuToggleButton.isVisible()) return false;
        return p.x >= 5 && p.x <= 5 + BUTTON_SIZE
                && p.y >= 5 && p.y <= 5 + BUTTON_SIZE;
    }

    // ── Bed overlap detection ────────────────────────────────────

    /**
     * Returns true when the pet dialog's bounds intersect the bed dialog's bounds.
     * Uses screen coordinates so the comparison is always accurate regardless of
     * where either window happens to be.
     */
    private boolean isOverBed() {
        if (bed == null) return false;
        Rectangle petBounds = dialog.getBounds();
        Rectangle bedBounds = bed.getBounds();
        return petBounds.intersects(bedBounds);
    }

    // ── Private helpers ──────────────────────────────────────────

    private void toggleMenu() {
        menuVisible = !menuVisible;
        if (menuVisible) {
            menuToggleButton.setText("✕");
            dialog.setSize(Main.PET_WIDTH + PetMenu.WIDTH, Main.PET_HEIGHT);
            dialog.setLocation(dialog.getX() - PetMenu.WIDTH, dialog.getY());
            dialog.add(menu.getPanel(), BorderLayout.WEST);
        } else {
            menuToggleButton.setText("☰");
            dialog.remove(menu.getPanel());
            dialog.setSize(Main.PET_WIDTH, Main.PET_HEIGHT);
            dialog.setLocation(dialog.getX() + PetMenu.WIDTH, dialog.getY());
        }
        dialog.revalidate();
        dialog.repaint();
    }

    private JButton buildToggleButton() {
        JButton btn = new JButton("☰") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? new Color(60,60,60,220)
                        : getModel().isRollover() ? new Color(80,80,80,220)
                        :                           new Color(50,50,50,200);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(100,100,100));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
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
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { e.consume(); }
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
        if (currentImage != null)
            g.drawImage(currentImage, 0, 0,
                    (int)(screenSize.getWidth()  / 4),
                    (int)(screenSize.getHeight() / 4), this);
    }
}