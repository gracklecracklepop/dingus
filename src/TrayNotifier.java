import java.awt.*;

public class TrayNotifier {

    public static String DEFAULT_ICON_PATH = "images/icon.png";

    private static TrayIcon trayIcon;
    private static boolean initialized = false;
    private static TrayIcon ownedIcon = null;

    public static boolean ensureInitialized() {
        return ensureInitialized(DEFAULT_ICON_PATH);
    }

    public static boolean ensureInitialized(String iconPath) {
        if (initialized && trayIcon != null) return true;
        return initialize(iconPath);
    }

    public static boolean initialize(String iconPath) {
        if (initialized && trayIcon != null) return true;

        if (!SystemTray.isSupported()) return false;

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(iconPath);

            TrayIcon icon = new TrayIcon(image, "Dingus");
            icon.setImageAutoSize(true);
            icon.setToolTip("Dingus");

            tray.add(icon);

            trayIcon = icon;
            ownedIcon = icon;
            initialized = true;
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /** Bind to the PetTray icon to avoid duplicates. */
    public static void bind(TrayIcon existingTrayIcon) {
        if (existingTrayIcon == null) return;

        if (ownedIcon != null) {
            try { SystemTray.getSystemTray().remove(ownedIcon); } catch (Exception ignored) {}
            ownedIcon = null;
        }

        trayIcon = existingTrayIcon;
        initialized = true;
    }

    public static boolean showNotification(String title, String message, TrayIcon.MessageType type) {
        if (!initialized || trayIcon == null) {
            if (!ensureInitialized()) return false;
        }
        try {
            trayIcon.displayMessage(title, message, type);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean showNotification(String title, String message) {
        return showNotification(title, message, TrayIcon.MessageType.INFO);
    }
}