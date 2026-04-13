import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.Desktop;
import java.lang.management.ManagementFactory;

public class PetMenu {

    private final JPanel panel;
    private final PetStats stats;

    private CardLayout cardLayout;
    private JPanel container;
    private JPanel statsPanel;

    private JProgressBar hungerBar, happinessBar, energyBar;
    private JLabel hungerLabel, happinessLabel, energyLabel;
    private JLabel mainMenuCoinLabel;

    private javax.swing.Timer usageTimer;

    private static final String[] GRASS_URLS = {
            "https://www.thisoldhouse.com/wp-content/uploads/2020/08/iStock_511747120-scaled.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSw15kIqSjCMWOaDlOx-rG_3J1J7oXXIRZCoA&s",
            "https://media.istockphoto.com/id/161820827/photo/morning-dew-drops-on-green-leafs.jpg",
            "https://www.monrovia.com/media/catalog/product/cache/7b381462074fb6871f01f9faa7ed2e11/r/e/rest_2_2_2226.webp"
    };

    public PetMenu(PetStats stats, JDialog dialog) {
        this.stats = stats;

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setOpaque(false);

        container.add(buildMainMenu(dialog), "menu");

        statsPanel = buildStatsMenu();
        container.add(statsPanel, "stats");

        this.panel = container;
    }

    public JPanel getPanel() { return panel; }

    // ── Usage Timer ─────────────────────────────────────────────

    public void startUsageTimer() {
        if (usageTimer != null) usageTimer.stop();
        usageTimer = new javax.swing.Timer(1000, e -> usageAdd());
        usageTimer.start();
    }

    public void stopUsageTimer() {
        if (usageTimer != null) {
            usageTimer.stop();
            usageTimer = null;
        }
    }

    public void usageAdd() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();
            //ramUsage.runLiveMode(osBean);
            long baseRam = PetStats.getBaseRam();
            stats.addHunger(1);
            updateLiveStats();
    }

    // ── Main Menu ───────────────────────────────────────────────

    private JPanel buildMainMenu(JDialog dialog) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_MAIN_TRANSPARENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("🐱 " + stats.getName());
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(15));

        mainMenuCoinLabel = new JLabel("🪙 " + stats.getCoins());
        mainMenuCoinLabel.setForeground(Theme.ACCENT_COINS);
        mainMenuCoinLabel.setFont(Theme.font(Theme.FONT_SIZE_LABEL));

        header.add(title, BorderLayout.WEST);
        header.add(mainMenuCoinLabel, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        // Scrollable content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        hungerLabel    = styledLabel("Hunger: "    + stats.getHunger()    + "%");
        happinessLabel = styledLabel("Happiness: " + stats.getHappiness() + "%");
        energyLabel    = styledLabel("Energy: "    + stats.getEnergy()    + "%");

        hungerBar    = makeProgressBar(stats.getHunger());
        happinessBar = makeProgressBar(stats.getHappiness());
        energyBar    = makeProgressBar(stats.getEnergy());

        content.add(wrapBar(hungerLabel,    hungerBar));    content.add(Box.createVerticalStrut(8));
        content.add(wrapBar(happinessLabel, happinessBar)); content.add(Box.createVerticalStrut(8));
        content.add(wrapBar(energyLabel,    energyBar));    content.add(Box.createVerticalStrut(15));

        addButton(content, "🍖 Feed", () -> {
            stats.addHunger(20); stats.addHappiness(5); stats.addCoins(5);
            updateLiveStats(); save();
        });

        addButton(content, "🎾 Play", () -> {
            stats.addHappiness(20); stats.addEnergy(-10); stats.addHunger(-10); stats.addCoins(5);
            updateLiveStats(); save(); openRandomGrass();
        });

        addButton(content, "😴 Sleep", () -> {
            stats.addEnergy(10); updateLiveStats(); save();
        });

        addButton(content, "🛒 Shop",     this::openShopWindow);
        addButton(content, "⚙️ Settings", () -> System.out.println("Opening settings!"));
        addButton(content, "👁 Hide",     () -> PetTray.hide(dialog));
        addButton(content, "❌ Exit",     () -> { stopUsageTimer(); save(); PetTray.remove(); System.exit(0); });

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(Theme.MENU_WIDTH, Theme.MENU_SCROLL_HEIGHT));

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 0));
        verticalBar.setUnitIncrement(16);
        verticalBar.setUI(createThinScrollBarUI());

        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void updateLiveStats() {
        updateBar(hungerBar,    hungerLabel,    "Hunger",    stats.getHunger());
        updateBar(happinessBar, happinessLabel, "Happiness", stats.getHappiness());
        updateBar(energyBar,    energyLabel,    "Energy",    stats.getEnergy());
        mainMenuCoinLabel.setText("🪙 " + stats.getCoins());
        stats.printStats();
    }

    // ── Shop Menu ───────────────────────────────────────────────

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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_MAIN_TRANSPARENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("🛒 Accessory Shop");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));

        JLabel shopCoinLabel = new JLabel("🪙 " + stats.getCoins());
        shopCoinLabel.setForeground(Theme.ACCENT_COINS);
        shopCoinLabel.setFont(Theme.font(Theme.FONT_SIZE_LABEL));

        JButton closeBtn = makeButton("X", shopDialog::dispose);
        closeBtn.setBackground(Theme.BTN_CLOSE);
        closeBtn.setPreferredSize(new Dimension(40, 30));

        header.add(title,         BorderLayout.WEST);
        header.add(shopCoinLabel, BorderLayout.CENTER);
        header.add(closeBtn,      BorderLayout.EAST);

        // Draggable header
        Point[] offset = {null};
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { offset[0] = e.getPoint(); }
        });
        header.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = shopDialog.getLocation();
                shopDialog.setLocation(loc.x + e.getX() - offset[0].x, loc.y + e.getY() - offset[0].y);
            }
        });

        mainContainer.add(header, BorderLayout.NORTH);

        // Grid
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
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(Theme.TEXT_PRIMARY);

        JButton buyBtn = makeButton("", null);
        buyBtn.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        if (stats.ownsAccessory(itemId)) {
            buyBtn.setText("Owned");
            buyBtn.setBackground(Theme.BG_SHOP_OWNED);
            buyBtn.setEnabled(false);
        } else {
            buyBtn.setText(price + " 🪙");
            buyBtn.setBackground(Theme.BG_SHOP_BUY);
            buyBtn.addActionListener(e -> {
                if (stats.getCoins() >= price) {
                    stats.addCoins(-price); stats.addAccessory(itemId); save();
                    shopCoinLabel.setText("🪙 " + stats.getCoins());
                    mainMenuCoinLabel.setText("🪙 " + stats.getCoins());
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
        slot.add(buyBtn, BorderLayout.SOUTH);
        return slot;
    }

    // ── Update a live bar ───────────────────────────────────────

    private void updateBar(JProgressBar bar, JLabel label, String name, int value) {
        bar.setValue(value);
        label.setText(name + ": " + value + "%");
        bar.setForeground(Theme.progressColor(value));
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
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        p.add(makeBar("Hunger",    stats.getHunger()));    p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Happiness", stats.getHappiness())); p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Energy",    stats.getEnergy()));    p.add(Box.createVerticalStrut(10));

        JLabel coins = new JLabel("🪙 Coins: " + stats.getCoins());
        coins.setForeground(Theme.ACCENT_COINS);
        coins.setFont(Theme.font(Theme.FONT_SIZE_SMALL));
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
                g2.setColor(Theme.BG_MAIN_TRANSPARENT);
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
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_PRIMARY);
        l.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        return l;
    }

    private JProgressBar makeProgressBar(int value) {
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override
            public void updateUI() {
                setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
                    @Override
                    protected void paintDeterminate(Graphics g, JComponent c) {
                        int width  = (int)(c.getWidth() * ((double) ((JProgressBar) c).getValue() / ((JProgressBar) c).getMaximum()));
                        int height = c.getHeight();

                        g.setColor(Theme.PROGRESS_TRACK);
                        g.fillRect(0, 0, c.getWidth(), height);

                        g.setColor(c.getForeground());
                        g.fillRect(0, 0, width, height);
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
        parent.add(Box.createVerticalStrut(5));
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled())                bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);
                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };

        btn.setBackground(Theme.BTN_DEFAULT);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(Theme.MENU_WIDTH - 30, 40));
        btn.setMaximumSize(new Dimension(Theme.MENU_WIDTH - 20, 20));
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