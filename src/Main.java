import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.*;



    private static final String DESKTOP = System.getProperty("user.home") + "/Desktop/";
    private static final Random random = new Random();
    private static volatile boolean running = true;

    public static void startRandomSaving(String sourcePath, int minSeconds, int maxSeconds) {
        Thread saverThread = new Thread(() -> {
            System.out.println("🎲 Started random image saver...");

            while (running) {
                try {
                    // Random delay between min and max
                    int delay = minSeconds + random.nextInt(maxSeconds - minSeconds + 1);
                    System.out.println("⏳ Waiting " + delay + " seconds until next save...");
                    Thread.sleep(delay * 1000L);

                    // Save the image
                    saveImage(sourcePath);

                } catch (InterruptedException e) {
                    System.out.println("🛑 Saver stopped");
                    break;
                } catch (IOException e) {
                    System.err.println("❌ Failed to save image: " + e.getMessage());
                }
            }
        });

        saverThread.setDaemon(true); // Won't prevent app from closing
        saverThread.start();
    }

    private static void saveImage(String sourcePath) throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "random_img_" + timestamp + ".png";

        Files.copy(
                Paths.get(sourcePath),
                Paths.get(DESKTOP + filename)
        );

        System.out.println("✅ Saved: " + filename);
    }

    public static void stop() {
        running = false;
    }

    public static void main(String[] args) throws InterruptedException {

        startRandomSaving("src/pngimg.com - cat_PNG115412.png", 5, 10);


        Thread.sleep(60000);
        stop();
    }
