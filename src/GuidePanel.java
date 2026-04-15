import javax.swing.*;
import java.awt.*;

public class GuidePanel extends JPanel {

    public GuidePanel(Runnable onBack) {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel title = new JLabel("📖 How to Play", SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        add(title, BorderLayout.NORTH);

        // Scrollable guide content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        addSection(content, "🐱 Your Pet",
                "Your Dingus is a digital companion that lives on your desktop. " +
                        "Keep it happy, fed, and rested to earn coins and unlock accessories!");

        addSection(content, "🍖 Feeding",
                "Click Feed to restore Hunger (+20) and Happiness (+5).\n" +
                        "You earn 5 coins per feed.\n" +
                        "⏱ Feed has a 30-minute cooldown — plan ahead!");

        addSection(content, "🎾 Playing",
                "Click Play to boost Happiness (+20) and earn 5 coins.\n" +
                        "Playing costs Energy (-10) and Hunger (-10).\n" +
                        "It also opens a random image of grass for your cat to look at.");

        addSection(content, "😴 Sleeping",
                "Drag your pet onto the bed in the bottom-right corner to put it to sleep.\n" +
                        "Sleeping restores Energy over time.\n" +
                        "The bed sprite disappears when your pet is resting in it.");

        addSection(content, "🪙 Coins",
                "Earn coins by feeding and playing with your pet.\n" +
                        "Spend coins in the Shop to buy accessories for your Dingus.");

        addSection(content, "🛒 Shop",
                "Open the Shop from the menu to browse accessories.\n" +
                        "Each item has a one-time cost in coins.\n" +
                        "Owned items are saved permanently and persist between sessions.");

        addSection(content, "📊 Stats",
                "Hunger, Happiness, and Energy each range from 0 to 100.\n" +
                        "Green = healthy (70+), Yellow = okay (30-69), Red = critical (<30).\n" +
                        "Try to keep all three bars green!");

        addSection(content, "💾 Saving",
                "Everything is saved automatically after every action.\n" +
                        "Your save file lives at: ~/.dingus/save_data.json\n" +
                        "You can edit it manually if you need to.");

        addSection(content, "⚙️ Settings",
                "Use Settings to re-edit your pet's name, gender, and colour.\n" +
                        "You can also re-run the RAM and CPU baseline scans from here.");

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 0));
        add(scroll, BorderLayout.CENTER);

        // Back button
        if (onBack != null) {
            JButton backBtn = makeButton("⬅ Back", onBack);
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setOpaque(false);
            footer.add(backBtn);
            add(footer, BorderLayout.SOUTH);
        }
    }

    private void addSection(JPanel parent, String heading, String body) {
        // Section heading
        JLabel headLabel = new JLabel(heading);
        headLabel.setForeground(Theme.ACCENT_SUCCESS);
        headLabel.setFont(Theme.font(Theme.FONT_SIZE_LABEL + 1));
        headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        parent.add(headLabel);

        // Section body
        JTextArea bodyArea = new JTextArea(body);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setLineWrap(true);
        bodyArea.setOpaque(false);
        bodyArea.setEditable(false);
        bodyArea.setFocusable(false);
        bodyArea.setForeground(Theme.TEXT_SECONDARY);
        bodyArea.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        bodyArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        parent.add(bodyArea);

        // Divider line
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 80, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        parent.add(sep);
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = bg.darker();
                else if (getModel().isRollover()) bg = bg.brighter();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setBackground(Theme.BTN_DEFAULT);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}