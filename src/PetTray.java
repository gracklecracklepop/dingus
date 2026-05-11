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

        TrayNotifier.ensureInitialized();

        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        Dimension size  = tray.getTrayIconSize();
        Image scaled    = icon.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);

        PopupMenu popup = new PopupMenu();
        addItem(popup, "Show Pet",  () -> show(window));
        addItem(popup, "Hide Pet",  () -> hide(window));
        popup.addSeparator();
        addItem(popup, "Exit",  () -> { remove(); System.exit(0); });

        trayIcon = new TrayIcon(scaled, "Dingus", popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { if (visible) hide(window); else show(window); }
            }
        });

        try {
            tray.add(trayIcon);
            TrayNotifier.bind(trayIcon);
        } catch (Exception ignored) {}
    }

    public static void hide(Window window) {
        window.setVisible(false);
        if (bed != null) bed.setVisible(false);
        visible = false;
        TrayNotifier.showNotification("Dingus", "I'm hiding in the tray!");
    }

    public static void show(Window window) {
        if (bed instanceof BedDialog bedDialog) {
            bedDialog.positionAtBottom();
            bedDialog.setVisible(true);
            bedDialog.bumpTopmost();
            bedDialog.toBack();
        } else if (bed != null) {
            bed.setVisible(true);
        }

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