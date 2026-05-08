import java.awt.*;
import java.awt.event.*;

public class PetTray {

    private static TrayIcon trayIcon;
    private static boolean  visible = true;
    private static Window   bed;
    private static Window   petWindow;

    public static void setup(Window window, Window bedWindow, Image icon) {
        bed = bedWindow;
        petWindow = window;

        // Ensure notifier exists early (won’t duplicate because bind() swaps it later)
        TrayNotifier.ensureInitialized();

        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Dimension size  = tray.getTrayIconSize();
        Image scaled    = icon.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);

        PopupMenu popup = new PopupMenu();
        addItem(popup, "Show Pet",  () -> show(window));
        addItem(popup, "Hide Pet",  () -> hide(window));
        popup.addSeparator();
        addItem(popup, "Feed",  () -> TrayNotifier.showNotification("Dingus", "Yummy! Thanks for the food!"));
        addItem(popup, "Play",  () -> TrayNotifier.showNotification("Dingus", "Let's play!"));
        popup.addSeparator();
        addItem(popup, "Exit",  () -> { remove(); System.exit(0); });

        trayIcon = new TrayIcon(scaled, "Dingus", popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (visible) hide(window); else show(window);
                }
            }
        });

        try {
            tray.add(trayIcon);

            // CRITICAL: bind TrayNotifier to THIS icon so you don’t get duplicates
            TrayNotifier.bind(trayIcon);

        } catch (AWTException e) {
            System.err.println("Tray error: " + e.getMessage());
        }
    }

    public static void hide(Window window) {
        window.setVisible(false);
        if (bed != null) bed.setVisible(false);
        visible = false;

        TrayNotifier.showNotification("Dingus", "I'm hiding in the tray! Double-click to bring me back.");
    }

    public static void show(Window window) {
        // Re-position bed and pet in case resolution/scale changed while hidden
        if (bed instanceof BedDialog bedDialog) {
            bedDialog.positionAtBottom();
        }
        if (bed != null) bed.setVisible(true);

        window.setLocation(BedDialog.getCatSnapPosition());
        window.setVisible(true);
        window.setAlwaysOnTop(true);
        window.toFront();

        visible = true;
    }

    public static void remove() {
        if (trayIcon != null) SystemTray.getSystemTray().remove(trayIcon);
        trayIcon = null;
    }

    private static void addItem(PopupMenu menu, String label, Runnable action) {
        MenuItem item = new MenuItem(label);
        item.addActionListener(e -> action.run());
        menu.add(item);
    }
}