import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class imageSaver {

    private static final String DESKTOP = System.getProperty("user.home") + "/Desktop/";
    private static final Random random = new Random();
    private static volatile boolean running = false;
    private static Thread saverThread = null;
    private static String lastSavedFilename = null;

    /**
     * Start saving images at random intervals
     * @param sourcePath Path to the source image file
     * @param minSeconds Minimum seconds between saves
     * @param maxSeconds Maximum seconds between saves
     * @return true if started successfully, false if already running or invalid parameters
     */
    public static boolean startRandomSaving(String sourcePath, int minSeconds, int maxSeconds) {
        if (running) {
            System.out.println("⚠️ Random image saver is already running");
            return false;
        }

        if (minSeconds > maxSeconds) {
            System.err.println("❌ minSeconds cannot be greater than maxSeconds");
            return false;
        }

        if (minSeconds < 0 || maxSeconds < 0) {
            System.err.println("❌ Time values must be positive");
            return false;
        }

        // Check if source file exists
        if (!Files.exists(Paths.get(sourcePath))) {
            System.err.println("❌ Source file not found: " + sourcePath);
            return false;
        }

        running = true;

        saverThread = new Thread(() -> {
            System.out.println("🎲 Started random image saver...");
            System.out.println("📁 Source: " + sourcePath);
            System.out.println("⏱️ Interval: " + minSeconds + "-" + maxSeconds + " seconds");

            while (running) {
                try {
                    // Calculate random delay between min and max
                    int delay = minSeconds + random.nextInt(maxSeconds - minSeconds + 1);
                    System.out.println("⏳ Waiting " + delay + " seconds until next save...");
                    Thread.sleep(delay * 1000L);

                    // Save the image
                    if (running) { // Check again in case stop was called during sleep
                        saveImage(sourcePath);
                    }

                } catch (InterruptedException e) {
                    System.out.println("🛑 Saver stopped (interrupted)");
                    break;
                } catch (IOException e) {
                    System.err.println("❌ Failed to save image: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("🛑 Random image saver stopped");
        });

        saverThread.setDaemon(true); // Won't prevent app from closing
        saverThread.start();

        return true;
    }

    /**
     * Save a single image to the desktop with timestamp
     * @param sourcePath Path to the source image file
     * @return filename of saved image, or null if failed
     */
    private static String saveImage(String sourcePath) throws IOException {
        // Create timestamp for unique filename
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "random_img_" + timestamp + ".png";

        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(DESKTOP + filename);

        // Check if source exists
        if (!Files.exists(source)) {
            throw new IOException("Source file not found: " + sourcePath);
        }

        // Copy the file
        Files.copy(source, destination);

        lastSavedFilename = filename;
        System.out.println("✅ Saved: " + filename);

        return filename;
    }

    /**
     * Save a single image immediately (no random delay)
     * @param sourcePath Path to the source image file
     * @param customName Custom filename (without extension)
     * @return filename of saved image, or null if failed
     */
    public static String saveImageNow(String sourcePath, String customName) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = customName + "_" + timestamp + ".png";

            Path source = Paths.get(sourcePath);
            Path destination = Paths.get(DESKTOP + filename);

            if (!Files.exists(source)) {
                System.err.println("❌ Source file not found: " + sourcePath);
                return null;
            }

            Files.copy(source, destination);
            lastSavedFilename = filename;
            System.out.println("✅ Saved: " + filename);

            return filename;

        } catch (IOException e) {
            System.err.println("❌ Failed to save image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save image with default name
     * @param sourcePath Path to the source image file
     * @return filename of saved image, or null if failed
     */
    public static String saveImageNow(String sourcePath) {
        return saveImageNow(sourcePath, "image");
    }

    /**
     * Stop the random saving process
     * @return true if stopped successfully, false if wasn't running
     */
    public static boolean stop() {
        if (!running) {
            System.out.println("⚠️ Random image saver is not running");
            return false;
        }

        running = false;

        // Interrupt the thread if it's sleeping
        if (saverThread != null && saverThread.isAlive()) {
            saverThread.interrupt();
        }

        return true;
    }

    /**
     * Check if the saver is currently running
     * @return true if running, false otherwise
     */
    public static boolean isRunning() {
        return running;
    }

    /**
     * Get the desktop path
     * @return the desktop directory path
     */
    public static String getDesktopPath() {
        return DESKTOP;
    }

    /**
     * Get the last saved filename
     * @return the last saved filename, or null if none saved yet
     */
    public static String getLastSavedFilename() {
        return lastSavedFilename;
    }

    /**
     * Get the full path of the last saved file
     * @return the full path, or null if none saved yet
     */
    public static String getLastSavedPath() {
        if (lastSavedFilename == null) {
            return null;
        }
        return DESKTOP + lastSavedFilename;
    }

    /**
     * Test method
     */
    public static void main(String[] args) throws InterruptedException {
        // Test immediate save
        String savedFile = saveImageNow("src/pngimg.com - cat_PNG115412.png", "test");
        if (savedFile != null) {
            System.out.println("✅ Immediate save successful: " + savedFile);
        } else {
            System.out.println("❌ Immediate save failed");
        }

        // Start random saving (every 5-10 seconds)
        boolean started = startRandomSaving("src/pngimg.com - cat_PNG115412.png", 5, 10);

        if (!started) {
            System.out.println("❌ Failed to start random saver");
            return;
        }

        // Let it run for 30 seconds
        System.out.println("⏱️ Running for 30 seconds...");
        Thread.sleep(30000);

        // Stop the saver
        boolean stopped = stop();
        System.out.println("Stop successful: " + stopped);

        // Print last saved file info
        System.out.println("Last saved: " + getLastSavedFilename());
        System.out.println("Full path: " + getLastSavedPath());

        System.out.println("🎉 Test completed!");
    }
}