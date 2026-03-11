import java.awt.Desktop;
import java.net.URI;

public class webUrlGrabber {


    public static boolean openURL(String url) {
        try {
            URI uri = new URI(url);

            // Check if desktop browsing is supported
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
                System.out.println("✅ Opened URL: " + url);
                return true;
            } else {
                System.out.println("❌ Desktop browsing is not supported on this platform.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to open URL: " + url);
            e.printStackTrace();
            return false;
        }
    }


    public static boolean isBrowsingSupported() {
        return Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }


    public static void main(String[] args) {
        // Test opening a URL
        boolean success = openURL("https://images.unsplash.com/photo-1533460004989-cef01064af7e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1yZWxhdGVkfDIzfHx8ZW58MHx8fHx8&fm=jpg&q=60&w=3000");

        if (success) {
            System.out.println("URL opened successfully!");
        } else {
            System.out.println("Failed to open URL");
        }
    }
}