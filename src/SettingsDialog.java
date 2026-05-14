import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.*;
import java.awt.TrayIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class SettingsDialog extends JDialog {

    private final PetStats stats;
    private final Runnable onSaved;

    // Must match Theme.paintMacWindow() traffic light geometry
    private static final int TL_X0  = 12;
    private static final int TL_Y0  = 9;
    private static final int TL_DOT = 10;
    private static final int TL_GAP = 6;

    public SettingsDialog(Window owner, PetStats stats, Runnable onSaved) {
        super(owner);
        TrayNotifier.ensureInitialized();

        this.stats = stats;
        this.onSaved = onSaved;

        setModal(true);
        setSize(560, 380);
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

        root.add(buildTitlebarOverlay(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));

        body.add(buildTabs(), BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ─────────────────────────────────────────────────────────────
    // Titlebar overlay (traffic-light hitboxes + draggable)
    // ─────────────────────────────────────────────────────────────

    private JLayeredPane buildTitlebarOverlay() {
        JLayeredPane layer = new JLayeredPane() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(10, Theme.TITLEBAR_HEIGHT);
            }

            @Override public void doLayout() {
                Component red = find("tl_red");
                Component org = find("tl_orange");
                Component grn = find("tl_green");

                if (red != null) red.setBounds(TL_X0, TL_Y0, TL_DOT, TL_DOT);
                if (org != null) org.setBounds(TL_X0 + (TL_DOT + TL_GAP), TL_Y0, TL_DOT, TL_DOT);
                if (grn != null) grn.setBounds(TL_X0 + 2 * (TL_DOT + TL_GAP), TL_Y0, TL_DOT, TL_DOT);
            }

            private Component find(String name) {
                for (Component c : getComponents()) if (name.equals(c.getName())) return c;
                return null;
            }
        };
        layer.setOpaque(false);

        JButton red = makeTrafficHitbox(this::dispose, "Close");
        red.setName("tl_red");

        JButton orange = makeTrafficHitbox(this::dispose, "Close");
        orange.setName("tl_orange");

        JButton green = makeTrafficHitbox(null, null);
        green.setName("tl_green");
        green.setEnabled(false);

        layer.add(red, Integer.valueOf(2));
        layer.add(orange, Integer.valueOf(2));
        layer.add(green, Integer.valueOf(2));

        // Drag dialog by titlebar area
        Point[] offset = {null};
        layer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { offset[0] = e.getPoint(); }
        });
        layer.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - offset[0].x,
                        loc.y + e.getY() - offset[0].y);
            }
        });

        return layer;
    }

    private JButton makeTrafficHitbox(Runnable action, String tooltip) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) { /* Theme draws circles */ }
        };
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tooltip != null) b.setToolTipText(tooltip);
        if (action != null) b.addActionListener(e -> action.run());
        return b;
    }

    // ─────────────────────────────────────────────────────────────
    // Tabs
    // ─────────────────────────────────────────────────────────────

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.setBackground(Theme.BG_MAIN);
        tabs.setForeground(Theme.TEXT_PRIMARY);
        tabs.setFont(Theme.font(Theme.FONT_SIZE_LABEL));

        tabs.addTab("Edit", buildEditTab());
        tabs.addTab("How to Play", buildHowToPlayTab());
        tabs.addTab("Bed Spawn", buildBedSpawnTab());
        tabs.addTab("DEV DEBUG", buildDevDebugTab());

        // light styling; depends on LAF
        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        return tabs;
    }

    // ─────────────────────────────────────────────────────────────
    // Tab: Edit
    // ─────────────────────────────────────────────────────────────

    private JPanel buildEditTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JPanel form = new JPanel(new GridLayout(3, 2, 12, 12));
        form.setOpaque(false);

        JTextField nameField = createStyledTextField(stats.getName() == null ? "" : stats.getName());
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new LimitFilter(10));

        JComboBox<String> genderBox = createStyledComboBox(new String[]{
                "Male (he/him)", "Female (she/her)", "Non-binary (they/them)"
        });
        if (stats.getGender() != null) genderBox.setSelectedItem(stats.getGender());

        JComboBox<String> colorBox = createStyledComboBox(new String[]{
                "Default (Orange)", "Void (Black)", "Ghost (White)"
        });
        if (stats.getSpriteColor() != null) colorBox.setSelectedItem(stats.getSpriteColor());

        form.add(styledLabel("Pet Name (max 10):"));
        form.add(nameField);

        form.add(styledLabel("Gender:"));
        form.add(genderBox);

        form.add(styledLabel("Sprite Color:"));
        form.add(colorBox);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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

    // ─────────────────────────────────────────────────────────────
    // Tab: How to Play
    // ─────────────────────────────────────────────────────────────

    private JComponent buildHowToPlayTab() {
        // If you don't have GuidePanel in your project, tell me and I’ll inline the text.
        GuidePanel guide = new GuidePanel(null);
        return guide;
    }

    // ─────────────────────────────────────────────────────────────
    // Tab: Bed Spawn (reposition bed)
    // ─────────────────────────────────────────────────────────────

    private JPanel buildBedSpawnTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JLabel info = new JLabel("<html>Reposition where the bed spawns.<br/>Preview follows your mouse; click to pin; Confirm to save.</html>");
        info.setForeground(Theme.TEXT_SECONDARY);
        info.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        JLabel current = new JLabel(bedPosText());
        current.setForeground(Theme.TEXT_PRIMARY);
        current.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        JButton choose = makeButton("Choose Bed Location", () -> {
            BufferedImage preview = loadBedPreviewForColor(stats.getSpriteColor());

            // Uses your overlay (preview + confirm)
            Point topLeft = BedPlacementOverlay.pickBedTopLeft(
                    this,
                    preview,
                    BedDialog.BED_WIDTH,
                    BedDialog.BED_HEIGHT
            );

            if (topLeft != null) {
                stats.setBedPos(topLeft.x, topLeft.y);
                SaveManager.save(stats);

                TrayNotifier.showNotification("Dingus", "Bed location updated.", TrayIcon.MessageType.INFO);

                current.setText(bedPosText());
                if (onSaved != null) onSaved.run();
            }
        });
        choose.setBackground(Theme.BTN_SECONDARY);

        JButton clear = makeButton("Reset to Default", () -> {
            // easiest “reset”: set to null via reflection is messy; so set to bottom-right of full screen.
            // If you want true null reset, add a stats.clearBedPos() method and call it here.
            Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();

            int x = screen.x + screen.width  - BedDialog.BED_WIDTH;
            int y = screen.y + screen.height - BedDialog.BED_HEIGHT;

            stats.setBedPos(x, y);
            SaveManager.save(stats);

            TrayNotifier.showNotification("Dingus", "Bed reset to default.", TrayIcon.MessageType.INFO);

            current.setText(bedPosText());
            if (onSaved != null) onSaved.run();
        });
        clear.setBackground(Theme.BTN_DEFAULT);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        btns.add(choose);
        btns.add(clear);

        p.add(info, BorderLayout.NORTH);

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(current);
        mid.add(Box.createVerticalStrut(10));
        mid.add(btns);

        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    private String bedPosText() {
        if (stats.hasBedPos()) {
            return "Current bed: (" + stats.getBedX() + ", " + stats.getBedY() + ")";
        }
        return "Current bed: not set (using default)";
    }

    private BufferedImage loadBedPreviewForColor(String spriteColor) {
        try {
            String path;
            if (spriteColor == null) spriteColor = "Default (Orange)";
            path = switch (spriteColor) {
                case "Void (Black)" -> "dingus - Copy/blackbed.png";
                case "Ghost (White)" -> "dingus - Copy/whitebed.png";
                default -> "dingus - Copy/orangebed.png";
            };
            return ImageIO.read(new File(path));
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Tab: DEV DEBUG
    // ─────────────────────────────────────────────────────────────

    private JPanel buildDevDebugTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JLabel warn = new JLabel("DEV tools (changes save immediately).");
        warn.setForeground(Theme.TEXT_SECONDARY);
        warn.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        JPanel grid = new JPanel(new GridLayout(4, 2, 12, 10));
        grid.setOpaque(false);

        // Use ints; we apply by delta using addX methods so we don't care if your getters are int/double.
        JSpinner hunger   = new JSpinner(new SpinnerNumberModel((int)Math.round(stats.getHunger()), 0, 100, 1));
        JSpinner happy    = new JSpinner(new SpinnerNumberModel(stats.getHappiness(), 0, 100, 1));
        JSpinner energy   = new JSpinner(new SpinnerNumberModel(stats.getEnergy(), 0, 100, 1));
        JSpinner coins    = new JSpinner(new SpinnerNumberModel(stats.getCoins(), -999999, 999999, 1));

        styleSpinner(hunger);
        styleSpinner(happy);
        styleSpinner(energy);
        styleSpinner(coins);

        grid.add(styledLabel("Hunger:"));    grid.add(hunger);
        grid.add(styledLabel("Happiness:")); grid.add(happy);
        grid.add(styledLabel("Energy:"));    grid.add(energy);
        grid.add(styledLabel("Coins:"));     grid.add(coins);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        JButton apply = makeButton("Apply Stats", () -> {
            int hTarget = (Integer) hunger.getValue();
            int haTarget = (Integer) happy.getValue();
            int eTarget = (Integer) energy.getValue();
            int cTarget = (Integer) coins.getValue();

            // apply by delta using your existing add methods
            stats.addHunger(hTarget - stats.getHunger());
            stats.addHappiness(haTarget - stats.getHappiness());
            stats.addEnergy(eTarget - stats.getEnergy());
            stats.addCoins(cTarget - stats.getCoins());

            SaveManager.save(stats);
            if (onSaved != null) onSaved.run();

            TrayNotifier.showNotification("DEV", "Stats applied.", TrayIcon.MessageType.INFO);
        });
        apply.setBackground(Theme.BTN_PRIMARY);

        JButton notif = makeButton("Test Notification", () -> {
            TrayNotifier.showNotification("DEV", "Hello from DEV DEBUG.", TrayIcon.MessageType.INFO);
        });
        notif.setBackground(Theme.BTN_SECONDARY);

        JButton low = makeButton("Simulate Low Stats", () -> {
            stats.addHunger(-80);
            stats.addHappiness(-60);
            stats.addEnergy(-80);
            SaveManager.save(stats);
            if (onSaved != null) onSaved.run();
            TrayNotifier.showNotification("DEV", "Set low stats (warnings should trigger).", TrayIcon.MessageType.WARNING);
        });
        low.setBackground(Theme.BTN_DEFAULT);
        JButton testPoo = makeButton("Test Desktop Poop", () -> {
            // Save immediately (0 sec delay) once, then stop
            imageSaver.startRandomSaving("images/poo.png", 0, 0, (filename) -> {
                SwingUtilities.invokeLater(() -> {
                    TrayNotifier.showNotification("DEV", "Saved: " + filename, TrayIcon.MessageType.INFO);
                    imageSaver.stop();
                });
            });
        });
        testPoo.setBackground(Theme.BTN_SECONDARY);
        buttons.add(testPoo);
        buttons.add(apply);
        buttons.add(notif);
        buttons.add(low);

        p.add(warn, BorderLayout.NORTH);

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.add(grid);
        mid.add(Box.createVerticalStrut(12));
        mid.add(buttons);

        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        sp.setOpaque(true);
        sp.setBackground(Theme.BG_INPUT);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2));

        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(Theme.BG_INPUT);
            de.getTextField().setForeground(Theme.TEXT_PRIMARY);
            de.getTextField().setCaretColor(Theme.TEXT_PRIMARY);
            de.getTextField().setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Styling helpers
    // ─────────────────────────────────────────────────────────────

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
            @Override public Dimension getPreferredSize() {
                int h = 34;
                int padX = 18;

                BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                try {
                    int w = Theme.mixedStringWidth(g2, getText(), Theme.FONT_SIZE_BUTTON);
                    return new Dimension(w + padX * 2, h);
                } finally {
                    g2.dispose();
                }
            }

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

                int textW = Theme.mixedStringWidth(g2, getText(), Theme.FONT_SIZE_BUTTON);
                FontMetrics fm = g2.getFontMetrics(Theme.font(Theme.FONT_SIZE_BUTTON));
                int x = (getWidth() - textW) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);
                Theme.drawMixedString(g2, getText(), x, y, Theme.FONT_SIZE_BUTTON);

                g2.dispose();
            }
        };

        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }

    // ─────────────────────────────────────────────────────────────
    // Name limit
    // ─────────────────────────────────────────────────────────────

    private static class LimitFilter extends DocumentFilter {
        private final int max;
        LimitFilter(int max) { this.max = max; }

        @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            if (fb.getDocument().getLength() + string.length() <= max) super.insertString(fb, offset, string, attr);
        }

        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;
            int cur = fb.getDocument().getLength();
            int next = cur - length + text.length();
            if (next <= max) super.replace(fb, offset, length, text, attrs);
        }
    }
}