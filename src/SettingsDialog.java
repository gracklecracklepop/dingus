import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class SettingsDialog extends JDialog {

    private final PetStats stats;
    private final Runnable onSaved;

    public SettingsDialog(Window owner, PetStats stats, Runnable onSaved) {
        super(owner);
        this.stats = stats;
        this.onSaved = onSaved;

        setModal(true);
        setSize(520, 300);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "SETTINGS");
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

        // form
        JPanel form = new JPanel(new GridLayout(3, 2, 12, 12));
        form.setOpaque(false);

        JTextField nameField = createStyledTextField(stats.getName() == null ? "" : stats.getName());

        JComboBox<String> genderBox = createStyledComboBox(new String[]{
                "Male (he/him)", "Female (she/her)", "Non-binary (they/them)"
        });
        if (stats.getGender() != null) genderBox.setSelectedItem(stats.getGender());

        JComboBox<String> colorBox = createStyledComboBox(new String[]{
                "Default (Orange)", "Void (Black)", "Ghost (White)"
        });
        if (stats.getSpriteColor() != null) colorBox.setSelectedItem(stats.getSpriteColor());

        form.add(styledLabel("Pet Name:"));
        form.add(nameField);

        form.add(styledLabel("Gender:"));
        form.add(genderBox);

        form.add(styledLabel("Sprite Color:"));
        form.add(colorBox);

        // buttons
        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);

        JButton close = makeButton("Close", this::dispose);
        close.setBackground(Theme.BTN_SECONDARY);

        JButton save = makeButton("Save", () -> {
            stats.setName(nameField.getText());
            stats.setGender((String) genderBox.getSelectedItem());
            stats.setSpriteColor((String) colorBox.getSelectedItem());
            SaveManager.save(stats);

            if (onSaved != null) onSaved.run();
            dispose();
        });
        save.setBackground(Theme.BTN_PRIMARY);

        buttons.add(close);
        buttons.add(save);

        p.add(form, BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    // ── Styling helpers ─────────────────────────────────────────

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_LABEL);
        l.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        return l;
    }

    private JTextField createStyledTextField(String defaultText) {
        JTextField field = new JTextField(defaultText) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.BG_INPUT);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        field.setOpaque(true);
        field.setBackground(Theme.BG_INPUT);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        field.setSelectionColor(Theme.BG_DROPDOWN_SELECTED);
        field.setSelectedTextColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setOpaque(true);
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        cb.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2));

        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("▾") {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(Theme.BG_INPUT);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Theme.BG_INPUT_BORDER);
                        ((Graphics2D) g).setStroke(new BasicStroke(2));
                        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                        g.setColor(Theme.TEXT_PRIMARY);
                        g.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                        FontMetrics fm = g.getFontMetrics();
                        g.drawString("▾",
                                (getWidth() - fm.stringWidth("▾")) / 2,
                                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    }
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                return btn;
            }
        });

        cb.setEditor(new BasicComboBoxEditor() {
            @Override protected JTextField createEditorComponent() {
                JTextField editor = new JTextField();
                editor.setOpaque(true);
                editor.setBackground(Theme.BG_INPUT);
                editor.setForeground(Theme.TEXT_PRIMARY);
                editor.setCaretColor(Theme.TEXT_PRIMARY);
                editor.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                editor.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                return editor;
            }
        });

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                label.setOpaque(true);

                if (index == -1) {
                    label.setBackground(Theme.BG_INPUT);
                    label.setForeground(Theme.TEXT_PRIMARY);
                } else if (isSelected) {
                    label.setBackground(Theme.BG_DROPDOWN_SELECTED);
                    label.setForeground(Theme.TEXT_PRIMARY);
                } else {
                    label.setBackground(Theme.BG_DROPDOWN_ITEM);
                    label.setForeground(Theme.TEXT_PRIMARY);
                }
                return label;
            }
        });

        return cb;
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
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

        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }
}