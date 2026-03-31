import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;


public class ramUsage {
    public static void main(String[] args) throws InterruptedException {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();


        System.out.println("=== Live System RAM Utilization (Ctrl+C to stop) ===");


        while (true) {
            long totalRam = osBean.getTotalMemorySize();
            long freeRam  = osBean.getFreeMemorySize();
            long usedRam  = totalRam - freeRam;
            double usagePercent = (double) usedRam / totalRam * 100;


            // Build a simple ASCII progress bar
            int barWidth = 30;
            int filled = (int) (usagePercent / 100 * barWidth);
            String bar = "[" + "#".repeat(filled) + "-".repeat(barWidth - filled) + "]";


            // Overwrite the same line in the terminal
            System.out.printf("\r%s %5.1f%%  |  Used: %.2f GB / %.2f GB  |  Free: %.2f GB",
                    bar, usagePercent,
                    usedRam  / 1e9,
                    totalRam / 1e9,
                    freeRam  / 1e9);


            System.out.flush();
            Thread.sleep(100);
        }
    }
}

