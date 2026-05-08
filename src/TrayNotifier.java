import java.awt.*;

public class TrayNotifier {

    // Default icon path used by ensureInitialized() if you don't pass one.
    // Change this once to wherever your tray icon actually is.
    public static String DEFAULT_ICON_PATH = "icon.png";

    private static TrayIcon trayIcon;
    private static boolean initialized = false;

    // If we created our own icon, we can remove it later when PetTray binds its icon.
    private static TrayIcon ownedIcon = null;

    /** Ensure tray is ready using DEFAULT_ICON_PATH. Safe to call many times. */
    public static boolean ensureInitialized() {
        return ensureInitialized(DEFAULT_ICON_PATH);
    }

    /** Ensure tray is ready. Safe to call many times. */
    public static boolean ensureInitialized(String iconPath) {
        if (initialized && trayIcon != null) return true;
        return initialize(iconPath);
    }

    /**
     * Initialize the system tray icon (temporary icon).
     * If PetTray later calls bind(...), this temporary icon will be removed.
     */
    public static boolean initialize(String iconPath) {
        if (initialized && trayIcon != null) return true;

        if (!SystemTray.isSupported()) {
            System.err.println("❌ SystemTray is not supported on this platform.");
            return false;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit().createImage(iconPath);
            TrayIcon icon = new TrayIcon(image, "Dingus");
            icon.setImageAutoSize(true);
            icon.setToolTip("Dingus");

            tray.add(icon);

            trayIcon = icon;
            ownedIcon = icon;   // mark as temporary-owned
            initialized = true;

            return true;
        } catch (AWTException e) {
            System.err.println("❌ TrayIcon could not be added to system tray");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize tray icon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Bind TrayNotifier to an existing TrayIcon (your PetTray icon).
     * This prevents duplicate tray icons and makes all notifications use the PetTray icon.
     */
    public static void bind(TrayIcon existingTrayIcon) {
        if (existingTrayIcon == null) return;

        // If we previously created a temporary icon, remove it.
        if (ownedIcon != null) {
            try {
                SystemTray.getSystemTray().remove(ownedIcon);
            } catch (Exception ignored) {}
            ownedIcon = null;
        }

        trayIcon = existingTrayIcon;
        initialized = true;
    }

    public static boolean showNotification(String title, String message) {
        return showNotification(title, message, TrayIcon.MessageType.INFO);
    }

    public static boolean showNotification(String title, String message, TrayIcon.MessageType messageType) {
        if (!initialized || trayIcon == null) {
            // Try to auto-init with default icon
            if (!ensureInitialized()) return false;
        }

        try {
            trayIcon.displayMessage(title, message, messageType);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to show notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean remove() {
        if (!initialized || trayIcon == null) return false;

        try {
            // Only remove if it's the temporary icon we owned.
            if (ownedIcon != null) {
                SystemTray.getSystemTray().remove(ownedIcon);
                ownedIcon = null;
            }
            initialized = false;
            trayIcon = null;
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to remove tray icon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean isSupported() {
        return SystemTray.isSupported();
    }
}