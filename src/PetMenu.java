import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;

public class PetMenu {

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  THEME CONFIGURATION  ██████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── BACKGROUNDS ───────────────────────────────────────────────────────────
    private static final Color BG_MAIN              = new Color(40, 40, 40);
    private static final Color BG_MAIN_TRANSPARENT  = new Color(40, 40, 40, 240);
    private static final Color BG_INPUT             = new Color(60, 60, 60);
    private static final Color BG_INPUT_BORDER      = new Color(80, 80, 80);
    private static final Color BG_DROPDOWN_SELECTED = new Color(80, 120, 80);

    // ─── BUTTONS ───────────────────────────────────────────────────────────────
    private static final Color BTN_DEFAULT          = new Color(60, 60, 60);
    private static final Color BTN_HOVER            = new Color(100, 100, 100);
    private static final Color BTN_PRESSED          = new Color(80, 80, 80);
    private static final Color BTN_PRIMARY          = new Color(80, 150, 80);
    private static final Color BTN_CLOSE            = new Color(150, 60, 60);

    // ─── TEXT COLORS ───────────────────────────────────────────────────────────
    private static final Color TEXT_PRIMARY         = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY       = new Color(200, 200, 200);
    private static final Color TEXT_DISABLED        = new Color(128, 128, 128);

    // ─── ACCENT COLORS ─────────────────────────────────────────────────────────
    private static final Color ACCENT_COINS         = new Color(255, 215, 0);
    private static final Color ACCENT_SUCCESS       = new Color(100, 200, 100);
    private static final Color ACCENT_ERROR         = new Color(255, 100, 100);

    // ─── PROGRESS BARS ─────────────────────────────────────────────────────────
    private static final Color PROGRESS_TRACK       = new Color(60, 60, 60);
    private static final Color PROGRESS_HIGH        = new Color(100, 200, 100);  // >= 70%
    private static final Color PROGRESS_MED         = new Color(255, 200, 50);   // >= 30%
    private static final Color PROGRESS_LOW         = new Color(255, 80, 80);    // < 30%

    // ─── SHOP ──────────────────────────────────────────────────────────────────
    private static final Color BG_SHOP_MAIN         = new Color(30, 30, 30);
    private static final Color BG_SHOP_HEADER       = new Color(40, 40, 40);
    private static final Color BG_SHOP_SLOT         = new Color(50, 50, 50);
    private static final Color BG_SHOP_SLOT_BORDER  = new Color(70, 70, 70);
    private static final Color BG_SHOP_BUY          = new Color(70, 70, 70);
    private static final Color BG_SHOP_OWNED        = new Color(100, 150, 100);
    private static final Color BG_SHOP_NO_FUNDS     = new Color(150, 50, 50);

    // ─── SCROLLBAR ─────────────────────────────────────────────────────────────
    private static final Color SCROLLBAR_THUMB      = new Color(120, 120, 120, 200);
    private static final Color SCROLLBAR_TRACK      = new Color(0, 0, 0, 0);

    // ─── FONTS ─────────────────────────────────────────────────────────────────
    private static final String FONT_PATH           = "images/Shape Bit.otf";
    private static final String FONT_FALLBACK       = "Arial";

    private static final int FONT_SIZE_TITLE        = 22;
    private static final int FONT_SIZE_HEADING      = 17;
    private static final int FONT_SIZE_BUTTON       = 13;
    private static final int FONT_SIZE_LABEL        = 12;
    private static final int FONT_SIZE_BODY         = 12;
    private static final int FONT_SIZE_SMALL        = 10;

    // ─── SIZING ────────────────────────────────────────────────────────────────
    static final int WIDTH                          = 150;
    private static final int CORNER_RADIUS          = 15;
    private static final int BUTTON_CORNER_RADIUS   = 7;
    private static final int SCROLL_HEIGHT          = 230;
    private static final int SCROLLBAR_WIDTH        = 3;

    // ═══════════════════════════════════════════════════════════════════════════
    // ████████████████████████  END THEME CONFIGURATION  ████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════


    // ─── Instance Variables ────────────────────────────────────────────────────
    private final JPanel panel;
    private final PetStats stats;

    private CardLayout cardLayout;
    private JPanel container;
    private JPanel statsPanel;

    private JProgressBar hungerBar, happinessBar, energyBar;
    private JLabel hungerLabel, happinessLabel, energyLabel;
    private JLabel mainMenuCoinLabel;

    private static final String[] GRASS_URLS = {
            "https://www.thisoldhouse.com/wp-content/uploads/2020/08/iStock_511747120-scaled.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSw15kIqSjCMWOaDlOx-rG_3J1J7oXXIRZCoA&s",
            "https://media.istockphoto.com/id/161820827/photo/morning-dew-drops-on-green-leafs.jpg?s=612x612&w=0&k=20&c=qxLfhCiF7yuICQr_Nm9c1ORLl3HDaSk8_F-xs_P5S-w=",
            "https://www.monrovia.com/media/catalog/product/cache/7b381462074fb6871f01f9faa7ed2e11/r/e/rest_2_2_2226.webp"
    };

    // ─── Font Cache ────────────────────────────────────────────────────────────
    private static Font cachedFont = null;

    private static Font loadFont(int fontSize) {
        if (cachedFont == null) {
            try {
                cachedFont = Font.createFont(Font.TRUETYPE_FONT, new File(FONT_PATH));
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(cachedFont);
            } catch (Exception e) {
                cachedFont = new Font(FONT_FALLBACK, Font.PLAIN, fontSize);
            }
        }
        return cachedFont.deriveFont(Font.PLAIN, (float) fontSize);
    }

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
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_MAIN_TRANSPARENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
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
        title.setForeground(TEXT_PRIMARY);
        title.setFont(loadFont(15));

        mainMenuCoinLabel = new JLabel("🪙 " + stats.getCoins());
        mainMenuCoinLabel.setForeground(ACCENT_COINS);
        mainMenuCoinLabel.setFont(loadFont(FONT_SIZE_LABEL));

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

        addButton(content, "🛒 Shop",     this::openShopWindow);
        addButton(content, "⚙️ Settings", () -> System.out.println("Opening settings!"));
        addButton(content, "👁 Hide",     () -> PetTray.hide(dialog));
        addButton(content, "❌ Exit",     () -> { save(); PetTray.remove(); System.exit(0); });

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(WIDTH, SCROLL_HEIGHT));

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        verticalBar.setUnitIncrement(16);
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = SCROLLBAR_THUMB;
                this.trackColor = SCROLLBAR_TRACK;
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
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {}
        });

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

        // ── Outer container: rounded dark background ──────────────
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_MAIN_TRANSPARENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ── Header: title on left, coins in center, X on right ────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("🛒 Accessory Shop");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(loadFont(FONT_SIZE_HEADING));

        JLabel shopCoinLabel = new JLabel("🪙 " + stats.getCoins());
        shopCoinLabel.setForeground(ACCENT_COINS);
        shopCoinLabel.setFont(loadFont(FONT_SIZE_LABEL));

        JButton closeBtn = makeButton("X", shopDialog::dispose);
        closeBtn.setBackground(BTN_CLOSE);
        closeBtn.setPreferredSize(new Dimension(40, 30));

        header.add(title,        BorderLayout.WEST);
        header.add(shopCoinLabel, BorderLayout.CENTER);
        header.add(closeBtn,     BorderLayout.EAST);

        // Draggable header
        Point[] offset = {null};
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                offset[0] = e.getPoint();
            }
        });
        header.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = shopDialog.getLocation();
                shopDialog.setLocation(loc.x + e.getX() - offset[0].x,
                        loc.y + e.getY() - offset[0].y);
            }
        });

        mainContainer.add(header, BorderLayout.NORTH);

        // ── Grid content ──────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(0, 3, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] sampleIcons = {"🎩", "👓", "🎀", "👑", "🎒", "🧣", "🎧", "🌸", "🕶️", "🧢"};

        for (int i = 0; i < 21; i++) {
            String iconStr = sampleIcons[i % sampleIcons.length];
            int price = (i + 1) * 10;
            String uniqueItemId = "acc_" + i;
            grid.add(createShopSlot(iconStr, price, shopCoinLabel, uniqueItemId));
        }

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        verticalBar.setUnitIncrement(16);
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = SCROLLBAR_THUMB;
                this.trackColor = SCROLLBAR_TRACK;
            }
            @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
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
        });

        mainContainer.add(scrollPane, BorderLayout.CENTER);

        shopDialog.add(mainContainer);
        shopDialog.setVisible(true);
    }

    private JPanel createShopSlot(String icon, int price, JLabel shopCoinLabel, String itemId) {
        JPanel slot = new JPanel(new BorderLayout(0, 5));
        slot.setBackground(BG_SHOP_SLOT);
        slot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BG_SHOP_SLOT_BORDER, 2),
                BorderFactory.createEmptyBorder(10, 5, 5, 5)
        ));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(TEXT_PRIMARY);

        JButton buyBtn = makeButton("", null); // text set below
        buyBtn.setFont(loadFont(FONT_SIZE_BODY));

        if (stats.ownsAccessory(itemId)) {
            buyBtn.setText("Owned");
            buyBtn.setBackground(BG_SHOP_OWNED);
            buyBtn.setEnabled(false);
        } else {
            buyBtn.setText(price + " 🪙");
            buyBtn.setBackground(BG_SHOP_BUY);

            buyBtn.addActionListener(e -> {
                if (stats.getCoins() >= price) {
                    stats.addCoins(-price);
                    stats.addAccessory(itemId);
                    save();

                    shopCoinLabel.setText("🪙 " + stats.getCoins());
                    mainMenuCoinLabel.setText("🪙 " + stats.getCoins());

                    buyBtn.setText("Owned");
                    buyBtn.setBackground(BG_SHOP_OWNED);
                    buyBtn.setEnabled(false);
                } else {
                    buyBtn.setBackground(BG_SHOP_NO_FUNDS);
                    Timer t = new Timer(200, evt -> buyBtn.setBackground(BG_SHOP_BUY));
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
        bar.setForeground(progressColor(value));
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
        title.setForeground(TEXT_PRIMARY);
        title.setFont(loadFont(FONT_SIZE_HEADING));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        p.add(makeBar("Hunger",    stats.getHunger()));    p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Happiness", stats.getHappiness())); p.add(Box.createVerticalStrut(8));
        p.add(makeBar("Energy",    stats.getEnergy()));    p.add(Box.createVerticalStrut(10));

        JLabel coins = new JLabel("🪙 Coins: " + stats.getCoins());
        coins.setForeground(ACCENT_COINS);
        coins.setFont(loadFont(FONT_SIZE_SMALL));
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
                g2.setColor(BG_MAIN_TRANSPARENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
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
        l.setForeground(TEXT_PRIMARY);
        l.setFont(loadFont(FONT_SIZE_LABEL));
        return l;
    }

    private Color progressColor(int value) {
        if (value >= 70) {return PROGRESS_HIGH;}
        else if (value >= 30) {return PROGRESS_MED;}

       else{ return PROGRESS_LOW;}

    }

    private JProgressBar makeProgressBar(int value) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(Math.max(0, Math.min(100, value)));
        bar.setStringPainted(false);
        bar.setBackground(PROGRESS_TRACK);
        bar.setForeground(progressColor(value));
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
                if (!isEnabled()) {
                    bg = new Color(50, 50, 50);
                } else if (getModel().isPressed()) {
                    bg = BTN_PRESSED;
                } else if (getModel().isRollover()) {
                    bg = BTN_HOVER;
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS + 1);
                g2.setColor(isEnabled() ? TEXT_PRIMARY : TEXT_DISABLED);
                g2.setFont(loadFont(FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };

        btn.setBackground(BTN_DEFAULT);
        btn.setForeground(TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(WIDTH - 30, 40));
        btn.setMaximumSize(new Dimension(WIDTH - 20, 20));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }

    private void save() { SaveManager.save(stats); }

    private void openRandomGrass() {
        String url = GRASS_URLS[(int)(Math.random() * GRASS_URLS.length)];
        try { Desktop.getDesktop().browse(new java.net.URI(url)); }
        catch (Exception e) { e.printStackTrace(); }
    }
}