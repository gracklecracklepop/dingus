import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;

public class PetMenu {

    static final int WIDTH = 150;

    private final JPanel panel;
    private final PetStats stats;

    private CardLayout cardLayout;
    private JPanel container;
    private JPanel statsPanel;

    // Live bar references for main menu only
    private JProgressBar hungerBar, happinessBar, energyBar;
    private JLabel hungerLabel, happinessLabel, energyLabel;

    // Live coin label on the main menu
    private JLabel mainMenuCoinLabel;

    private static final String[] GRASS_URLS = {
            "https://www.thisoldhouse.com/wp-content/uploads/2020/08/iStock_511747120-scaled.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSw15kIqSjCMWOaDlOx-rG_3J1J7oXXIRZCoA&s",
            "https://media.istockphoto.com/id/161820827/photo/morning-dew-drops-on-green-leafs.jpg?s=612x612&w=0&k=20&c=qxLfhCiF7yuICQr_Nm9c1ORLl3HDaSk8_F-xs_P5S-w=",
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

    // ── Main Menu ───────────────────────────────────────────────

    private JPanel buildMainMenu(JDialog dialog) {
        // 1. Create main wrapper with the rounded dark background
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 40, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 2. Fixed Header Panel (Title on left, Coins on right)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Spacing below header

        JLabel title = new JLabel("🐱 " + stats.getName());
        title.setForeground(Color.WHITE);
        title.setFont(loadFont(15));

        mainMenuCoinLabel = new JLabel("🪙 " + stats.getCoins());
        mainMenuCoinLabel.setForeground(new Color(255, 215, 0));
        mainMenuCoinLabel.setFont(loadFont(12));

        header.add(title, BorderLayout.WEST);
        header.add(mainMenuCoinLabel, BorderLayout.EAST);

        wrapper.add(header, BorderLayout.NORTH); // Pins it to the top

        // 3. Scrollable Content Panel
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
            stats.addHunger(20);
            stats.addHappiness(5);
            stats.addCoins(5);
            updateLiveStats();
            save();
        });

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
        addButton(content, "⚙️ Settings", () -> System.out.println("Opening settings!"));
        addButton(content, "👁 Hide",     () -> PetTray.hide(dialog));
        addButton(content, "❌ Exit",     () -> { save(); PetTray.remove(); System.exit(0); });

        // 4. Configure Scroll Pane
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5)); // Tiny right padding so scrollbar doesn't overlap edges
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Lock the height of the scroll area so scrolling is required (e.g. 230px tall)
        scrollPane.setPreferredSize(new Dimension(WIDTH, 230));

        // Apply Custom Thin Scrollbar UI
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(3, 0)); // Ultra thin
        verticalBar.setUnitIncrement(16); // Smooth scrolling speed
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(120, 120, 120, 200);
                this.trackColor = new Color(0, 0, 0, 0); // Transparent track
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 3, 3);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {} // Hidden
        });

        wrapper.add(scrollPane, BorderLayout.CENTER); // Fills remaining space below header

        return wrapper;
    }

    private void updateLiveStats() {
        updateBar(hungerBar,    hungerLabel,    "Hunger",    stats.getHunger());
        updateBar(happinessBar, happinessLabel, "Happiness", stats.getHappiness());
        updateBar(energyBar,    energyLabel,    "Energy",    stats.getEnergy());
        mainMenuCoinLabel.setText("🪙 " + stats.getCoins());
        stats.printStats();
    }

    // ── Shop Menu (Centered Pop-up) ──────────────────────────────

    private void openShopWindow() {
        JDialog shopDialog = new JDialog();
        shopDialog.setTitle("🛒 Accessory Shop");
        shopDialog.setModal(true);
        shopDialog.setSize(500, 600);
        shopDialog.setLocationRelativeTo(null);
        shopDialog.setLayout(new BorderLayout());
        shopDialog.getContentPane().setBackground(new Color(30, 30, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(40, 40, 40));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Accessory Shop");
        title.setForeground(Color.WHITE);
        title.setFont(loadFont(20));

        JLabel shopCoinLabel = new JLabel("🪙 " + stats.getCoins());
        shopCoinLabel.setForeground(new Color(255, 215, 0));
        shopCoinLabel.setFont(loadFont(18));

        header.add(title, BorderLayout.WEST);
        header.add(shopCoinLabel, BorderLayout.EAST);
        shopDialog.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 4, 15, 15));
        grid.setBackground(new Color(30, 30, 30));
        grid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] sampleIcons = {"🎩", "👓", "🎀", "👑", "🎒", "🧣", "🎧", "🌸", "🕶️", "🧢"};

        for (int i = 0; i < 40; i++) {
            String iconStr = sampleIcons[i % sampleIcons.length];
            int price = (i + 1) * 10;
            String uniqueItemId = "acc_" + i;

            JPanel slot = createShopSlot(iconStr, price, shopCoinLabel, uniqueItemId);
            grid.add(slot);
        }

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(30, 30, 30));
        shopDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setBackground(new Color(40, 40, 40));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeBtn = new JButton("Close Shop");
        closeBtn.setFont(loadFont(14));
        closeBtn.setBackground(new Color(255, 100, 100));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> shopDialog.dispose());

        footer.add(closeBtn);
        shopDialog.add(footer, BorderLayout.SOUTH);

        shopDialog.setVisible(true);
    }

    private JPanel createShopSlot(String icon, int price, JLabel shopCoinLabel, String itemId) {
        JPanel slot = new JPanel(new BorderLayout(0, 5));
        slot.setBackground(new Color(50, 50, 50));
        slot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 2),
                BorderFactory.createEmptyBorder(10, 5, 5, 5)
        ));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(Color.WHITE);

        JButton buyBtn = new JButton();
        buyBtn.setFont(loadFont(12));
        buyBtn.setFocusPainted(false);
        buyBtn.setForeground(Color.WHITE);

        if (stats.ownsAccessory(itemId)) {
            buyBtn.setText("Owned");
            buyBtn.setBackground(new Color(100, 150, 100));
            buyBtn.setEnabled(false);
        } else {
            buyBtn.setText(price + " 🪙");
            buyBtn.setBackground(new Color(70, 70, 70));

            buyBtn.addActionListener(e -> {
                if (stats.getCoins() >= price) {
                    stats.addCoins(-price);
                    stats.addAccessory(itemId);
                    save();

                    shopCoinLabel.setText("🪙 " + stats.getCoins());
                    mainMenuCoinLabel.setText("🪙 " + stats.getCoins());

                    buyBtn.setText("Owned");
                    buyBtn.setBackground(new Color(100, 150, 100));
                    buyBtn.setEnabled(false);
                } else {
                    buyBtn.setBackground(new Color(150, 50, 50));
                    Timer t = new Timer(200, evt -> buyBtn.setBackground(new Color(70, 70, 70)));
                    t.setRepeats(false);
                    t.start();
                }
            });
        }

        slot.add(iconLabel, BorderLayout.CENTER);
        slot.add(buyBtn, BorderLayout.SOUTH);
        return slot;
    }

    // ── Update a live bar in-place ───────────────────────────────

    private void updateBar(JProgressBar bar, JLabel label, String name, int value) {
        bar.setValue(value);
        label.setText(name + ": " + value + "%");
        if (value >= 70)      bar.setForeground(new Color(100, 200, 100));
        else if (value >= 30) bar.setForeground(new Color(255, 200, 50));
        else                  bar.setForeground(new Color(255, 80, 80));
        bar.repaint();
        label.repaint();
    }

    // ── Stats Menu ───────────────────────────────────────────────

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
        title.setForeground(Color.WHITE);
        title.setFont(loadFont(17));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        p.add(makeBar("Hunger",    stats.getHunger()));    p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Happiness", stats.getHappiness())); p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Energy",    stats.getEnergy()));    p.add(Box.createVerticalStrut(10));

        JLabel coins = new JLabel("🪙 Coins: " + stats.getCoins());
        coins.setForeground(new Color(255, 215, 0));
        coins.setFont(loadFont(10));
        coins.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(coins);
        p.add(Box.createVerticalStrut(10));

        addButton(p, "⬅ Back", () -> cardLayout.show(container, "menu"));

        return p;
    }

    // ── Shared UI ────────────────────────────────────────────────

    private JPanel createBasePanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 40, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
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
        l.setForeground(Color.WHITE);
        l.setFont(loadFont(12));
        return l;
    }

    private JProgressBar makeProgressBar(int value) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(Math.max(0, Math.min(100, value)));
        bar.setStringPainted(false);
        bar.setBackground(new Color(60, 60, 60));
        if (value >= 70)      bar.setForeground(new Color(100, 200, 100));
        else if (value >= 30) bar.setForeground(new Color(255, 200, 50));
        else                  bar.setForeground(new Color(255, 80, 80));
        return bar;
    }

    private JPanel wrapBar(JLabel label, JProgressBar bar) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(WIDTH - 30, 30));
        p.add(label, BorderLayout.NORTH);
        p.add(bar,   BorderLayout.CENTER);
        return p;
    }

    private JPanel makeBar(String label, int value) {
        JLabel l = styledLabel(label + ": " + value + "%");
        return wrapBar(l, makeProgressBar(value));
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
                Color bg = getModel().isPressed()  ? new Color(80,80,80)
                        : getModel().isRollover() ? new Color(100,100,100)
                        : new Color(60,60,60);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(loadFont(13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };

        btn.setFont(loadFont(17));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(WIDTH - 30, 40));
        btn.setMaximumSize(new Dimension(WIDTH - 20, 20));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private static Font loadFont(int fontSize) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("images/Shape Bit.otf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font.deriveFont(Font.PLAIN, fontSize);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, fontSize);
        }
    }

    private void save() { SaveManager.save(stats); }

    private void openRandomGrass() {
        String url = GRASS_URLS[(int)(Math.random() * GRASS_URLS.length)];
        try { Desktop.getDesktop().browse(new java.net.URI(url)); }
        catch (Exception e) { e.printStackTrace(); }
    }
}