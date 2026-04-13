import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Theme {

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  BACKGROUNDS  ██████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color BG_MAIN              = new Color(40, 40, 40);
    public static final Color BG_MAIN_TRANSPARENT  = new Color(40, 40, 40, 255);
    public static final Color BG_INPUT             = new Color(60, 60, 60);
    public static final Color BG_INPUT_BORDER      = new Color(80, 80, 80);
    public static final Color BG_DROPDOWN_ITEM     = new Color(50, 50, 50);
    public static final Color BG_DROPDOWN_SELECTED = new Color(80, 120, 80);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  BUTTONS  ██████████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color BTN_DEFAULT          = new Color(60, 60, 60);
    public static final Color BTN_HOVER            = new Color(100, 100, 100);
    public static final Color BTN_PRESSED          = new Color(80, 80, 80);
    public static final Color BTN_DISABLED         = new Color(50, 50, 50);
    public static final Color BTN_PRIMARY          = new Color(80, 150, 80);
    public static final Color BTN_SECONDARY        = new Color(100, 100, 100);
    public static final Color BTN_CLOSE            = new Color(150, 60, 60);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  TEXT COLORS  ██████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color TEXT_PRIMARY         = new Color(255, 255, 255);
    public static final Color TEXT_SECONDARY       = new Color(200, 200, 200);
    public static final Color TEXT_DISABLED        = new Color(128, 128, 128);
    public static final Color TEXT_LABEL           = new Color(255, 255, 255);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  ACCENT COLORS  ███████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color ACCENT_COINS         = new Color(255, 215, 0);
    public static final Color ACCENT_RAM           = new Color(150, 255, 150);
    public static final Color ACCENT_CPU           = new Color(150, 200, 255);
    public static final Color ACCENT_SUCCESS       = new Color(100, 200, 100);
    public static final Color ACCENT_ERROR         = new Color(255, 100, 100);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  PROGRESS BARS  ███████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color PROGRESS_TRACK       = new Color(60, 60, 60);
    public static final Color PROGRESS_HIGH        = new Color(100, 200, 100);   // >= 70%
    public static final Color PROGRESS_MED         = new Color(255, 200, 50);    // >= 30%
    public static final Color PROGRESS_LOW         = new Color(255, 80, 80);     // < 30%
    public static final Color PROGRESS_RAM         = new Color(100, 200, 100);
    public static final Color PROGRESS_CPU         = new Color(100, 150, 255);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  SHOP COLORS  █████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color BG_SHOP_SLOT         = new Color(50, 50, 50);
    public static final Color BG_SHOP_SLOT_BORDER  = new Color(70, 70, 70);
    public static final Color BG_SHOP_BUY          = new Color(70, 70, 70);
    public static final Color BG_SHOP_OWNED        = new Color(100, 150, 100);
    public static final Color BG_SHOP_NO_FUNDS     = new Color(150, 50, 50);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  SCROLLBAR  ███████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color SCROLLBAR_THUMB      = new Color(120, 120, 120, 200);
    public static final Color SCROLLBAR_TRACK      = new Color(0, 0, 0, 0);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  FONT CONFIG  █████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final String FONT_PATH           = "images/Shape Bit.otf";
    public static final String FONT_FALLBACK       = "Arial";

    public static final int FONT_SIZE_TITLE        = 22;
    public static final int FONT_SIZE_HEADING      = 17;
    public static final int FONT_SIZE_BUTTON       = 13;
    public static final int FONT_SIZE_LABEL        = 12;
    public static final int FONT_SIZE_BODY         = 12;
    public static final int FONT_SIZE_SMALL        = 10;

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  SIZING  ██████████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final int CORNER_RADIUS          = 15;
    public static final int BUTTON_CORNER_RADIUS   = 7;

    // ─── PetMenu Specific ──────────────────────────────────────────────────────
    public static final int MENU_WIDTH             = 150;
    public static final int MENU_SCROLL_HEIGHT     = 230;
    public static final int SCROLLBAR_WIDTH        = 3;

    // ─── SetupWizard Specific ──────────────────────────────────────────────────
    public static final int WIZARD_WIDTH           = 500;
    public static final int WIZARD_HEIGHT          = 400;

    // ─── Timing ────────────────────────────────────────────────────────────────
    public static final int SCAN_DURATION_SECONDS  = 15;

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  FONT LOADER  █████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    private static Font cachedFont = null;

    public static Font font(int size) {
        if (cachedFont == null) {
            try {
                cachedFont = Font.createFont(Font.TRUETYPE_FONT, new File(FONT_PATH));
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(cachedFont);
            } catch (Exception e) {
                cachedFont = new Font(FONT_FALLBACK, Font.PLAIN, size);
            }
        }
        return cachedFont.deriveFont(Font.PLAIN, (float) size);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  HELPERS  ██████████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static Color progressColor(int value) {
        if (value >= 70) {
            return PROGRESS_HIGH;
        } else if (value >= 30) {
            return PROGRESS_MED;
        } else {
            return PROGRESS_LOW;
        }

    }

    public static void applyUIManagerDefaults() {
        UIManager.put("ComboBox.background",          BG_INPUT);
        UIManager.put("ComboBox.foreground",          TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", BG_DROPDOWN_SELECTED);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);
        UIManager.put("ComboBox.disabledBackground",  BG_INPUT);
        UIManager.put("ComboBox.disabledForeground",  TEXT_DISABLED);

        UIManager.put("TextField.background",          BG_INPUT);
        UIManager.put("TextField.foreground",          TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",     TEXT_PRIMARY);
        UIManager.put("TextField.selectionBackground", BG_DROPDOWN_SELECTED);
        UIManager.put("TextField.selectionForeground", TEXT_PRIMARY);

        UIManager.put("List.background",          BG_DROPDOWN_ITEM);
        UIManager.put("List.foreground",          TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", BG_DROPDOWN_SELECTED);
        UIManager.put("List.selectionForeground", TEXT_PRIMARY);

        UIManager.put("PopupMenu.background", BG_DROPDOWN_ITEM);
        UIManager.put("PopupMenu.foreground", TEXT_PRIMARY);
        UIManager.put("PopupMenu.border",     BorderFactory.createLineBorder(BG_INPUT_BORDER, 1));
    }
}