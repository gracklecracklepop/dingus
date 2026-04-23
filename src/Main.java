import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    static final int PET_WIDTH  = 360;
    static final int PET_HEIGHT = 290;

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            // --- WIZARD CHECK ---
            if (!SaveManager.saveExists()) {
                SetupWizard wizard = new SetupWizard();
                wizard.setVisible(true);

                if (!wizard.isFinished()) {
                    System.exit(0);
                }

                SaveManager.save(wizard.getGeneratedStats());
            }

            PetStats currentStats = SaveManager.load();

            // Utility owner frame keeps the app off the taskbar
            JFrame hiddenOwner = new JFrame();
            hiddenOwner.setUndecorated(true);
            hiddenOwner.setSize(0, 0);
            hiddenOwner.setType(Window.Type.UTILITY);
            hiddenOwner.setVisible(true);

            Image icon = loadAppIcon();
            hiddenOwner.setIconImages(List.of(
                    icon.getScaledInstance(16,  16,  Image.SCALE_SMOOTH),
                    icon.getScaledInstance(32,  32,  Image.SCALE_SMOOTH),
                    icon.getScaledInstance(64,  64,  Image.SCALE_SMOOTH),
                    icon.getScaledInstance(128, 128, Image.SCALE_SMOOTH)
            ));

            // Bed window — shown first so it sits behind the pet
            BedDialog bed = new BedDialog(hiddenOwner);
            bed.positionAtBottom();
            bed.setAlwaysOnTop(true);
            bed.setVisible(true);

            JDialog dialog = new JDialog(hiddenOwner);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));
            dialog.setLayout(new BorderLayout());
            dialog.setSize(PET_WIDTH, PET_HEIGHT);
            dialog.setAlwaysOnTop(true);
            dialog.setResizable(false);

// Position using usable screen area — always above the taskbar
            Rectangle usable = Theme.getUsableScreen();
            dialog.setLocation(
                    usable.x + usable.width  - PET_WIDTH  - 40,
                    usable.y + usable.height - PET_HEIGHT
            );

            PetPanel panel = new PetPanel(dialog);
            panel.setOpaque(false);

            dialog.add(panel, BorderLayout.CENTER);

            PetTray.setup(dialog, bed, icon);
            attachDragListener(panel, dialog);

            // Show dialog first
            dialog.setVisible(true);
            imageSaver.startRandomSaving("images/poo.png", 1800, 3600);

            // After visible: snap cat to bed and link bed reference
            SwingUtilities.invokeLater(() -> {
                Point snapPos = BedDialog.getCatSnapPosition();
                dialog.setLocation(snapPos.x, snapPos.y);
                panel.setBedDialog(bed);
            });
        });
    }

    private static void attachDragListener(PetPanel panel, JDialog dialog) {
        final Point[] dragOffset = {null};
        final boolean[] active   = {false};

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (panel.isOverMenuButton(e.getPoint())) return;

                // Record where on the dialog the mouse clicked
                Point dialogLoc = dialog.getLocation();
                dragOffset[0] = new Point(
                        e.getXOnScreen() - dialogLoc.x,
                        e.getYOnScreen() - dialogLoc.y
                );

                active[0] = true;
                panel.setDragging(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!active[0]) return;
                active[0] = false;
                panel.setDragging(false);
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!active[0] || dragOffset[0] == null) return;

                // Subtract the offset so the dialog doesn't jump
                int newX = e.getXOnScreen() - dragOffset[0].x;
                int newY = e.getYOnScreen() - dragOffset[0].y;

                dialog.setLocation(newX, newY);
            }
        });
    }

    static Image loadAppIcon() {
        for (String path : new String[]{"images/icon.png", "images/yarn_icon.png"}) {
            try { return ImageIO.read(new File(path)); } catch (IOException ignored) {}
        }
        return createFallbackIcon();
    }

    private static Image createFallbackIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(255, 200, 100));
        g.fillOval(8, 16, 48, 44);
        drawEar(g, new int[]{8,16,24},  new int[]{24,4,24});
        drawEar(g, new int[]{40,48,56}, new int[]{24,4,24});

        g.setColor(new Color(255, 150, 150));
        drawEar(g, new int[]{12,16,20}, new int[]{22,10,22});
        drawEar(g, new int[]{44,48,52}, new int[]{22,10,22});

        g.setColor(Color.BLACK);
        g.fillOval(20, 32, 8, 10);
        g.fillOval(36, 32, 8, 10);
        g.setColor(Color.WHITE);
        g.fillOval(22, 34, 3, 3);
        g.fillOval(38, 34, 3, 3);

        g.setColor(new Color(255, 150, 150));
        g.fillPolygon(new int[]{32,28,36}, new int[]{44,50,50}, 3);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawArc(24, 48, 8, 6, 0, -180);
        g.drawArc(32, 48, 8, 6, 0, -180);
        g.drawLine( 8,44, 20,46); g.drawLine( 8,48, 20,48); g.drawLine( 8,52, 20,50);
        g.drawLine(56,44, 44,46); g.drawLine(56,48, 44,48); g.drawLine(56,52, 44,50);

        g.dispose();
        return img;
    }

    private static void drawEar(Graphics2D g, int[] xs, int[] ys) {
        g.fillPolygon(xs, ys, 3);
    }
}