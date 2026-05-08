import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class TextLimitDocumentFilter extends DocumentFilter {
    private final int maxChars;

    public TextLimitDocumentFilter(int maxChars) {
        this.maxChars = maxChars;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string == null) return;
        String filtered = filter(string);
        int newLen = fb.getDocument().getLength() + filtered.length();
        if (newLen <= maxChars) super.insertString(fb, offset, filtered, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text == null) text = "";
        String filtered = filter(text);
        int curLen = fb.getDocument().getLength();
        int newLen = curLen - length + filtered.length();
        if (newLen <= maxChars) super.replace(fb, offset, length, filtered, attrs);
        else {
            // truncate to fit
            int allowed = Math.max(0, maxChars - (curLen - length));
            if (allowed > 0) super.replace(fb, offset, length, filtered.substring(0, allowed), attrs);
        }
    }

    private String filter(String s) {
        // “10 letters” – keep it simple: allow letters/numbers/space/_/-
        return s.replaceAll("[^A-Za-z0-9 _-]", "");
    }
}