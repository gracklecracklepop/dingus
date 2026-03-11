import java.awt.*;

public class TrayNotifier {

    private static TrayIcon trayIcon;
    private static boolean initialized = false;

    /**
     * Initialize the system tray icon
     * @param iconPath Path to the icon image file
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initialize(String iconPath) {
        if (initialized) {
            System.out.println("⚠️ TrayNotifier already initialized");
            return true;
        }

        if (!SystemTray.isSupported()) {
            System.err.println("❌ SystemTray is not supported on this platform.");
            return false;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Load the icon image
            Image image = Toolkit.getDefaultToolkit().createImage(iconPath);

            trayIcon = new TrayIcon(image, "Dinglet");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Dinglet - Your Virtual Pet");

            tray.add(trayIcon);
            initialized = true;
            System.out.println("✅ System tray icon initialized");
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
     * Show a notification in the system tray
     * @param title The notification title
     * @param message The notification message
     * @return true if notification was shown, false otherwise
     */
    public static boolean showNotification(String title, String message) {
        return showNotification(title, message, TrayIcon.MessageType.INFO);
    }

    /**
     * Show a notification with a specific message type
     * @param title The notification title
     * @param message The notification message
     * @param messageType The type of notification (INFO, WARNING, ERROR, NONE)
     * @return true if notification was shown, false otherwise
     */
    public static boolean showNotification(String title, String message, TrayIcon.MessageType messageType) {
        if (!initialized || trayIcon == null) {
            System.err.println("❌ TrayIcon not initialized. Call initialize() first.");
            return false;
        }

        try {
            trayIcon.displayMessage(title, message, messageType);
            System.out.println("📢 Notification shown: " + title);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to show notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove the tray icon
     * @return true if icon was removed, false otherwise
     */
    public static boolean remove() {
        if (!initialized || trayIcon == null) {
            System.out.println("⚠️ Tray icon not initialized, nothing to remove");
            return false;
        }

        try {
            SystemTray.getSystemTray().remove(trayIcon);
            initialized = false;
            trayIcon = null;
            System.out.println("🗑️ Tray icon removed");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to remove tray icon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if the tray notifier is initialized
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Check if system tray is supported on this platform
     * @return true if supported, false otherwise
     */
    public static boolean isSupported() {
        return SystemTray.isSupported();
    }

    /**
     * Test method
     */
    public static void main(String[] args) {
        try {
            // Initialize with an icon
            boolean initSuccess = initialize("icon.png");

            if (!initSuccess) {
                System.out.println("Failed to initialize, exiting...");
                return;
            }

            // Show different types of notifications
            boolean notifSuccess = showNotification("Dinglet has something to say!", "I want to go on a walk!");
            System.out.println("Notification 1 success: " + notifSuccess);

            // Wait a bit, then show another
            Thread.sleep(3000);
            notifSuccess = showNotification("Dinglet is happy!", "Thanks for playing with me! 🎾", TrayIcon.MessageType.INFO);
            System.out.println("Notification 2 success: " + notifSuccess);

            // Wait before removing
            Thread.sleep(3000);
            boolean removeSuccess = remove();
            System.out.println("Remove success: " + removeSuccess);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}