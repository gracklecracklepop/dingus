import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.lang.management.ManagementFactory;

public class measuring extends JPanel {
    static boolean done = false;
   // ramUsage rm = new ramUsage();
    public static void test(){
        System.out.println("No save file found — starting fresh.");
        OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();
        try {
            ramUsage.runTimedMode(osBean, 10);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
   
}
