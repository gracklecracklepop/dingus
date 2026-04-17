import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ramUsage {
    public static void main(String[] args) throws InterruptedException {


        // Set to true to run for 30 seconds and collect samples
        boolean timedMode = true;
        int durationSeconds = 30;

        if (timedMode) {
        } else {

        }
    }

    static void runTimedMode(OperatingSystemMXBean osBean, int durationSeconds)
            throws InterruptedException {

        List<Long> usageSamples = new ArrayList<>();
        long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        long nextSampleTime = System.currentTimeMillis() + 1000L;

        System.out.println("=== Timed RAM Monitor (" + durationSeconds + "s) — Ctrl+C to abort ===");

        while (System.currentTimeMillis() < endTime) {
            long totalRam = osBean.getTotalMemorySize();
            long freeRam  = osBean.getFreeMemorySize();
            long usedRam  = totalRam - freeRam;
            double usagePercent = (double) usedRam / totalRam * 100;

            // Collect one sample per second
            if (System.currentTimeMillis() >= nextSampleTime) {
                usageSamples.add(usedRam);
                nextSampleTime += 1000L;
            }

            // Live display
            int barWidth = 30;
            int filled = (int) (usagePercent / 100 * barWidth);
            String bar = "[" + "#".repeat(filled) + "-".repeat(barWidth - filled) + "]";

            long remaining = (endTime - System.currentTimeMillis()) / 1000;

            System.out.flush();
            Thread.sleep(100);
        }

        printSummary(usageSamples);
    }

    static long runLiveMode(OperatingSystemMXBean osBean)
            throws InterruptedException {


            long totalRam = osBean.getTotalMemorySize();
            long freeRam  = osBean.getFreeMemorySize();

            return (totalRam-freeRam)/1000000;

    }

    private static void printSummary(List<Long> samples) {
        System.out.println("\n\n=== 30-Second Summary ===");
        System.out.printf("Samples collected : %d%n", samples.size());

        if (samples.isEmpty()) return;

        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (long sample : samples) {
            sum += sample;
            if (sample < min) min = sample;
            if (sample > max) max = sample;
        }

        long avg = sum / samples.size();

        System.out.printf("Average RAM used  : %.2f GB%n", avg / 1e9);
        System.out.printf("Min RAM used      : %.2f GB%n", min / 1e9);
        System.out.printf("Max RAM used      : %.2f GB%n", max / 1e9);
        System.out.printf("Total (sum)       : %.2f GB-samples%n", sum / 1e9);

        // Print each second's reading
        System.out.println("\n--- Per-Second Log ---");
        for (int i = 0; i < samples.size(); i++) {
            System.out.printf("  t=%2ds  →  %.2f GB%n", i + 1, samples.get(i) / 1e9);
        }
        System.out.println(avg);
    }
}