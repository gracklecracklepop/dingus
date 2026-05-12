import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ShopDialog extends JDialog {

    private final PetStats stats;
    private final Runnable onCoinsChanged;
    private final Window petWindow; // Dingus window (used for accessory placement overlay)

    private JLabel coinLabel;

    // Must match Theme.paintMacWindow() traffic light geometry
    private static final int TL_X0  = 12;
    private static final int TL_Y0  = 9;
    private static final int TL_DOT = 10;
    private static final int TL_GAP = 6;

    public ShopDialog(Window owner, Window petWindow, PetStats stats, Runnable onCoinsChanged) {
        super(owner);
        this.stats = stats;
        this.onCoinsChanged = onCoinsChanged;
        this.petWindow = petWindow;

        setModal(true);
        setSize(520, 620);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "");
                g2.dispose();
            }
        };
        root.setOpaque(false);

        root.add(buildTitlebarOverlay(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ─────────────────────────────────────────────────────────────
    // Titlebar overlay: hitboxes + coins on TOP RIGHT
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

                if (coinLabel != null) {
                    Dimension ps = coinLabel.getPreferredSize();
                    int x = getWidth() - 12 - ps.width;
                    coinLabel.setBounds(x, 5, ps.width, ps.height + 2);
                }
            }

            private Component find(String name) {
                for (Component c : getComponents()) if (name.equals(c.getName())) return c;
                return null;
            }
        };
        layer.setOpaque(false);

        JButton red = makeTrafficHitbox(this::dispose, "Close");
        red.setName("tl_red");

        JButton orange = makeTrafficHitbox(this::dispose, "Back");
        orange.setName("tl_orange");

        JButton green = makeTrafficHitbox(null, null);
        green.setName("tl_green");
        green.setEnabled(false);

        layer.add(red, Integer.valueOf(2));
        layer.add(orange, Integer.valueOf(2));
        layer.add(green, Integer.valueOf(2));

        coinLabel = Theme.mixedLabel("🪙 " + stats.getCoins(), Theme.FONT_SIZE_LABEL, Theme.ACCENT_COINS);
        layer.add(coinLabel, Integer.valueOf(1));

        // Drag window by titlebar
        Point[] offset = {null};
        layer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { offset[0] = e.getPoint(); }
        });
        layer.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - offset[0].x, loc.y + e.getY() - offset[0].y);
            }
        });

        return layer;
    }

    private JButton makeTrafficHitbox(Runnable action, String tooltip) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) { /* Theme draws dots */ }
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
    // Body
    // ─────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        JLabel title = Theme.mixedLabel("🛒 ACCESSORY SHOP", Theme.FONT_SIZE_HEADING, Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        body.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
        grid.setOpaque(false);

        for (StoreItem it : AccessoryCatalog.SHOP_ITEMS) {
            grid.add(createShopSlot(it));
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 0));
        bar.setUnitIncrement(16);
        bar.setUI(createThinScrollBarUI());

        body.add(scroll, BorderLayout.CENTER);

        // Footer: Customize button (ONLY place you can set accessory position)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton customize = themedButton("Customize", Theme.BTN_SECONDARY, () -> {
            AccessoryCustomizerDialog d = new AccessoryCustomizerDialog(
                    this,            // owner dialog
                    petWindow,       // target pet window for overlay placement
                    stats,
                    () -> {
                        SaveManager.save(stats);
                        updateCoinsUI();
                        if (onCoinsChanged != null) onCoinsChanged.run();
                    }
            );
            d.setVisible(true);
        });

        footer.add(customize);
        body.add(footer, BorderLayout.SOUTH);

        return body;
    }

    private JPanel createShopSlot(StoreItem it) {
        JPanel slot = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                g2.setColor(Theme.BG_SHOP_SLOT);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                g2.dispose();
            }
        };
        slot.setOpaque(false);
        slot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel iconBox = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                g2.setColor(Theme.BG_INPUT);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                g2.dispose();
            }
        };
        iconBox.setOpaque(false);
        iconBox.setPreferredSize(new Dimension(120, 62));

        JLabel iconLabel = new JLabel(it.glyph == null ? "?" : it.glyph);
        iconLabel.setFont(Theme.emojiFont(30));
        iconLabel.setForeground(Theme.TEXT_PRIMARY);
        iconBox.add(iconLabel);

        JButton buy = makePriceButton();

        if (stats.ownsAccessory(it.id)) {
            buy.setText("OWNED");
            buy.setBackground(Theme.BG_SHOP_OWNED);
            buy.setEnabled(false);
        } else {
            buy.setText(String.valueOf(it.price));
            buy.setBackground(Theme.BTN_DEFAULT);
            buy.addActionListener(e -> {
                if (stats.getCoins() >= it.price) {
                    stats.addCoins(-it.price);
                    stats.addAccessory(it.id);
                    SaveManager.save(stats);

                    buy.setText("OWNED");
                    buy.setBackground(Theme.BG_SHOP_OWNED);
                    buy.setEnabled(false);

                    updateCoinsUI();
                } else {
                    Color old = buy.getBackground();
                    buy.setBackground(Theme.BG_SHOP_NO_FUNDS);
                    Timer t = new Timer(180, evt -> buy.setBackground(old));
                    t.setRepeats(false);
                    t.start();
                }
            });
        }

        slot.add(iconBox, BorderLayout.CENTER);
        slot.add(buy, BorderLayout.SOUTH);
        return slot;
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
        btn.setPreferredSize(new Dimension(120, 34));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton makePriceButton() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled()) bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed()) bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(0, 0, getWidth()-1, getHeight()-1);

                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                int x = (getWidth()-fm.stringWidth(t))/2;
                int y = (getHeight()+fm.getAscent()-fm.getDescent())/2;
                g2.drawString(t, x, y);

                g2.dispose();
            }
        };

        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(10, 28));
        btn.setBackground(Theme.BTN_DEFAULT);
        return btn;
    }

    private void updateCoinsUI() {
        if (coinLabel != null) {
            coinLabel.setText("🪙 " + stats.getCoins());
            coinLabel.revalidate();
            coinLabel.repaint();
        }
        if (onCoinsChanged != null) onCoinsChanged.run();
        repaint();
    }

    private BasicScrollBarUI createThinScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Theme.SCROLLBAR_THUMB;
                trackColor = Theme.SCROLLBAR_TRACK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setColor(thumbColor);
                g2.fillRect(r.x, r.y, r.width, r.height);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}
        };
    }
}