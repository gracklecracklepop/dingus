import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestWindowCheck {

    // PowerShell script that gets the window title under the current mouse position
    private static final String PS_SCRIPT =
            "Add-Type @\"\n" +
                    "using System;\n" +
                    "using System.Runtime.InteropServices;\n" +
                    "public class WinAPI {\n" +
                    "    [DllImport(\\\"user32.dll\\\")]\n" +
                    "    public static extern IntPtr WindowFromPoint(POINT pt);\n" +
                    "    [DllImport(\\\"user32.dll\\\")]\n" +
                    "    public static extern int GetWindowText(IntPtr hWnd, System.Text.StringBuilder text, int count);\n" +
                    "    [DllImport(\\\"user32.dll\\\")]\n" +
                    "    public static extern bool GetCursorPos(out POINT pt);\n" +
                    "    [StructLayout(LayoutKind.Sequential)]\n" +
                    "    public struct POINT { public int X; public int Y; }\n" +
                    "}\n" +
                    "\"@\n" +
                    "$pt = New-Object WinAPI+POINT\n" +
                    "[WinAPI]::GetCursorPos([ref]$pt) | Out-Null\n" +
                    "$hwnd = [WinAPI]::WindowFromPoint($pt)\n" +
                    "$sb = New-Object System.Text.StringBuilder 256\n" +
                    "[WinAPI]::GetWindowText($hwnd, $sb, 256) | Out-Null\n" +
                    "$sb.ToString()";

    public static String getWindowUnderMouse() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{
                    "powershell", "-NoProfile", "-Command", PS_SCRIPT
            });
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String title = reader.readLine();
            proc.waitFor();
            return (title != null && !title.isBlank()) ? title : "(no title)";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Tracking window under mouse... (Ctrl+C to stop)");
        System.out.println("Move your mouse over different windows.\n");

        String lastTitle = "";

        while (true) {
            String title = getWindowUnderMouse();
            if (!title.equals(lastTitle)) {
                System.out.println("Window: " + title);
                lastTitle = title;
            }
            Thread.sleep(200);
        }
    }
}