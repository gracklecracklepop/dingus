import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AccessoryCustomizerDialog extends JDialog {

    private final PetStats stats;
    private final Runnable onChanged;
    private final Window petWindow; // the actual Dingus window we overlay

    private static final Color[] PALETTE = {
            new Color(0x1F1B16), new Color(0xC78B2E), new Color(0xE07A5F), new Color(0x7AB9C5),
            new Color(0x9FD39A), new Color(0xF2D074), new Color(0x6B5CA5), new Color(0xFFFFFF)
    };

    public AccessoryCustomizerDialog(Window ownerDialog, Window petWindow, PetStats stats, Runnable onChanged) {
        super(ownerDialog);
        this.stats = stats;
        this.onChanged = onChanged;
        this.petWindow = petWindow;

        setModal(true);
        setUndecorated(true);
        setType(Type.UTILITY);
        setBackground(new Color(0,0,0,0));
        setSize(560, 380);
        setLocationRelativeTo(ownerDialog);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "CUSTOMIZE");
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(Theme.TITLEBAR_HEIGHT + 10, 14, 14, 14));

        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JComponent buildContent() {
        JPanel main = new JPanel(new BorderLayout(12, 0));
        main.setOpaque(false);

        main.add(buildHatList(), BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        right.add(buildSpawnPositionPanel());
        right.add(Box.createVerticalStrut(10));
        right.add(buildRgbColorPanel());
        right.add(Box.createVerticalGlue());
        right.add(buildFooter());

        main.add(right, BorderLayout.CENTER);
        return main;
    }

    private JComponent buildHatList() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(200, 10));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Hats (owned)");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        left.add(title);
        left.add(Box.createVerticalStrut(8));

        left.add(themedButton("None", Theme.BTN_DEFAULT, () -> {
            stats.setEquippedHatId(null);
            SaveManager.save(stats);
            changed();
        }));
        left.add(Box.createVerticalStrut(6));

        List<StoreItem> hats = AccessoryCatalog.ownedHats(stats);
        for (StoreItem it : hats) {
            left.add(themedButton(it.glyph + "  " + it.name, Theme.BTN_DEFAULT, () -> {
                stats.setEquippedHatId(it.id);
                SaveManager.save(stats);
                changed();
            }));
            left.add(Box.createVerticalStrut(6));
        }

        JScrollPane scroll = new JScrollPane(left);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 0));

        return scroll;
    }

    // ─────────────────────────────────────────────────────────────
    // THIS is where your buildSpawnPositionPanel goes (inside this class)
    // ─────────────────────────────────────────────────────────────

    private JPanel buildSpawnPositionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        JComboBox<AccessoryPlacementOverlay.Pose> poseBox =
                new JComboBox<>(AccessoryPlacementOverlay.Pose.values());
        poseBox.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        poseBox.setBackground(Theme.BG_INPUT);
        poseBox.setForeground(Theme.TEXT_PRIMARY);
        poseBox.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2));

        JLabel sizeLabel = new JLabel("Size: 100%");
        sizeLabel.setForeground(Theme.TEXT_PRIMARY);
        sizeLabel.setFont(Theme.font(Theme.FONT_SIZE_LABEL));

        JSlider sizeSlider = new JSlider(50, 200, 100); // 50%..200%
        sizeSlider.setOpaque(false);
        sizeSlider.setFocusable(false);

// when pose changes, load its saved size into slider
        poseBox.addActionListener(e -> {
            var pose = (AccessoryPlacementOverlay.Pose) poseBox.getSelectedItem();
            int pct = (int) Math.round(getScaleForPose(pose) * 100.0);
            pct = Math.max(50, Math.min(200, pct));
            sizeSlider.setValue(pct);
            sizeLabel.setText("Size: " + pct + "%");
        });

// when slider changes, save size for current pose
        sizeSlider.addChangeListener(e -> {
            var pose = (AccessoryPlacementOverlay.Pose) poseBox.getSelectedItem();
            int pct = sizeSlider.getValue();
            sizeLabel.setText("Size: " + pct + "%");

            double scale = pct / 100.0;
            setScaleForPose(pose, scale);

            // save only when user releases (avoids spam)
            if (!sizeSlider.getValueIsAdjusting()) {
                SaveManager.save(stats);
                if (onChanged != null) onChanged.run();
            }
        });

        JButton setPos = themedButton("Set Hat Position", Theme.BTN_SECONDARY, () -> {
            StoreItem it = AccessoryCatalog.byId(stats.getEquippedHatId());
            if (it == null || it.glyph == null) {
                TrayNotifier.showNotification("Dingus", "Equip a hat first.", TrayIcon.MessageType.INFO);
                return;
            }
            if (petWindow == null) {
                TrayNotifier.showNotification("Dingus", "Pet window not found.", TrayIcon.MessageType.ERROR);
                return;
            }

            Color fill = new Color(stats.getHatColorRGB(), true);
            Color outline = Theme.BG_INPUT_BORDER;

            var pose = (AccessoryPlacementOverlay.Pose) poseBox.getSelectedItem();

            double[] res = AccessoryPlacementOverlay.pick(
                    this,                 // owner dialog
                    petWindow,            // pet window to overlay
                    stats.getSpriteColor(),
                    it.glyph,
                    fill,
                    outline,
                    pose
            );
            if (res == null) return;

            switch (pose) {
                case SITTING -> { stats.setHeadNormalXFrac(res[0]); stats.setHeadNormalYFrac(res[1]); }
                case BED     -> { stats.setHeadBedXFrac(res[0]);    stats.setHeadBedYFrac(res[1]); }
                case DRAG    -> { stats.setHeadDragXFrac(res[0]);   stats.setHeadDragYFrac(res[1]); }
            }

            SaveManager.save(stats);
            changed();
        });

        JLabel lbl = new JLabel("Pose:");
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setFont(Theme.font(Theme.FONT_SIZE_LABEL));

        p.add(lbl);
        p.add(poseBox);
        p.add(setPos);

        return p;
    }

    private JComponent buildRgbColorPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);

        JLabel title = new JLabel("Hat Color (RGB)");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        wrap.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 1, 0, 8));
        grid.setOpaque(false);

        // current color from stats (ARGB)
        Color cur = new Color(stats.getHatColorRGB(), true);

        JSlider r = makeRgbSlider(cur.getRed());
        JSlider g = makeRgbSlider(cur.getGreen());
        JSlider b = makeRgbSlider(cur.getBlue());

        JLabel hex = new JLabel("", SwingConstants.RIGHT);
        hex.setForeground(Theme.TEXT_SECONDARY);
        hex.setFont(Theme.font(Theme.FONT_SIZE_SMALL));

        JPanel preview = new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g2 = (Graphics2D) g0.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c = new Color(
                        r.getValue(),
                        g.getValue(),
                        b.getValue()
                );

                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-2, getHeight()-2, 10, 10);

                g2.dispose();
            }
        };
        preview.setOpaque(false);
        preview.setPreferredSize(new Dimension(80, 18));

        Runnable applyLive = () -> {
            int rgb = (0xFF << 24)
                    | (r.getValue() << 16)
                    | (g.getValue() << 8)
                    | (b.getValue());
            stats.setHatColorRGB(rgb);

            hex.setText(String.format("#%02X%02X%02X", r.getValue(), g.getValue(), b.getValue()));
            preview.repaint();

            // update pet immediately
            if (onChanged != null) onChanged.run();
        };

        // Apply live while dragging; only SAVE when the user releases slider
        r.addChangeListener(e -> {
            applyLive.run();
            if (!r.getValueIsAdjusting()) SaveManager.save(stats);
        });
        g.addChangeListener(e -> {
            applyLive.run();
            if (!g.getValueIsAdjusting()) SaveManager.save(stats);
        });
        b.addChangeListener(e -> {
            applyLive.run();
            if (!b.getValueIsAdjusting()) SaveManager.save(stats);
        });

        // initialize text/preview
        applyLive.run();

        grid.add(rgbRow("Red", r));
        grid.add(rgbRow("Green", g));
        grid.add(rgbRow("Blue", b));

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);
        bottom.add(preview, BorderLayout.WEST);
        bottom.add(hex, BorderLayout.CENTER);

        wrap.add(grid, BorderLayout.CENTER);
        wrap.add(bottom, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel rgbRow(String label, JSlider slider) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(Theme.TEXT_PRIMARY);
        l.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        l.setPreferredSize(new Dimension(18, 10));

        row.add(l, BorderLayout.WEST);
        row.add(slider, BorderLayout.CENTER);
        return row;
    }

    private JSlider makeRgbSlider(int value) {
        JSlider s = new JSlider(0, 255, value);
        s.setOpaque(false);
        s.setFocusable(false);
        return s;
    }

    private JComponent buildColorRow() {
        JPanel colors = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        colors.setOpaque(false);

        JLabel lbl = new JLabel("Color:");
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        colors.add(lbl);

        for (Color c : PALETTE) {
            colors.add(colorDot(c));
        }
        return colors;
    }

    private JComponent colorDot(Color c) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(0, 0, getWidth()-2, getHeight()-2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(18,18));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            stats.setHatColorRGB(c.getRGB());
            SaveManager.save(stats);
            changed();
        });
        return b;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton close = themedButton("Close", Theme.BTN_DEFAULT, this::dispose);
        footer.add(close);

        return footer;
    }

    private void changed() {
        if (onChanged != null) onChanged.run();
        repaint();
    }

    private double getScaleForPose(AccessoryPlacementOverlay.Pose pose) {
        return switch (pose) {
            case SITTING -> stats.getHatScaleNormal();
            case BED     -> stats.getHatScaleBed();
            case DRAG    -> stats.getHatScaleDrag();
        };
    }

    private void setScaleForPose(AccessoryPlacementOverlay.Pose pose, double scale) {
        switch (pose) {
            case SITTING -> stats.setHatScaleNormal(scale);
            case BED     -> stats.setHatScaleBed(scale);
            case DRAG    -> stats.setHatScaleDrag(scale);
        }
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
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-2, getHeight()-2,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);

                g2.dispose();
            }
        };
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 34));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }
}