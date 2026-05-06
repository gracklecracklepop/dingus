import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {

    private final PetStats stats;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // Force theme colors before any components are created
    static {
        Theme.applyUIManagerDefaults();
    }

    public SettingsDialog(PetStats stats) {
        this.stats = stats;

        setTitle("Settings");
        setModal(true);
        setSize(Theme.WIZARD_WIDTH, Theme.WIZARD_HEIGHT);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "SETTINGS");
                g2.dispose();
            }

        };
        mainContainer.setBorder(BorderFactory.createEmptyBorder(
                Theme.TITLEBAR_HEIGHT + 12, 16, 16, 16
        ));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainContainer.add(buildHeader(), BorderLayout.NORTH);

        cardPanel.setOpaque(false);
        cardPanel.add(buildMainSettingsCard(), "main");
        cardPanel.add(buildEditDingusCard(),   "edit");
        cardPanel.add(new GuidePanel(() -> cardLayout.show(cardPanel, "main")), "guide");

        mainContainer.add(cardPanel, BorderLayout.CENTER);
        add(mainContainer);
        cardLayout.show(cardPanel, "main");
    }

    // ── Header ──────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("⚙️ Settings");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = makeButton("X", this::dispose);
        closeBtn.setBackground(Theme.BTN_CLOSE);
        closeBtn.setPreferredSize(new Dimension(40, 30));
        header.add(closeBtn, BorderLayout.EAST);

        Point[] offset = {null};
        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { offset[0] = e.getPoint(); }
        });
        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - offset[0].x, loc.y + e.getY() - offset[0].y);
            }
        });

        return header;
    }

    // ── Main Settings Card ──────────────────────────────────────

    private JPanel buildMainSettingsCard() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("What would you like to do?", SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        p.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setOpaque(false);

        JButton editBtn = makeButton("✏️ Edit My Dingus", () ->
                cardLayout.show(cardPanel, "edit"));
        editBtn.setPreferredSize(new Dimension(220, 45));
        editBtn.setMaximumSize(new Dimension(220, 45));
        editBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        editBtn.setBackground(Theme.BTN_PRIMARY);

        JButton guideBtn = makeButton("📖 How to Play", () ->
                cardLayout.show(cardPanel, "guide"));
        guideBtn.setPreferredSize(new Dimension(220, 45));
        guideBtn.setMaximumSize(new Dimension(220, 45));
        guideBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        guideBtn.setBackground(Theme.BTN_SECONDARY);

        JButton closeBtn = makeButton("✖ Close", this::dispose);
        closeBtn.setPreferredSize(new Dimension(220, 45));
        closeBtn.setMaximumSize(new Dimension(220, 45));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setBackground(Theme.BTN_CLOSE);

        buttons.add(Box.createVerticalStrut(20));
        buttons.add(editBtn);
        buttons.add(Box.createVerticalStrut(15));
        buttons.add(guideBtn);
        buttons.add(Box.createVerticalStrut(15));
        buttons.add(closeBtn);

        p.add(buttons, BorderLayout.CENTER);
        return p;
    }

    // ── Edit Dingus Card ────────────────────────────────────────

    private JPanel buildEditDingusCard() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("✏️ Edit My Dingus", SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        p.add(title, BorderLayout.NORTH);

        JTextField nameField = createStyledTextField(stats.getName());

        JComboBox<String> genderBox = createStyledComboBox(new String[]{
                "Male (he/him)", "Female (she/her)", "Non-binary (they/them)"
        });
        for (int i = 0; i < genderBox.getItemCount(); i++) {
            if (genderBox.getItemAt(i).equals(stats.getGender())) {
                genderBox.setSelectedIndex(i); break;
            }
        }

        JComboBox<String> colorBox = createStyledComboBox(new String[]{
                "Default (Orange)", "Void (Black)", "Ghost (White)"
        });
        for (int i = 0; i < colorBox.getItemCount(); i++) {
            if (colorBox.getItemAt(i).equals(stats.getSpriteColor())) {
                colorBox.setSelectedIndex(i); break;
            }
        }

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setOpaque(false);

        form.add(styledLabel("Pet Name:"));     form.add(nameField);
        form.add(styledLabel("Gender:"));       form.add(genderBox);
        form.add(styledLabel("Sprite Color:")); form.add(colorBox);
        form.add(new JLabel());                 form.add(new JLabel());

        JButton backBtn = makeButton("⬅ Back", () -> cardLayout.show(cardPanel, "main"));
        backBtn.setBackground(Theme.BTN_SECONDARY);

        JButton saveBtn = makeButton("💾 Save", null);
        saveBtn.setBackground(Theme.BTN_PRIMARY);

        saveBtn.addActionListener(e -> {
            stats.setName(nameField.getText());
            stats.setGender((String) genderBox.getSelectedItem());
            stats.setSpriteColor((String) colorBox.getSelectedItem());
            SaveManager.save(stats);

            saveBtn.setText("✓ Saved!");
            saveBtn.setBackground(Theme.ACCENT_SUCCESS);
            new Timer(1500, evt -> {
                saveBtn.setText("💾 Save");
                saveBtn.setBackground(Theme.BTN_PRIMARY);
            }) {{ setRepeats(false); start(); }};
        });

        form.add(backBtn);
        form.add(saveBtn);

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    // ── Styling Helpers ─────────────────────────────────────────

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

//        cb.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setOpaque(true);
        cb.setBackground(Theme.BG_INPUT);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        cb.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 1));

        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("▼") {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(Theme.BG_INPUT_BORDER);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Theme.TEXT_PRIMARY);
                        FontMetrics fm = g.getFontMetrics();
                        g.drawString("▼", (getWidth() - fm.stringWidth("▼")) / 2,
                                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    }
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                return btn;
            }
            @Override protected void installDefaults() {
                super.installDefaults();
                LookAndFeel.installProperty(comboBox, "opaque", true);
            }
        });

        cb.setEditor(new BasicComboBoxEditor() {
            @Override protected JTextField createEditorComponent() {
                JTextField editor = new JTextField() {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(Theme.BG_INPUT);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                editor.setOpaque(true);
                editor.setBackground(Theme.BG_INPUT);
                editor.setForeground(Theme.TEXT_PRIMARY);
                editor.setCaretColor(Theme.TEXT_PRIMARY);
                editor.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                editor.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
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
                if (index == -1)         { label.setBackground(Theme.BG_INPUT);             label.setForeground(Theme.TEXT_PRIMARY); }
                else if (isSelected)     { label.setBackground(Theme.BG_DROPDOWN_SELECTED); label.setForeground(Theme.TEXT_PRIMARY); }
                else                     { label.setBackground(Theme.BG_DROPDOWN_ITEM);     label.setForeground(Theme.TEXT_PRIMARY); }
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
                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = bg.darker();
                else if (getModel().isRollover()) bg = bg.brighter();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);
                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);
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
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }
}