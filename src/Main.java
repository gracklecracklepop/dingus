import java.awt.*;
import java.awt.Window.Type;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
    static final int PET_WIDTH  = 360;
    static final int PET_HEIGHT = 290;

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        Theme.applyUIManagerDefaults();

        TrayNotifier.DEFAULT_ICON_PATH = "images/yarn_icon.png";
        TrayNotifier.ensureInitialized();

        SwingUtilities.invokeLater(() -> {
            // First-time setup
            if (!SaveManager.saveExists()) {
                SetupWizard wizard = new SetupWizard();
                wizard.setVisible(true);
                if (!wizard.isFinished()) System.exit(0);
                SaveManager.save(wizard.getGeneratedStats());
            }

            // Hidden owner keeps dialogs grouped
            JFrame hiddenOwner = new JFrame();
            hiddenOwner.setUndecorated(true);
            hiddenOwner.setSize(0, 0);
            hiddenOwner.setType(Type.UTILITY);
            hiddenOwner.setVisible(true);

            Image icon = loadAppIcon();
            hiddenOwner.setIconImages(List.of(
                    icon.getScaledInstance(16,  16,  Image.SCALE_SMOOTH),
                    icon.getScaledInstance(32,  32,  Image.SCALE_SMOOTH),
                    icon.getScaledInstance(64,  64,  Image.SCALE_SMOOTH),
                    icon.getScaledInstance(128, 128, Image.SCALE_SMOOTH)
            ));

            // ── Bed dialog: CREATE + POSITION, but DO NOT SHOW at boot ─────────
            // This prevents the “bed only” flash.
            BedDialog bed = new BedDialog();
            bed.positionAtBottom();
            bed.setVisible(false);

            // ── Pet dialog ─────────────────────────────────────────────────────
            JDialog dialog = new JDialog(hiddenOwner);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));
            dialog.setLayout(new BorderLayout());
            dialog.setSize(PET_WIDTH, PET_HEIGHT);
            dialog.setResizable(false);
            dialog.setAlwaysOnTop(true);

            // Spawn relative to bed position (works even if bed is hidden)
            Point snap = BedDialog.getCatSnapPosition();
            dialog.setLocation(snap.x, snap.y);

            PetPanel panel = new PetPanel(dialog);
            panel.setOpaque(false);
            dialog.add(panel, BorderLayout.CENTER);

            // Give panel bed reference (panel will keep bed hidden at start because isInBed=true)
            panel.setBedDialog(bed);

            PetTray.setup(dialog, bed, icon);
            attachDragListener(panel, dialog);

            dialog.setVisible(true);

            // Ensure pet is topmost
            SwingUtilities.invokeLater(() -> {
                dialog.setAlwaysOnTop(true);
                dialog.toFront();
            });

            imageSaver.startRandomSaving("images/poo.png", 1800, 3600);
        });
    }

    private static void attachDragListener(final PetPanel panel, final JDialog dialog) {
        final Point[] dragOffset = new Point[]{null};
        final boolean[] active = new boolean[]{false};

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!panel.isOverMenuButton(e.getPoint())) {
                    panel.setDragging(true);
                    Point dialogLoc = dialog.getLocation();
                    dragOffset[0] = new Point(
                            e.getXOnScreen() - dialogLoc.x,
                            e.getYOnScreen() - dialogLoc.y
                    );
                    active[0] = true;
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                if (active[0]) {
                    active[0] = false;
                    panel.setDragging(false);
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (active[0] && dragOffset[0] != null) {
                    int newX = e.getXOnScreen() - dragOffset[0].x;
                    int newY = e.getYOnScreen() - dragOffset[0].y;
                    dialog.setLocation(newX, newY);
                }
            }
        });
    }

    static Image loadAppIcon() {
        for (String path : new String[]{"images/icon.png", "images/yarn_icon.png"}) {
            try { return ImageIO.read(new File(path)); }
            catch (IOException ignored) {}
        }
        return createFallbackIcon();
    }

    private static Image createFallbackIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(255, 200, 100));
        g.fillOval(8, 16, 48, 44);

        g.setColor(Color.BLACK);
        g.fillOval(20, 32, 8, 10);
        g.fillOval(36, 32, 8, 10);

        g.dispose();
        return img;
    }
}