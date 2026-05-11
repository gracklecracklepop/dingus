import javax.swing.*;
import java.awt.*;

public class SleepDurationDialog extends JDialog {

    private Integer minutes = null;

    public SleepDurationDialog(Window owner) {
        super(owner);
        setModal(true);
        setUndecorated(true);
        setType(Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));
        setSize(420, 220);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "Sleep");
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(
                Theme.TITLEBAR_HEIGHT + 12, 16, 16, 16
        ));

        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JLabel msg = new JLabel("How long should Dingus sleep?");
        msg.setForeground(Theme.TEXT_PRIMARY);
        msg.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        p.add(msg, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(10, 0));
        mid.setOpaque(false);

        JLabel left = new JLabel("1 min");
        left.setForeground(Theme.TEXT_SECONDARY);
        left.setFont(Theme.font(Theme.FONT_SIZE_SMALL));

        JLabel right = new JLabel("180 min");
        right.setForeground(Theme.TEXT_SECONDARY);
        right.setFont(Theme.font(Theme.FONT_SIZE_SMALL));

        JSlider slider = new JSlider(1, 180, 15);
        slider.setOpaque(false);

        JLabel value = new JLabel(slider.getValue() + " minutes", SwingConstants.CENTER);
        value.setForeground(Theme.TEXT_PRIMARY);
        value.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        slider.addChangeListener(e -> value.setText(slider.getValue() + " minutes"));

        mid.add(left, BorderLayout.WEST);
        mid.add(slider, BorderLayout.CENTER);
        mid.add(right, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(value, BorderLayout.NORTH);
        center.add(mid, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton cancel = themedButton("Cancel", Theme.BTN_SECONDARY, () -> { minutes = null; dispose(); });
        JButton ok     = themedButton("Sleep",   Theme.BTN_PRIMARY,   () -> { minutes = slider.getValue(); dispose(); });

        btns.add(cancel);
        btns.add(ok);
        p.add(btns, BorderLayout.SOUTH);

        return p;
    }

    private JButton themedButton(String text, Color bg, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color fill = getBackground();
                if (!isEnabled()) fill = Theme.BTN_DISABLED;
                else if (getModel().isPressed()) fill = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) fill = Theme.BTN_HOVER;

                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);

                g2.dispose();
            }
        };

        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 34));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    public static Integer promptMinutes(Window owner) {
        SleepDurationDialog d = new SleepDurationDialog(owner);
        d.setVisible(true);
        return d.minutes;
    }
}