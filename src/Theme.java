import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Theme {

    public static final Color BG_MAIN              = new Color(0xF4,0xEA,0xD6); // cream
    public static final Color BG_MAIN_TRANSPARENT  = new Color(0xF4,0xEA,0xD6, 235);
    public static final Color BG_INPUT             = new Color(0xEA,0xDD,0xC2); // darker cream
    public static final Color BG_INPUT_BORDER      = new Color(0x1F,0x1B,0x16); // ink

    public static final Color BG_DROPDOWN_ITEM     = new Color(0xF4,0xEA,0xD6);
    public static final Color BG_DROPDOWN_SELECTED = new Color(0xC9,0xE3,0xE7); // pale blue selection

    // Buttons (boxed, ink outline)
    public static final Color BTN_ACCENT     = BG_INPUT_BORDER;                 // ink outline
    public static final Color BTN_DEFAULT    = new Color(0xEA,0xDD,0xC2);
    public static final Color BTN_HOVER      = new Color(0xF0,0xE4,0xCB);
    public static final Color BTN_PRESSED    = new Color(0xDE,0xD0,0xB6);
    public static final Color BTN_DISABLED   = new Color(0xE6,0xDB,0xC7);

    public static final Color BTN_PRIMARY    = new Color(0xB9,0xE0,0xB3);       // soft green
    public static final Color BTN_SECONDARY  = new Color(0xD2,0xEC,0xF0);       // soft blue
    public static final Color BTN_CLOSE      = new Color(0xFF,0x5F,0x57);       // mac red

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  TEXT COLORS  ██████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color TEXT_PRIMARY   = new Color(0x1F,0x1B,0x16);
    public static final Color TEXT_SECONDARY = new Color(0x3A,0x33,0x2C);
    public static final Color TEXT_DISABLED  = new Color(0x88,0x80,0x75);
    public static final Color TEXT_LABEL     = TEXT_PRIMARY;

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  ACCENT COLORS  ███████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color ACCENT_COINS   = new Color(0xC7,0x8B,0x2E);
    public static final Color ACCENT_RAM           = new Color(150, 255, 150);
    public static final Color ACCENT_CPU           = new Color(150, 200, 255);
    public static final Color ACCENT_SUCCESS       = new Color(100, 200, 100);
    public static final Color ACCENT_ERROR         = new Color(255, 100, 100);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  PROGRESS BARS  ███████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Color PROGRESS_TRACK = BG_INPUT;
    public static final Color PROGRESS_HIGH  = new Color(0x9F,0xD3,0x9A);
    public static final Color PROGRESS_MED   = new Color(0xF2,0xD0,0x74);
    public static final Color PROGRESS_LOW   = new Color(0xE0,0x7A,0x5F);
    public static final Color PROGRESS_RAM   = new Color(0xB6,0xE3,0xD0);
    public static final Color PROGRESS_CPU   = new Color(0xB8,0xCF,0xF0);

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

    public static final Color SCROLLBAR_THUMB = new Color(0x1F,0x1B,0x16, 120);
    public static final Color SCROLLBAR_TRACK = new Color(0,0,0,0);

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  FONT CONFIG  █████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final String FONT_PATH           = "images/Atop.ttf";
    public static final String FONT_FALLBACK       = "Arial";
    public static final String EMOJI_FONT          = "Segoe UI Emoji";

    public static final int FONT_SIZE_TITLE        = 23;
    public static final int FONT_SIZE_HEADING      = 18;
    public static final int FONT_SIZE_BUTTON       = 14;
    public static final int FONT_SIZE_LABEL        = 13;
    public static final int FONT_SIZE_BODY         = 14;
    public static final int FONT_SIZE_SMALL        = 11;

    public static final int OUTLINE_THICKNESS = 2;
    public static final int TITLEBAR_HEIGHT   = 26;

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  SIZING  ██████████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static final int CORNER_RADIUS        = 10;
    public static final int BUTTON_CORNER_RADIUS = 8;

    // ─── PetMenu Specific ──────────────────────────────────────────────────────
    public static final int MENU_WIDTH             = 150;
    public static final int MENU_SCROLL_HEIGHT     = 230;
    public static final int SCROLLBAR_WIDTH        = 3;

    // ─── SetupWizard Specific ──────────────────────────────────────────────────
    public static final int WIZARD_WIDTH           = 500;
    public static final int WIZARD_HEIGHT          = 400;

    // ─── Timing ────────────────────────────────────────────────────────────────
    public static final int SCAN_DURATION_SECONDS  = 15;

    public static void paintMacWindow(Graphics2D g2, int w, int h, String title) {
        // Keep text AA on since you said “keep font for now”
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // subtle shadow
        g2.setColor(new Color(0,0,0,35));
        g2.fillRoundRect(3,4, w-5, h-5, CORNER_RADIUS, CORNER_RADIUS);

        // body
        g2.setColor(BG_MAIN);
        g2.fillRoundRect(0,0, w-6, h-6, CORNER_RADIUS, CORNER_RADIUS);

        // outline + titlebar divider
        g2.setColor(BG_INPUT_BORDER);
        g2.setStroke(new BasicStroke(OUTLINE_THICKNESS));
        g2.drawRoundRect(0,0, w-7, h-7, CORNER_RADIUS, CORNER_RADIUS);
        g2.drawLine(0, TITLEBAR_HEIGHT, w-7, TITLEBAR_HEIGHT);

        // traffic lights (left)
        int x = 12, y = 9, d = 10, gap = 6;
        drawDot(g2, x, y, d, new Color(0xFF,0x5F,0x57));
        drawDot(g2, x + (d+gap), y, d, new Color(0xFE,0xBC,0x2E));
        drawDot(g2, x + 2*(d+gap), y, d, new Color(0x28,0xC8,0x40));

        // centered title
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "DINGUS");
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(
                Theme.TITLEBAR_HEIGHT + 10, 12, 12, 12
        ));
    }

    public static String ellipsizeMixed(Graphics2D g2, String s, int maxWidth, int fontSize) {
        if (s == null) return "";
        if (mixedStringWidth(g2, s, fontSize) <= maxWidth) return s;

        final String ell = "…";
        String base = s;

        // Remove codepoints from the end until it fits with ellipsis
        while (!base.isEmpty()) {
            int end = base.offsetByCodePoints(base.length(), -1);
            base = base.substring(0, end);
            String candidate = base + ell;
            if (mixedStringWidth(g2, candidate, fontSize) <= maxWidth) return candidate;
        }
        return ell;
    }

    private static void drawDot(Graphics2D g2, int x, int y, int d, Color fill) {
        g2.setColor(fill);
        g2.fillOval(x, y, d, d);
        g2.setColor(BG_INPUT_BORDER);
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(x, y, d, d);
    }

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

    public static Font emojiFont(int size) {
        return new Font(EMOJI_FONT, Font.PLAIN, size);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  HELPERS  ██████████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static Color progressColor(int value) {
        if (value >= 70) return PROGRESS_HIGH;
        else if (value >= 30) return PROGRESS_MED;
        else return PROGRESS_LOW;
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
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  MIXED TEXT RENDERING  █████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static int drawMixedString(Graphics2D g2, String text, int x, int y, int fontSize) {
        int currentX = x;
        Font textFont = font(fontSize);
        Font emojFont = emojiFont(fontSize);

        StringBuilder buffer = new StringBuilder();
        boolean currentIsEmoji = false;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            boolean isEmoji = isEmojiCodePoint(codePoint);

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

        if (buffer.length() > 0) {
            g2.setFont(currentIsEmoji ? emojFont : textFont);
            String segment = buffer.toString();
            g2.drawString(segment, currentX, y);
            currentX += g2.getFontMetrics().stringWidth(segment);
        }

        return currentX - x;
    }

    public static int mixedStringWidth(Graphics2D g2, String text, int fontSize) {
        int totalWidth = 0;
        Font textFont = font(fontSize);
        Font emojFont = emojiFont(fontSize);

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

    private static boolean isEmojiCodePoint(int codePoint) {
        if (codePoint >= 0x1F600 && codePoint <= 0x1F64F) return true;
        if (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) return true;
        if (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) return true;
        if (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) return true;
        if (codePoint >= 0x1FA00 && codePoint <= 0x1FA6F) return true;
        if (codePoint >= 0x1FA70 && codePoint <= 0x1FAFF) return true;
        if (codePoint >= 0x2600  && codePoint <= 0x26FF)  return true;
        if (codePoint >= 0x2700  && codePoint <= 0x27BF)  return true;
        if (codePoint >= 0xFE00  && codePoint <= 0xFE0F)  return true;
        if (codePoint == 0x200D)                          return true;
        if (codePoint >= 0x2300  && codePoint <= 0x23FF)  return true;
        if (codePoint >= 0x2B50  && codePoint <= 0x2B55)  return true;
        if (codePoint == 0x2764  || codePoint == 0x2763)  return true;
        if (codePoint >= 0x231A  && codePoint <= 0x231B)  return true;
        if (codePoint >= 0x23E9  && codePoint <= 0x23F3)  return true;
        if (codePoint >= 0x25AA  && codePoint <= 0x25FE)  return true;
        if (codePoint == 0x00A9  || codePoint == 0x00AE)  return true;
        if (codePoint == 0x2122)                          return true;
        if (codePoint >= 0x203C  && codePoint <= 0x2049)  return true;
        if (codePoint == 0x20E3)                          return true;
        if (codePoint >= 0xE0020 && codePoint <= 0xE007F) return true;
        return false;
    }

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

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  SCREEN HELPERS  ██████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    public static GraphicsConfiguration getPrimaryScreenConfig() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    /**
     * Returns the usable desktop area, excluding taskbar/docked bars.
     * This is the correct "working area" to position your pet in.
     */
    public static Rectangle getUsableScreen() {
        GraphicsConfiguration gc = getPrimaryScreenConfig();
        Rectangle bounds = gc.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        return new Rectangle(
                bounds.x + insets.left,
                bounds.y + insets.top,
                bounds.width  - insets.left - insets.right,
                bounds.height - insets.top  - insets.bottom
        );
    }

    /**
     * Returns the actual screen insets for the primary screen.
     */
    public static Insets getScreenInsets() {
        return Toolkit.getDefaultToolkit().getScreenInsets(getPrimaryScreenConfig());
    }

    /**
     * Returns DPI scale using the graphics transform.
     */
    public static double getDpiScale() {
        return getPrimaryScreenConfig().getDefaultTransform().getScaleX();
    }

    /**
     * Scales a logical pixel value by current DPI.
     */
    public static int scaled(int pixels) {
        return (int) Math.round(pixels * getDpiScale());
    }

    public static int getScalePercent() {
        return (int) Math.round(getDpiScale() * 100);
    }
}