import java.awt.*;

public class MouseColorDetector {
    public static void main(String[] args) throws Exception {
        // Robot requires AWT permissions and may throw an AWTException
        Robot robot = new Robot();

        while (true) {
            // 1. Get current mouse position
            Point mousePos = MouseInfo.getPointerInfo().getLocation();

            // 2. Get the color at those coordinates
            Color color = robot.getPixelColor(mousePos.x, mousePos.y);

            // Output the results
            System.out.println("Coordinates: " + mousePos.x + ", " + mousePos.y +
                    " | RGB: " + color.getRed() + ", " +
                    color.getGreen() + ", " + color.getBlue());

            // Slow down the loop to save CPU cycles
            Thread.sleep(100);
        }
    }
}
