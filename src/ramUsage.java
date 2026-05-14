import com.sun.management.OperatingSystemMXBean;

public class ramUsage {

    /** Returns used RAM in MiB (matches SetupWizard baseRam units). */
    public static long runRamLiveMode(OperatingSystemMXBean osBean) {
        long total = osBean.getTotalMemorySize();
        long free  = osBean.getFreeMemorySize();
        long used  = total - free;
        return used / (1024L * 1024L);
    }

    /** Returns CPU load as percent (0..100). Non-blocking. */
    public static double runCpuLiveMode(OperatingSystemMXBean osBean) {
        double load = osBean.getCpuLoad(); // -1.0 if unavailable
        if (load < 0) return 0.0;
        return load * 100.0;
    }
}