import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Theme {

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  BACKGROUNDS  ██████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color BG_MAIN = new Color(40, 40, 40, 255);
    public static final Color BG_INPUT             = new Color(60, 60, 60);
    public static final Color BG_INPUT_BORDER      = new Color(80, 80, 80);
    public static final Color BG_DROPDOWN_ITEM     = new Color(50, 50, 50);
    public static final Color BG_DROPDOWN_SELECTED = new Color(88, 114, 151);

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
    public static final Color PROGRESS_HIGH        = new Color(108, 227, 99);   // >= 70%
    public static final Color PROGRESS_MED         = new Color(211, 255, 50);    // >= 30%
    public static final Color PROGRESS_LOW         = new Color(255, 200, 80);     // < 30%
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
    public static final String EMOJI_FONT = "Segoe UI Emoji";
    public static Font emojiFont(int size) {
        return new Font(EMOJI_FONT, Font.PLAIN, size);
    }

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

    /**
     * Draws a string using the custom font for normal text
     * and the emoji font for emoji characters.
     * Returns the total width drawn.
     */
    public static int drawMixedString(Graphics2D g2, String text, int x, int y, int fontSize) {
        int currentX = x;
        Font textFont  = font(fontSize);
        Font emojFont  = emojiFont(fontSize);

        // Process character by character, grouping runs of same type
        StringBuilder buffer = new StringBuilder();
        boolean currentIsEmoji = false;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            boolean isEmoji = isEmojiCodePoint(codePoint);

            // If we switch from text→emoji or emoji→text, flush the buffer
            if (buffer.length() > 0 && isEmoji != currentIsEmoji) {
                g2.setFont(currentIsEmoji ? emojFont : textFont);
                String segment = buffer.toString();
                g2.drawString(segment, currentX, y);
                currentX += g2.getFontMetrics().stringWidth(segment);
                buffer.setLength(0);
            }

            currentIsEmoji = isEmoji;
            buffer.appendCodePoint(codePoint);
            i += Character.charCount(codePoint);
        }

        // Flush remaining buffer
        if (buffer.length() > 0) {
            g2.setFont(currentIsEmoji ? emojFont : textFont);
            String segment = buffer.toString();
            g2.drawString(segment, currentX, y);
            currentX += g2.getFontMetrics().stringWidth(segment);
        }

        return currentX - x; // Total width drawn
    }

    /**
     * Measures the total width of a mixed emoji/text string.
     */
    public static int mixedStringWidth(Graphics2D g2, String text, int fontSize) {
        int totalWidth = 0;
        Font textFont  = font(fontSize);
        Font emojFont  = emojiFont(fontSize);

        StringBuilder buffer = new StringBuilder();
        boolean currentIsEmoji = false;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            boolean isEmoji = isEmojiCodePoint(codePoint);

            if (buffer.length() > 0 && isEmoji != currentIsEmoji) {
                g2.setFont(currentIsEmoji ? emojFont : textFont);
                totalWidth += g2.getFontMetrics().stringWidth(buffer.toString());
                buffer.setLength(0);
            }

            currentIsEmoji = isEmoji;
            buffer.appendCodePoint(codePoint);
            i += Character.charCount(codePoint);
        }

        if (buffer.length() > 0) {
            g2.setFont(currentIsEmoji ? emojFont : textFont);
            totalWidth += g2.getFontMetrics().stringWidth(buffer.toString());
        }

        return totalWidth;
    }

    /**
     * Returns true if the given Unicode code point is an emoji or symbol
     * that should be rendered with the emoji font.
     */
    private static boolean isEmojiCodePoint(int codePoint) {
        // Common emoji ranges
        if (codePoint >= 0x1F600 && codePoint <= 0x1F64F) return true; // Emoticons
        if (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) return true; // Misc symbols & pictographs
        if (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) return true; // Transport & map
        if (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) return true; // Supplemental symbols
        if (codePoint >= 0x1FA00 && codePoint <= 0x1FA6F) return true; // Chess symbols
        if (codePoint >= 0x1FA70 && codePoint <= 0x1FAFF) return true; // Extended-A
        if (codePoint >= 0x2600  && codePoint <= 0x26FF)  return true; // Misc symbols
        if (codePoint >= 0x2700  && codePoint <= 0x27BF)  return true; // Dingbats
        if (codePoint >= 0xFE00  && codePoint <= 0xFE0F)  return true; // Variation selectors
        if (codePoint >= 0x200D  && codePoint <= 0x200D)  return true; // Zero-width joiner
        if (codePoint >= 0x2300  && codePoint <= 0x23FF)  return true; // Misc technical
        if (codePoint >= 0x2B50  && codePoint <= 0x2B55)  return true; // Stars
        if (codePoint == 0x2764  || codePoint == 0x2763)  return true; // Hearts
        if (codePoint >= 0x231A  && codePoint <= 0x231B)  return true; // Watch, hourglass
        if (codePoint >= 0x23E9  && codePoint <= 0x23F3)  return true; // Media controls
        if (codePoint >= 0x25AA  && codePoint <= 0x25FE)  return true; // Geometric shapes
        if (codePoint == 0x00A9  || codePoint == 0x00AE)  return true; // ©®
        if (codePoint == 0x2122)                          return true; // ™
        if (codePoint >= 0x203C  && codePoint <= 0x2049)  return true; // ‼⁉
        if (codePoint >= 0x20E3  && codePoint <= 0x20E3)  return true; // Combining enclosing keycap
        if (codePoint >= 0xE0020 && codePoint <= 0xE007F) return true; // Tags
        return false;
    }

    /**
     * Creates a JLabel that renders emojis with the emoji font
     * and regular text with the custom pixel font.
     */
    public static JLabel mixedLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(getForeground());

                g2.setFont(font(fontSize));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                drawMixedString(g2, getText(), 0, y, fontSize);
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() {
                Graphics2D g2 = (Graphics2D) getGraphics();
                if (g2 == null) return super.getPreferredSize();
                g2.setFont(font(fontSize));
                int w = mixedStringWidth(g2, getText(), fontSize);
                int h = g2.getFontMetrics().getHeight();
                return new Dimension(w + 4, h + 4);
            }
        };
        label.setForeground(color);
        return label;
    }
}