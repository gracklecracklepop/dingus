import javax.swing.filechooser.FileSystemView;
import java.io.InputStream;
import java.nio.file.*;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

public class imageSaver {

    private static final Random random = new Random();
    private static volatile boolean running = false;
    private static Thread saverThread = null;
    private static String lastSavedFilename = null;

    private static volatile Path cachedDesktopDir = null;

    public static boolean isRunning() {
        return running;
    }

    public static String getLastSavedFilename() {
        return lastSavedFilename;
    }

    /** Backwards-compatible: no callback */
    public static boolean startRandomSaving(String sourcePath, int minSeconds, int maxSeconds) {
        return startRandomSaving(sourcePath, minSeconds, maxSeconds, null);
    }

    /**
     * Starts a background saver. Calls onSaved.accept(filename) after each successful save.
     * Stops when stop() is called.
     */
    public static synchronized boolean startRandomSaving(
            String sourcePath,
            int minSeconds,
            int maxSeconds,
            Consumer<String> onSaved
    ) {
        if (running) {
            System.err.println("[imageSaver] already running");
            return false;
        }

        if (minSeconds > maxSeconds || minSeconds < 0) {
            System.err.println("[imageSaver] invalid time range: " + minSeconds + ".." + maxSeconds);
            return false;
        }

        // validate that we can open the source at least once
        if (!canOpenSource(sourcePath)) {
            System.err.println("[imageSaver] source not found as file or resource: " + sourcePath);
            System.err.println("[imageSaver] tried: " + resolveFileSource(sourcePath));
            return false;
        }

        running = true;

        saverThread = new Thread(() -> {
            try {
                while (running) {
                    int delay = minSeconds + random.nextInt(maxSeconds - minSeconds + 1);
                    Thread.sleep(delay * 1000L);
                    if (!running) break;

                    try {
                        String saved = saveImage(sourcePath);
                        if (saved != null && onSaved != null) {
                            try { onSaved.accept(saved); } catch (Throwable ignored) {}
                        }
                    } catch (IOException e) {
                        System.err.println("[imageSaver] copy failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException ignored) {
                // stopping
            } finally {
                running = false;
            }
        }, "dingus-image-saver");

        // daemon so it can't prevent app exit
        saverThread.setDaemon(true);
        saverThread.start();
        return true;
    }

    /** For DEV/Debug: save exactly once immediately (no thread). */
    public static String saveOnce(String sourcePath) throws IOException {
        return saveImage(sourcePath);
    }

    public static synchronized boolean stop() {
        if (!running) return false;

        running = false;
        if (saverThread != null && Thread.currentThread() != saverThread) {
            saverThread.interrupt();
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // Source resolution (file OR classpath resource)
    // ─────────────────────────────────────────────────────────────

    private static Path resolveFileSource(String sourcePath) {
        Path p = Paths.get(sourcePath);
        if (p.isAbsolute()) return p;

        // resolve relative paths against working directory
        return Paths.get(System.getProperty("user.dir")).resolve(sourcePath).normalize();
    }

    private static boolean canOpenSource(String sourcePath) {
        Path file = resolveFileSource(sourcePath);
        if (Files.exists(file)) return true;

        String resKey = sourcePath.startsWith("/") ? sourcePath.substring(1) : sourcePath;
        try (InputStream in = imageSaver.class.getClassLoader().getResourceAsStream(resKey)) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }

    private interface SourceStreamProvider {
        InputStream open() throws IOException;
    }

    private static SourceStreamProvider sourceProvider(String sourcePath) throws IOException {
        Path file = resolveFileSource(sourcePath);
        if (Files.exists(file)) {
            return () -> Files.newInputStream(file, StandardOpenOption.READ);
        }

        String resKey = sourcePath.startsWith("/") ? sourcePath.substring(1) : sourcePath;
        InputStream test = imageSaver.class.getClassLoader().getResourceAsStream(resKey);
        if (test == null) throw new IOException("Resource not found: " + resKey);
        test.close();

        return () -> {
            InputStream in = imageSaver.class.getClassLoader().getResourceAsStream(resKey);
            if (in == null) throw new IOException("Resource not found: " + resKey);
            return in;
        };
    }

    // ─────────────────────────────────────────────────────────────
    // Desktop resolution
    // ─────────────────────────────────────────────────────────────

    private static Path desktopDir() {
        if (cachedDesktopDir != null) return cachedDesktopDir;

        String home = System.getProperty("user.home");
        String userProfile = System.getenv("USERPROFILE");
        if (userProfile != null && !userProfile.isBlank()) home = userProfile;

        String oneDriveEnv = System.getenv("OneDrive"); // common Windows
        Path oneDrive = (oneDriveEnv != null && !oneDriveEnv.isBlank()) ? Paths.get(oneDriveEnv) : null;

        Path[] candidates = new Path[] {
                Paths.get(home, "Desktop"),
                (oneDrive != null) ? oneDrive.resolve("Desktop") : null,
                Paths.get(home, "OneDrive", "Desktop"),
                Paths.get(home, "OneDrive - Personal", "Desktop"),
                Paths.get(home, "OneDrive - Documents", "Desktop")
        };

        for (Path p : candidates) {
            if (p != null && Files.isDirectory(p)) {
                cachedDesktopDir = p;
                System.out.println("[imageSaver] desktopDir=" + cachedDesktopDir.toAbsolutePath());
                return cachedDesktopDir;
            }
        }

        // Swing's idea of "home" is often Desktop on Windows
        try {
            Path fsv = FileSystemView.getFileSystemView().getHomeDirectory().toPath();
            if (Files.isDirectory(fsv)) {
                cachedDesktopDir = fsv;
                System.out.println("[imageSaver] desktopDir=" + cachedDesktopDir.toAbsolutePath());
                return cachedDesktopDir;
            }
        } catch (Throwable ignored) {}

        cachedDesktopDir = Paths.get(home);
        System.out.println("[imageSaver] desktopDir fallback=" + cachedDesktopDir.toAbsolutePath());
        return cachedDesktopDir;
    }

    // ─────────────────────────────────────────────────────────────
    // Save logic
    // ─────────────────────────────────────────────────────────────

    private static String saveImage(String sourcePath) throws IOException {
        SourceStreamProvider src = sourceProvider(sourcePath);

        Path dir = desktopDir();
        try { Files.createDirectories(dir); } catch (Exception ignored) {}

        String baseName = "poo";
        String extension = ".png";

        Path destination = dir.resolve(baseName + extension);
        if (Files.exists(destination)) {
            int counter = 1;
            do {
                destination = dir.resolve(baseName + "_" + counter + extension);
                counter++;
            } while (Files.exists(destination));
        }

        try (InputStream in = src.open()) {
            Files.copy(in, destination);
        }

        lastSavedFilename = destination.getFileName().toString();
        System.out.println("[imageSaver] saved -> " + destination.toAbsolutePath());
        return lastSavedFilename;
    }
}