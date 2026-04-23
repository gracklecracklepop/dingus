import java.nio.file.*;
import java.io.IOException;
import java.util.Random;

public class imageSaver {

    private static final String DESKTOP = System.getProperty("user.home") + "/Desktop/";
    private static final Random random = new Random();
    private static volatile boolean running = false;
    private static Thread saverThread = null;
    private static String lastSavedFilename = null;

    public static boolean startRandomSaving(String sourcePath, int minSeconds, int maxSeconds) {
        if (running) {
            System.out.println("⚠️ Already running");
            return false;
        }

        if (minSeconds > maxSeconds || minSeconds < 0) {
            System.err.println("❌ Invalid time range");
            return false;
        }

        if (!Files.exists(Paths.get(sourcePath))) {
            System.err.println("❌ Source file not found");
            return false;
        }

        running = true;

        saverThread = new Thread(() -> {
            System.out.println("🎲 Started random image saver (runs forever)");

            while (running) {
                try {
                    int delay = minSeconds + random.nextInt(maxSeconds - minSeconds + 1);
                    System.out.println("⏳ Waiting " + delay + " seconds...");
                    Thread.sleep(delay * 1000L);

                    if (running) {
                        saveImage(sourcePath);
                    }

                } catch (InterruptedException e) {
                    System.out.println("🛑 Stopped");
                    break;
                } catch (IOException e) {
                    System.err.println("❌ Error: " + e.getMessage());
                }
            }
        });

        saverThread.setDaemon(false); // keep program alive
        saverThread.start();

        return true;
    }

    private static String saveImage(String sourcePath) throws IOException {
        Path source = Paths.get(sourcePath);

        String baseName = "poo";
        String extension = ".png";

        Path destination = Paths.get(DESKTOP, baseName + extension);

        // First file: catsitting.png
        if (!Files.exists(destination)) {
            Files.copy(source, destination);
            lastSavedFilename = destination.getFileName().toString();
            System.out.println("✅ Saved: " + lastSavedFilename);
            return lastSavedFilename;
        }

        // Next files: catsitting_1, _2, _3...
        int counter = 1;
        do {
            destination = Paths.get(DESKTOP, baseName + "_" + counter + extension);
            counter++;
        } while (Files.exists(destination));

        Files.copy(source, destination);

        lastSavedFilename = destination.getFileName().toString();
        System.out.println("✅ Saved: " + lastSavedFilename);

        return lastSavedFilename;
    }

    public static boolean stop() {
        if (!running) {
            System.out.println("⚠️ Not running");
            return false;
        }

        running = false;

        if (saverThread != null) {
            saverThread.interrupt();
        }

        return true;
    }

    public static void main(String[] args) {
        // Start it (runs forever until you kill program or call stop())
        startRandomSaving("images/poo.png", 30, 100);

        // Optional: keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}