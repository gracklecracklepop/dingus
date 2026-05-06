import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;

public class PetMenu {

    private JLabel nameLabel;        // name above bars
    private JPanel mainMenuPanel;    // optional if you want to rebuild

    private final JPanel panel;
    private final PetStats stats;
    private final JDialog hostDialog;

    private CardLayout cardLayout;
    private JPanel container;
    private JPanel statsPanel;

    private JProgressBar hungerBar, happinessBar, energyBar;
    private JLabel hungerLabel, happinessLabel, energyLabel;

    // titlebar coin label (next to traffic lights)
    private JLabel mainMenuCoinLabel;

    // ── Cooldown tracking ───────────────────────────────────────
    private static final long FEED_COOLDOWN_MS = 15 * 60 * 1000L;
    private long lastFedTime = 0L;
    private JButton feedButton;
    private Timer cooldownTimer;

    long ramUse;
    long base;
    private javax.swing.Timer usageTimer;
    OperatingSystemMXBean osBean = (OperatingSystemMXBean)
            ManagementFactory.getOperatingSystemMXBean();

    private static final String[] GRASS_URLS = {
            "https://www.thisoldhouse.com/wp-content/uploads/2020/08/iStock_511747120-scaled.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSw15kIqSjCMWOaDlOx-rG_3J1J7oXXIRZCoA&s",
            "https://media.istockphoto.com/id/161820827/photo/morning-dew-drops-on-green-leafs.jpg",
            "https://www.monrovia.com/media/catalog/product/cache/7b381462074fb6871f01f9faa7ed2e11/r/e/rest_2_2_2226.webp"
    };

    public PetMenu(PetStats stats, JDialog dialog) {
        this.stats = stats;
        this.hostDialog = dialog;

        cardLayout = new CardLayout();
        container  = new JPanel(cardLayout);
        container.setOpaque(false);

        container.add(buildMainMenu(), "menu");

        statsPanel = buildStatsMenu();
        container.add(statsPanel, "stats");

        this.panel = container;

        cooldownTimer = new Timer(1000, e -> refreshFeedButton());
        cooldownTimer.start();

        base = stats.getBaseRam();
        startUsageTimer();
    }

    public JPanel getPanel() { return panel; }

    // ───────────────────────── RAM usage hooks (unchanged) ─────────────────────

    public void usageAdd() throws InterruptedException {
        ramUse = ramUsage.runRamLiveMode((com.sun.management.OperatingSystemMXBean) osBean);
        stats.addHunger(((double)(ramUse - base)) / 1000000);
        updateLiveStats();
    }

    public void startUsageTimer() {
        if (usageTimer != null) usageTimer.stop();
        usageTimer = new javax.swing.Timer(1000, e -> {
            try { usageAdd(); }
            catch (InterruptedException ex) { throw new RuntimeException(ex); }
        });
        usageTimer.start();
    }

    public void stopUsageTimer() {
        if (usageTimer != null) {
            usageTimer.stop();
            usageTimer = null;
        }
    }

    // ── Cooldown Helpers ────────────────────────────────────────

    private boolean isFeedOnCooldown() {
        return (Instant.now().toEpochMilli() - lastFedTime) < FEED_COOLDOWN_MS;
    }

    private long feedCooldownRemainingMs() {
        return Math.max(0, FEED_COOLDOWN_MS - (Instant.now().toEpochMilli() - lastFedTime));
    }

    private String formatCooldown(long ms) {
        long total   = ms / 1000;
        long minutes = total / 60;
        long seconds = total % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void refreshFeedButton() {
        if (feedButton == null) return;
        if (isFeedOnCooldown()) {
            feedButton.setText("🍖 " + formatCooldown(feedCooldownRemainingMs()));
            feedButton.setEnabled(false);
            feedButton.setBackground(Theme.BTN_DISABLED);
        } else {
            feedButton.setText("🍖 Feed");
            feedButton.setEnabled(true);
            feedButton.setBackground(Theme.BTN_DEFAULT);
        }
        feedButton.repaint();
    }

    // ── Main Menu (mac titlebar + traffic buttons + coins) ─────────────────────

    private JPanel buildMainMenu() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Title is handled by content (name above bars). Keep titlebar clean.
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "");
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);

        // Titlebar overlay (traffic lights + coins)
        JPanel titleBar = buildTitleBar();
        wrapper.add(titleBar, BorderLayout.NORTH);

        // Body (name row + scroll content)
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        // Name above stat bars (like your old header), ellipsized (no scaling)
        String name = stats.getName();
        if (name == null || name.isBlank()) name = "DINGUS";

        JLabel nameLabel = Theme.mixedLabel(ellipsizeForMenu("🐱 " + name, Theme.MENU_WIDTH - 60, 15),
                15, Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel nameRow = new JPanel(new BorderLayout());
        nameRow.setOpaque(false);
        nameRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        nameRow.add(nameLabel, BorderLayout.WEST);

        body.add(nameRow, BorderLayout.NORTH);

        // Scrollable content
        JPanel content = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Insets in = getInsets();
                int width = 0;
                int height = in.top + in.bottom;

                for (Component c : getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension pref = c.getPreferredSize();
                    width = Math.max(width, pref.width);
                    height += pref.height;
                }
                return new Dimension(width + in.left + in.right, height);
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        hungerLabel    = styledLabel("Hunger: "    + stats.getHunger()    + "%");
        happinessLabel = styledLabel("Happiness: " + stats.getHappiness() + "%");
        energyLabel    = styledLabel("Energy: "    + stats.getEnergy()    + "%");

        hungerBar    = makeProgressBar((int) stats.getHunger());
        happinessBar = makeProgressBar(stats.getHappiness());
        energyBar    = makeProgressBar(stats.getEnergy());

        content.add(wrapBar(hungerLabel,    hungerBar));    content.add(Box.createVerticalStrut(8));
        content.add(wrapBar(happinessLabel, happinessBar)); content.add(Box.createVerticalStrut(8));
        content.add(wrapBar(energyLabel,    energyBar));    content.add(Box.createVerticalStrut(14));

        // Feed button with cooldown
        feedButton = makeButton("🍖 Feed", () -> {
            if (isFeedOnCooldown()) return;
            stats.addHunger(20);
            stats.addHappiness(5);
            stats.addCoins(5);
            updateLiveStats();
            save();
            lastFedTime = Instant.now().toEpochMilli();
            refreshFeedButton();
        });
        content.add(feedButton);
        refreshFeedButton();

        addButton(content, "🎾 Play", () -> {
            stats.addHappiness(20);
            stats.addEnergy(-10);
            stats.addHunger(-10);
            stats.addCoins(5);
            updateLiveStats();
            save();
            openRandomGrass();
        });

        addButton(content, "😴 Sleep", () -> {
            stats.addEnergy(10);
            updateLiveStats();
            save();
        });

        addButton(content, "🛒 Shop", this::openShopWindow);

        addButton(content, "⚙ Settings", () -> {
            SettingsDialog settings = new SettingsDialog(stats);
            settings.setVisible(true);
        });

        // (Hide/Exit removed here; now handled by traffic lights)

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 4));
        verticalBar.setUnitIncrement(10);
        verticalBar.setUI(createThinScrollBarUI());

        body.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(body, BorderLayout.CENTER);

        return wrapper;
    }

    public void refreshFromStats() {
        // name
        String name = stats.getName();
        if (name == null || name.isBlank()) name = "DINGUS";
        if (nameLabel != null) nameLabel.setText("🐱 " + name);

        // coins (if needed)
        if (mainMenuCoinLabel != null) mainMenuCoinLabel.setText("🪙 " + stats.getCoins());

        // bars (if you want immediate reflect)
        updateLiveStats();

        panel.revalidate();
        panel.repaint();
    }

    private JPanel buildTitleBar() {
        // Must match Theme.paintMacWindow() dot geometry:
        final int x0  = 12;
        final int y0  = 9;
        final int dot = 10;
        final int gap = 6;

        JPanel titleBar = new JPanel(null) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(10, Theme.TITLEBAR_HEIGHT);
            }
            @Override public void doLayout() {
                // position coin label tightly after the 3 dots
                int coinsX = x0 + (dot * 3) + (gap * 2) + 8; // smaller padding = closer to corner
                Dimension ps = mainMenuCoinLabel.getPreferredSize();
                mainMenuCoinLabel.setBounds(coinsX, 6, ps.width, ps.height);
            }
        };
        titleBar.setOpaque(false);

        // Invisible clickable hitboxes OVER the dots Theme draws
        JButton red    = makeTrafficHitbox(this::exitApp, "Exit");
        JButton orange = makeTrafficHitbox(this::hidePet, "Hide");
        JButton green  = makeTrafficHitbox(null, null); // no-op, purely for mac look (optional)

        red.setBounds(x0, y0, dot, dot);
        orange.setBounds(x0 + (dot + gap), y0, dot, dot);
        green.setBounds(x0 + 2 * (dot + gap), y0, dot, dot);
        green.setEnabled(false);

        titleBar.add(red);
        titleBar.add(orange);
        titleBar.add(green);

        // Coins next to traffic lights (render same as before: uses mixedLabel)
        mainMenuCoinLabel = Theme.mixedLabel("🪙 " + stats.getCoins(),
                Theme.FONT_SIZE_LABEL, Theme.ACCENT_COINS);
        titleBar.add(mainMenuCoinLabel);

        return titleBar;
    }

    private JButton makeTrafficHitbox(Runnable action, String tooltip) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                // intentionally empty: Theme.paintMacWindow already draws the circles
            }
        };
        b.setPreferredSize(new Dimension(10, 10));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tooltip != null) b.setToolTipText(tooltip);
        if (action != null) b.addActionListener(e -> action.run());
        return b;
    }

    private void hidePet() {
        PetTray.hide(hostDialog);
    }

    private void exitApp() {
        save();
        PetTray.remove();
        System.exit(0);
    }

    private void updateLiveStats() {
        updateBar(hungerBar,    hungerLabel,    "Hunger",    stats.getHunger());
        updateBar(happinessBar, happinessLabel, "Happiness", stats.getHappiness());
        updateBar(energyBar,    energyLabel,    "Energy",    stats.getEnergy());

        if (mainMenuCoinLabel != null) {
            mainMenuCoinLabel.setText("🪙 " + stats.getCoins());
            mainMenuCoinLabel.revalidate();
            mainMenuCoinLabel.repaint();
        }
    }

    // Ellipsis without scaling (uses your existing Theme mixed measurement)
    private String ellipsizeForMenu(String s, int maxWidth, int fontSize) {
        if (s == null) return "";
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try {
            if (Theme.mixedStringWidth(g2, s, fontSize) <= maxWidth) return s;
            final String ell = "…";
            String base = s;
            while (!base.isEmpty()) {
                int end = base.offsetByCodePoints(base.length(), -1);
                base = base.substring(0, end);
                String cand = base + ell;
                if (Theme.mixedStringWidth(g2, cand, fontSize) <= maxWidth) return cand;
            }
            return ell;
        } finally {
            g2.dispose();
        }
    }

    // ── Shop Menu (unchanged) ───────────────────────────────────

    private void openShopWindow() {
        JDialog shopDialog = new JDialog();
        shopDialog.setModal(true);
        shopDialog.setSize(500, 600);
        shopDialog.setLocationRelativeTo(null);
        shopDialog.setUndecorated(true);
        shopDialog.setType(Window.Type.UTILITY);
        shopDialog.setBackground(new Color(0, 0, 0, 0));

        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // keep your current shop styling; you can convert this later too
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel title = Theme.mixedLabel("🛒 Accessory Shop", Theme.FONT_SIZE_HEADING, Theme.TEXT_PRIMARY);

        JLabel shopCoinLabel = Theme.mixedLabel("🪙 " + stats.getCoins(), Theme.FONT_SIZE_LABEL, Theme.ACCENT_COINS);

        JButton closeBtn = makeButton("✖", shopDialog::dispose);
        closeBtn.setBackground(Theme.BTN_CLOSE);
        closeBtn.setPreferredSize(new Dimension(40, 30));

        header.add(title,         BorderLayout.WEST);
        header.add(shopCoinLabel, BorderLayout.CENTER);
        header.add(closeBtn,      BorderLayout.EAST);

        Point[] offset = {null};
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { offset[0] = e.getPoint(); }
        });
        header.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = shopDialog.getLocation();
                shopDialog.setLocation(
                        loc.x + e.getX() - offset[0].x,
                        loc.y + e.getY() - offset[0].y);
            }
        });

        mainContainer.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] icons = {"🎩", "👓", "🎀", "👑", "🎒", "🧣", "🎧", "🌸", "🕶️", "🧢"};

        for (int i = 0; i < 21; i++) {
            grid.add(createShopSlot(icons[i % icons.length], (i + 1) * 10, shopCoinLabel, "acc_" + i));
        }

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 0));
        bar.setUnitIncrement(16);
        bar.setUI(createThinScrollBarUI());

        mainContainer.add(scrollPane, BorderLayout.CENTER);
        shopDialog.add(mainContainer);
        shopDialog.setVisible(true);
    }

    private JPanel createShopSlot(String icon, int price, JLabel shopCoinLabel, String itemId) {
        JPanel slot = new JPanel(new BorderLayout(0, 5));
        slot.setBackground(Theme.BG_SHOP_SLOT);
        slot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BG_SHOP_SLOT_BORDER, 2),
                BorderFactory.createEmptyBorder(10, 5, 5, 5)
        ));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(Theme.emojiFont(40));
        iconLabel.setForeground(Theme.TEXT_PRIMARY);

        JButton buyBtn = makeButton("", null);

        if (stats.ownsAccessory(itemId)) {
            buyBtn.setText("Owned");
            buyBtn.setBackground(Theme.BG_SHOP_OWNED);
            buyBtn.setEnabled(false);
        } else {
            buyBtn.setText(price + "");
            buyBtn.setBackground(Theme.BG_SHOP_BUY);
            buyBtn.addActionListener(e -> {
                if (stats.getCoins() >= price) {
                    stats.addCoins(-price);
                    stats.addAccessory(itemId);
                    save();
                    shopCoinLabel.setText("🪙 " + stats.getCoins());
                    if (mainMenuCoinLabel != null) mainMenuCoinLabel.setText("🪙 " + stats.getCoins());
                    buyBtn.setText("Owned");
                    buyBtn.setBackground(Theme.BG_SHOP_OWNED);
                    buyBtn.setEnabled(false);
                } else {
                    buyBtn.setBackground(Theme.BG_SHOP_NO_FUNDS);
                    Timer t = new Timer(200, evt -> buyBtn.setBackground(Theme.BG_SHOP_BUY));
                    t.setRepeats(false); t.start();
                }
            });
        }

        slot.add(iconLabel, BorderLayout.CENTER);
        slot.add(buyBtn,    BorderLayout.SOUTH);
        return slot;
    }

    // ── Update a live bar ───────────────────────────────────────

    private void updateBar(JProgressBar bar, JLabel label, String name, double value) {
        bar.setValue((int) value);
        double valueRounded = (double) Math.round(value * 100.0) / 100;
        label.setText(name + ": " + valueRounded + "%");
        bar.setForeground(Theme.progressColor((int) value));
        bar.repaint();
        label.repaint();
    }

    // ── Stats Menu ──────────────────────────────────────────────

    private void showStats() {
        container.remove(statsPanel);
        statsPanel = buildStatsMenu();
        container.add(statsPanel, "stats");
        cardLayout.show(container, "stats");
        container.revalidate();
        container.repaint();
    }

    private JPanel buildStatsMenu() {
        JPanel p = createBasePanel();

        JLabel title = new JLabel("📊 Stats");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.emojiFont(Theme.FONT_SIZE_HEADING));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        p.add(makeBar("Hunger",    (int) stats.getHunger())); p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Happiness", stats.getHappiness()));    p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Energy",    stats.getEnergy()));       p.add(Box.createVerticalStrut(10));

        JLabel coins = new JLabel("🪙 Coins: " + stats.getCoins());
        coins.setForeground(Theme.ACCENT_COINS);
        coins.setFont(Theme.emojiFont(Theme.FONT_SIZE_SMALL));
        coins.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(coins);
        p.add(Box.createVerticalStrut(10));

        addButton(p, "⬅ Back", () -> cardLayout.show(container, "menu"));
        return p;
    }

    // ── Shared UI ───────────────────────────────────────────────

    private JPanel createBasePanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return p;
    }

    private JLabel styledLabel(String text) {
        return Theme.mixedLabel(text, Theme.FONT_SIZE_LABEL, Theme.TEXT_PRIMARY);
    }

    private JProgressBar makeProgressBar(int value) {
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override public void updateUI() {
                setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
                    @Override protected void paintDeterminate(Graphics g, JComponent c) {
                        int w = (int) (c.getWidth() * ((double) ((JProgressBar) c).getValue() / ((JProgressBar) c).getMaximum()));
                        int h = c.getHeight();
                        g.setColor(Theme.PROGRESS_TRACK);
                        g.fillRect(0, 0, c.getWidth(), h);
                        g.setColor(c.getForeground());
                        g.fillRect(0, 0, w, h);
                    }
                });
            }
        };
        bar.setValue(Math.max(0, Math.min(100, value)));
        bar.setStringPainted(false);
        bar.setBorderPainted(false);
        bar.setBackground(Theme.PROGRESS_TRACK);
        bar.setForeground(Theme.progressColor(value));
        return bar;
    }

    private JPanel wrapBar(JLabel label, JProgressBar bar) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Theme.MENU_WIDTH - 30, 30));
        p.add(label, BorderLayout.NORTH);
        p.add(bar,   BorderLayout.CENTER);
        return p;
    }

    private JPanel makeBar(String label, int value) {
        return wrapBar(styledLabel(label + ": " + value + "%"), makeProgressBar(value));
    }

    private void addButton(JPanel parent, String label, Runnable action) {
        parent.add(makeButton(label, action));
    }

    private JButton makeButton(String text, Runnable action) {
        int btnHeight  = 22;
        int btnSpacing = 6;
        int totalHeight = btnHeight + btnSpacing;

        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), btnHeight,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);

                g2.setColor(Theme.BTN_ACCENT);
                g2.drawRoundRect(0, 0, getWidth() - 1, btnHeight - 1,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);

                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);

                int textWidth = Theme.mixedStringWidth(g2, getText(), Theme.FONT_SIZE_BUTTON);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - textWidth) / 2;
                int y = (btnHeight + fm.getAscent() - fm.getDescent()) / 2;

                Theme.drawMixedString(g2, getText(), x, y, Theme.FONT_SIZE_BUTTON);

                g2.dispose();
            }
        };

        btn.setBackground(Theme.BTN_DEFAULT);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(Theme.MENU_WIDTH - 30, totalHeight));
        btn.setMaximumSize(new Dimension(Theme.MENU_WIDTH - 20, totalHeight));
        btn.setMinimumSize(new Dimension(Theme.MENU_WIDTH - 30, totalHeight));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 3, 3);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}
        };
    }

    private void save() { SaveManager.save(stats); }

    private void openRandomGrass() {
        String url = GRASS_URLS[(int)(Math.random() * GRASS_URLS.length)];
        try { Desktop.getDesktop().browse(new java.net.URI(url)); }
        catch (Exception e) { e.printStackTrace(); }
    }
}