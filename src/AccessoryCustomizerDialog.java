import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.util.List;

public class AccessoryCustomizerDialog extends JDialog {

    private final PetStats stats;
    private final Runnable onChanged;
    private final Window petWindow; // Dingus window (overlay target)

    private final JComboBox<AccessoryPlacementOverlay.Pose> poseBox =
            new JComboBox<>(AccessoryPlacementOverlay.Pose.values());

    private final JLabel sizeLabel = new JLabel("Size: 100%");
    private final JSlider sizeSlider = new JSlider(50, 200, 100);

    private final JSlider rSlider = new JSlider(0, 255, 31);
    private final JSlider gSlider = new JSlider(0, 255, 27);
    private final JSlider bSlider = new JSlider(0, 255, 22);

    // RGB changes are staged (applied only when dialog closes)
    private final int initialHatColorRGB;
    private int pendingHatColorRGB;
    private boolean pendingColorDirty = false;

    private final JPanel colorPreview = new JPanel() {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color c = new Color(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            g2.setColor(Theme.BG_INPUT_BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);

            g2.dispose();
        }
    };

    private final JLabel hexLabel = new JLabel("", SwingConstants.RIGHT);

    public AccessoryCustomizerDialog(Window ownerDialog, Window petWindow, PetStats stats, Runnable onChanged) {
        super(ownerDialog);
        TrayNotifier.ensureInitialized();

        this.stats = stats;
        this.onChanged = onChanged;
        this.petWindow = petWindow;

        this.initialHatColorRGB = stats.getHatColorRGB();
        this.pendingHatColorRGB = initialHatColorRGB;

        setModal(true);
        setUndecorated(true);
        setType(Type.UTILITY);
        setBackground(new Color(0,0,0,0));
        setSize(620, 420);
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

        // Make sliders match theme (size slider same style as RGB sliders)
        styleSlider(sizeSlider, Theme.BTN_DEFAULT);
        styleSlider(rSlider, Theme.BTN_DEFAULT);
        styleSlider(gSlider, Theme.BTN_DEFAULT);
        styleSlider(bSlider, Theme.BTN_DEFAULT);

        initRgbFromStats();
        initSizeFromStats();
        hookEvents();
        refreshHex();

        // ESC closes (and commits staged RGB)
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        root.getActionMap().put("esc", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }

    @Override public void dispose() {
        commitPendingColorIfNeeded();
        super.dispose();
    }

    private void commitPendingColorIfNeeded() {
        if (!pendingColorDirty) return;
        if (pendingHatColorRGB == stats.getHatColorRGB()) return;

        stats.setHatColorRGB(pendingHatColorRGB);
        SaveManager.save(stats);
        changed(); // repaint pet ONCE, at close
    }

    private JComponent buildContent() {
        JPanel main = new JPanel(new BorderLayout(12, 0));
        main.setOpaque(false);

        main.add(buildHatList(), BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        right.add(buildPlacementRow());
        right.add(Box.createVerticalStrut(12));
        right.add(buildRgbPanel());
        right.add(Box.createVerticalGlue());
        right.add(buildFooter());

        main.add(right, BorderLayout.CENTER);
        return main;
    }

    private JComponent buildHatList() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setPreferredSize(new Dimension(220, 10));
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Hats (owned)");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        list.add(title);
        list.add(Box.createVerticalStrut(8));

        list.add(themedButton("None", Theme.BTN_DEFAULT, () -> {
            stats.setEquippedHatId(null);
            SaveManager.save(stats);
            changed();
        }));
        list.add(Box.createVerticalStrut(6));

        List<StoreItem> hats = AccessoryCatalog.ownedHats(stats);
        for (StoreItem it : hats) {
            list.add(themedButton(it.glyph + "  " + it.name, Theme.BTN_DEFAULT, () -> {
                stats.setEquippedHatId(it.id);
                SaveManager.save(stats);
                changed();
            }));
            list.add(Box.createVerticalStrut(6));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 0));
        return scroll;
    }

    private JComponent buildPlacementRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 10);
        c.anchor = GridBagConstraints.WEST;

        JLabel poseLbl = new JLabel("Pose:");
        poseLbl.setForeground(Theme.TEXT_PRIMARY);
        poseLbl.setFont(Theme.font(Theme.FONT_SIZE_LABEL));

        poseBox.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        poseBox.setBackground(Theme.BG_INPUT);
        poseBox.setForeground(Theme.TEXT_PRIMARY);
        poseBox.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2));

        sizeLabel.setForeground(Theme.TEXT_PRIMARY);
        sizeLabel.setFont(Theme.font(Theme.FONT_SIZE_LABEL));

        sizeSlider.setOpaque(false);
        sizeSlider.setFocusable(false);
        sizeSlider.setPreferredSize(new Dimension(260, 28));

        // Size column: label on top, slider underneath
        JPanel sizeCol = new JPanel();
        sizeCol.setOpaque(false);
        sizeCol.setLayout(new BoxLayout(sizeCol, BoxLayout.Y_AXIS));
        sizeCol.add(sizeLabel);
        sizeCol.add(Box.createVerticalStrut(4));
        sizeCol.add(sizeSlider);

        JButton setPos = themedButton("Set Hat Position", Theme.BTN_SECONDARY, this::doSetHatPosition);

        // Pose
        c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        row.add(poseLbl, c);

        c.gridx = 1;
        row.add(poseBox, c);

        // Size column gets stretch space
        c.gridx = 2; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        row.add(sizeCol, c);

        // Button on the right
        c.gridx = 3; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.insets = new Insets(0, 0, 0, 0);
        row.add(setPos, c);

        return row;
    }

    // ... keep the rest of your AccessoryCustomizerDialog as-is,
// only replace doSetHatPosition() and add the helper findPetPanel(...)

    private void doSetHatPosition() {
        StoreItem it = AccessoryCatalog.byId(stats.getEquippedHatId());
        if (it == null || it.glyph == null) {
            TrayNotifier.showNotification("Dingus", "Equip a hat first.", TrayIcon.MessageType.INFO);
            return;
        }
        if (petWindow == null) {
            TrayNotifier.showNotification("Dingus", "Pet window not found.", TrayIcon.MessageType.ERROR);
            return;
        }

        AccessoryPlacementOverlay.Pose pose = (AccessoryPlacementOverlay.Pose) poseBox.getSelectedItem();

        // Use pending color if you're staging RGB; otherwise use stats.getHatColorRGB()
        Color fill = new Color(stats.getHatColorRGB(), true);
        Color outline = Theme.BG_INPUT_BORDER;

        int sizePx = computeHatSizePx(); // your existing method (consistent size)

        PetPanel petPanel = findPetPanel(petWindow);
        if (petPanel == null) {
            TrayNotifier.showNotification("Dingus", "PetPanel not found.", TrayIcon.MessageType.ERROR);
            return;
        }

        // Switch the *real* sprite to match dropdown pose
        petPanel.beginHatPlacementPreview(pose);

        Rectangle spriteBox = petPanel.getSpriteBoxInWindowCoords(pose);

        try {
            double[] res = AccessoryPlacementOverlay.pickOnPetWindow(
                    this,
                    petWindow,
                    it.glyph,
                    fill,
                    outline,
                    pose,
                    sizePx,
                    spriteBox
            );
            if (res == null) return;

            switch (pose) {
                case SITTING -> { stats.setHeadNormalXFrac(res[0]); stats.setHeadNormalYFrac(res[1]); }
                case BED     -> { stats.setHeadBedXFrac(res[0]);    stats.setHeadBedYFrac(res[1]); }
                case DRAG    -> { stats.setHeadDragXFrac(res[0]);   stats.setHeadDragYFrac(res[1]); }
            }

            SaveManager.save(stats);
            changed();
        } finally {
            // Restore whatever the pet was actually doing
            petPanel.endHatPlacementPreview();
        }
    }

    private static PetPanel findPetPanel(Window w) {
        if (w == null) return null;

        if (w instanceof RootPaneContainer rpc) {
            Container c = rpc.getContentPane();
            return findPetPanelIn(c);
        }
        if (w instanceof Container c) {
            return findPetPanelIn(c);
        }
        return null;
    }

    private static PetPanel findPetPanelIn(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof PetPanel pp) return pp;
            if (comp instanceof Container child) {
                PetPanel found = findPetPanelIn(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JComponent buildRgbPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);

        JLabel title = new JLabel("Hat Color (RGB)");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        wrap.add(title, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3, 1, 0, 10));
        rows.setOpaque(false);

        rows.add(rgbRow("R", rSlider));
        rows.add(rgbRow("G", gSlider));
        rows.add(rgbRow("B", bSlider));

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);
        colorPreview.setOpaque(false);
        colorPreview.setPreferredSize(new Dimension(110, 20));

        hexLabel.setForeground(Theme.TEXT_SECONDARY);
        hexLabel.setFont(Theme.font(Theme.FONT_SIZE_SMALL));

        bottom.add(colorPreview, BorderLayout.WEST);
        bottom.add(hexLabel, BorderLayout.CENTER);

        wrap.add(rows, BorderLayout.CENTER);
        wrap.add(bottom, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel rgbRow(String label, JSlider s) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(Theme.TEXT_PRIMARY);
        l.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        l.setPreferredSize(new Dimension(18, 10));

        s.setOpaque(false);
        s.setFocusable(false);
        s.setPreferredSize(new Dimension(240, 28));

        row.add(l, BorderLayout.WEST);
        row.add(s, BorderLayout.CENTER);
        return row;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton close = themedButton("Close", Theme.BTN_DEFAULT, this::dispose);
        footer.add(close);

        return footer;
    }

    private void hookEvents() {
        poseBox.addActionListener(e -> changed());

        // consistent size across all sprites: set ALL scales together
        sizeSlider.addChangeListener(e -> {
            int pct = sizeSlider.getValue();
            sizeLabel.setText("Size: " + pct + "%");

            double s = clamp(pct / 100.0, 0.50, 2.00);
            stats.setHatScaleNormal(s);
            stats.setHatScaleBed(s);
            stats.setHatScaleDrag(s);

            if (!sizeSlider.getValueIsAdjusting()) {
                SaveManager.save(stats);
                changed();
            } else {
                changed();
            }
        });

        // RGB: update ONLY the dialog preview + staged value (no pet repaint, no save)
        Runnable rgbStage = () -> {
            pendingHatColorRGB = (0xFF << 24)
                    | (rSlider.getValue() << 16)
                    | (gSlider.getValue() << 8)
                    | (bSlider.getValue());
            pendingColorDirty = (pendingHatColorRGB != initialHatColorRGB);

            refreshHex();
            colorPreview.repaint();
        };

        rSlider.addChangeListener(e -> rgbStage.run());
        gSlider.addChangeListener(e -> rgbStage.run());
        bSlider.addChangeListener(e -> rgbStage.run());
    }

    private void refreshHex() {
        hexLabel.setText(String.format("#%02X%02X%02X", rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
    }

    private void initRgbFromStats() {
        Color c = new Color(stats.getHatColorRGB(), true);
        rSlider.setValue(c.getRed());
        gSlider.setValue(c.getGreen());
        bSlider.setValue(c.getBlue());
    }

    private void initSizeFromStats() {
        // canonical = normal (and we keep all 3 equal)
        int pct = (int) Math.round(stats.getHatScaleNormal() * 100.0);
        pct = clampInt(pct, 50, 200);
        sizeSlider.setValue(pct);
        sizeLabel.setText("Size: " + pct + "%");
    }

    private int computeHatSizePx() {
        int base = 26; // must match PetPanel base
        double scale = clamp(stats.getHatScaleNormal(), 0.50, 2.00);
        int sizePx = (int) Math.round(base * scale);
        return clampInt(sizePx, 12, 72);
    }

    private void changed() {
        if (onChanged != null) onChanged.run();
    }

    // ───────────────── Slider theme ─────────────────

    private void styleSlider(JSlider s, Color fillColor) {
        s.setOpaque(false);
        s.setFocusable(false);
        s.setPaintTicks(false);
        s.setPaintLabels(false);
        s.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        s.setUI(new BasicSliderUI(s) {
            @Override protected Dimension getThumbSize() {
                return new Dimension(16, 16);
            }

            @Override public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int trackH = 8;
                int y = trackRect.y + (trackRect.height - trackH) / 2;

                int x1 = trackRect.x;
                int w  = trackRect.width;

                int thumbCx = thumbRect.x + thumbRect.width / 2;
                int filledW = Math.max(0, Math.min(w, thumbCx - x1));

                g2.setColor(Theme.BG_INPUT);
                g2.fillRoundRect(x1, y, w, trackH, 8, 8);

                g2.setColor(fillColor);
                g2.fillRoundRect(x1, y, filledW, trackH, 8, 8);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x1, y, w - 1, trackH - 1, 8, 8);

                g2.dispose();
            }

            @Override public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = thumbRect.x;
                int y = thumbRect.y;
                int w = thumbRect.width;
                int h = thumbRect.height;

                Color fill = slider.isEnabled() ? Theme.BTN_DEFAULT : Theme.BTN_DISABLED;

                g2.setColor(fill);
                g2.fillOval(x, y, w - 1, h - 1);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, w - 1, h - 1);

                g2.dispose();
            }

            @Override public void paintFocus(Graphics g) {
                // no focus ring
            }
        });
    }

    // ───────────────── Buttons ─────────────────

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

    private static int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}