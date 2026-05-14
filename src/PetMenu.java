import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;

public class PetMenu {

    private final JPanel panel;
    private final PetStats stats;
    private final JDialog hostDialog;
    private final Runnable onExternalStatsChanged;

    // ── Hunger gain from system usage ───────────────────────────────
    private static final double RAM_MIB_PER_HUNGER_PER_SEC = 1500.0;
    private static final double CPU_PCT_PER_HUNGER_PER_SEC = 80.0;
    private static final double MAX_HUNGER_GAIN_PER_TICK = 2.0;

    private long lastAutoSaveMs = 0;
    private static final long AUTO_SAVE_INTERVAL_MS = 10_000;

    // ── Poop feature ───────────────────────────────────────────────
    private static final double POOP_HUNGER_THRESHOLD = 70.0;   // requested
    private static final int POOP_MIN_DELAY_SEC = 60;
    private static final int POOP_MAX_DELAY_SEC = 180;
    private static final double POOP_HUNGER_DROP = 10.0;        // requested
    private static final int POOP_HAPPINESS_GAIN = 5;

    private boolean poopNotified = false;

    private CardLayout cardLayout;
    private JPanel container;

    private JProgressBar hungerBar, happinessBar, energyBar;
    private JLabel hungerLabel, happinessLabel, energyLabel;

    private JLabel nameLabel;
    private JLabel mainMenuCoinLabel;

    // ── Feed cooldown ────────────────────────────────────────────
    private static final long FEED_COOLDOWN_MS = 15 * 60 * 1000L;
    private long lastFedTime = 0L;
    private JButton feedButton;
    private Timer cooldownTimer;

    // ── RAM/CPU tracking ─────────────────────────────────────────
    long ramUse;         // used MiB live
    long base;           // baseline MiB for happiness decay
    private javax.swing.Timer usageTimer;

    OperatingSystemMXBean osBean = (OperatingSystemMXBean)
            ManagementFactory.getOperatingSystemMXBean();

    // ── Happiness decay + threshold notifications ────────────────
    private Timer happinessDecayTimer;
    private double happinessDecayCarry = 0.0;
    private boolean happinessWarned = false;
    private boolean energyWarned = false;
    private boolean hungerWarned = false;

    // ── Sleep ────────────────────────────────────────────────────
    private Timer sleepTimer;

    // ── Sleep tuning ─────────────────────────────────────────────
    private static final double SLEEP_HALF_LIFE_MIN = 45.0;
    private static final int MAX_SLEEP_GAIN = 90;

    // ── Touch Grass mini-game ────────────────────────────────────
    private boolean grassGameActive = false;
    private GrassDialog currentGrassTab = null;

    private static final String[] GRASS_URLS = {
            "https://www.thisoldhouse.com/wp-content/uploads/2020/08/iStock_511747120-scaled.jpg",
            "https://media.istockphoto.com/id/161820827/photo/morning-dew-drops-on-green-leafs.jpg",
            "https://www.monrovia.com/media/catalog/product/cache/7b381462074fb6871f01f9faa7ed2e11/r/e/rest_2_2_2226.webp"
    };

    public PetMenu(PetStats stats, JDialog dialog, Runnable onExternalStatsChanged) {
        TrayNotifier.ensureInitialized();

        this.stats = stats;
        this.hostDialog = dialog;
        this.onExternalStatsChanged = onExternalStatsChanged;

        cardLayout = new CardLayout();
        container  = new JPanel(cardLayout);
        container.setOpaque(false);

        container.add(buildMainMenu(), "menu");
        container.add(buildDeadMenu(), "dead");
        this.panel = container;

        if (stats.isDead()) {
            enterDeadStateUI();
            return;
        }

        cooldownTimer = new Timer(1000, e -> refreshFeedButton());
        cooldownTimer.start();

        base = stats.getBaseRam(); // MiB baseline
        startUsageTimer();
        startHappinessDecayTimer();
        maybeNotifyThresholds();
    }

    public JPanel getPanel() { return panel; }

    public void refreshFromStats() {
        if (nameLabel != null) {
            String name = stats.getName();
            if (name == null || name.isBlank()) name = "DINGUS";
            nameLabel.setText(ellipsizeMixed("🐱 " + name, Theme.MENU_WIDTH - 60, 15));
        }
        if (mainMenuCoinLabel != null) mainMenuCoinLabel.setText("🪙 " + stats.getCoins());

        updateLiveStats();
        panel.revalidate();
        panel.repaint();
    }

    // ───────────────────────── RAM+CPU usage hooks ─────────────────────────

    public void usageAdd() {
        com.sun.management.OperatingSystemMXBean os =
                (com.sun.management.OperatingSystemMXBean) osBean;

        long usedMiB  = ramUsage.runRamLiveMode(os);
        double cpuPct = ramUsage.runCpuLiveMode(os);

        ramUse = usedMiB;

        ensureUsageBaselines(usedMiB, cpuPct);

        double baseRam = stats.getBaseRam();   // MiB
        double baseCpu = stats.getBaseCpu();   // %

        // NOTE: deltas can be negative -> hunger can decrease too
        double deltaRam = usedMiB - baseRam;
        double deltaCpu = cpuPct - baseCpu;

        // usage drives hunger up/down
        double hungerDelta =
                (deltaRam / RAM_MIB_PER_HUNGER_PER_SEC) +
                        (deltaCpu / CPU_PCT_PER_HUNGER_PER_SEC);

        hungerDelta = clamp(hungerDelta, -MAX_HUNGER_GAIN_PER_TICK, MAX_HUNGER_GAIN_PER_TICK);

        if (hungerDelta != 0) stats.addHunger(hungerDelta);

        // autosave throttled
        long now = System.currentTimeMillis();
        if (now - lastAutoSaveMs >= AUTO_SAVE_INTERVAL_MS) {
            SaveManager.save(stats);
            lastAutoSaveMs = now;
        }
        System.out.println("RAM " + usedMiB + "MiB (base " + stats.getBaseRam() + ") "
                + "CPU " + String.format("%.1f", cpuPct) + "% (base " + String.format("%.1f", stats.getBaseCpu()) + ") "
                + "hungerDelta=" + String.format("%.3f", hungerDelta)
                + " hunger=" + String.format("%.2f", stats.getHunger()));
        updateLiveStats();
        maybeNotifyThresholds();
    }

    public void startUsageTimer() {
        if (usageTimer != null) usageTimer.stop();
        usageTimer = new javax.swing.Timer(1000, e -> {
            try { usageAdd(); }
            catch (RuntimeException ex) { ex.printStackTrace(); }
        });
        usageTimer.start();
    }

    // ── Happiness decay ──────────────────────────────────────────

    private void startHappinessDecayTimer() {
        if (happinessDecayTimer != null) happinessDecayTimer.stop();

        happinessDecayTimer = new Timer(60_000, e -> {
            double baseLossPerMin = 30.0 / 180.0;
            double delta = Math.max(0, (double) ramUse - (double) base);
            double ramFactor = 1.0 + Math.min(2.0, delta / 1500.0);
            double loss = baseLossPerMin * ramFactor;

            happinessDecayCarry += loss;
            int dec = (int) Math.floor(happinessDecayCarry);
            if (dec > 0) {
                stats.addHappiness(-dec);
                happinessDecayCarry -= dec;
                save();
                updateLiveStats();
                maybeNotifyThresholds();
            }
        });
        happinessDecayTimer.start();
    }

    private void maybeNotifyThresholds() {
        if (stats.getHunger() < 30 && !hungerWarned) {
            TrayNotifier.showNotification("Dingus", "I'm hungry... feed me!", TrayIcon.MessageType.WARNING);
            hungerWarned = true;
        }
        if (stats.getHunger() >= 40) hungerWarned = false;

        if (stats.getHappiness() < 50 && !happinessWarned) {
            TrayNotifier.showNotification("Dingus", "I'm getting sad... play with me soon!", TrayIcon.MessageType.WARNING);
            happinessWarned = true;
        }
        if (stats.getHappiness() >= 55) happinessWarned = false;

        if (stats.getEnergy() < 30 && !energyWarned) {
            TrayNotifier.showNotification("Dingus", "I'm tired... I need sleep.", TrayIcon.MessageType.WARNING);
            energyWarned = true;
        }
        if (stats.getEnergy() >= 40) energyWarned = false;
    }

    // ── Touch Grass mini-game ────────────────────────────────────

    private boolean grassGameShouldContinue() {
        return stats.getHappiness() < 100 && stats.getEnergy() > 0;
    }

    private void startTouchGrassGame() {
        TrayNotifier.ensureInitialized();

        if (stats.getEnergy() <= 30) {
            TrayNotifier.showNotification("Dingus", "I'm too tired to play... let me sleep.", TrayIcon.MessageType.WARNING);
            return;
        }

        if (stats.getHappiness() >= 70) {
            TrayNotifier.showNotification("Dingus", "I'm already super happy! No more play right now.", TrayIcon.MessageType.INFO);
            return;
        }

        grassGameActive = true;
        TrayNotifier.showNotification("Dingus", "Let's play! Drag me onto the grass tab.", TrayIcon.MessageType.INFO);
        openNextGrassTab();
    }

    private void stopTouchGrassGame() {
        grassGameActive = false;
        if (currentGrassTab != null) {
            try { currentGrassTab.dispose(); } catch (Exception ignored) {}
            currentGrassTab = null;
        }
    }

    private void openNextGrassTab() {
        if (!grassGameActive) return;

        if (!grassGameShouldContinue()) {
            grassGameActive = false;
            return;
        }

        if (currentGrassTab != null) {
            try { currentGrassTab.dispose(); } catch (Exception ignored) {}
            currentGrassTab = null;
        }

        try {
            String url = GRASS_URLS[(int)(Math.random() * GRASS_URLS.length)];
            Window grassOwner = (hostDialog.getOwner() instanceof Window w) ? w : null;

            currentGrassTab = new GrassDialog(grassOwner, url, () -> {
                currentGrassTab = null;
                if (grassGameActive) SwingUtilities.invokeLater(this::openNextGrassTab);
            });

            currentGrassTab.setVisible(true);

            SwingUtilities.invokeLater(() -> {
                currentGrassTab.toBack();

                hostDialog.setAlwaysOnTop(true);
                hostDialog.toFront();

                hostDialog.setAlwaysOnTop(false);
                hostDialog.setAlwaysOnTop(true);
                hostDialog.toFront();
            });

        } catch (Exception e) {
            e.printStackTrace();
            grassGameActive = false;
        }
    }

    public void checkGrassDrop(Rectangle petBoundsOnScreen) {
        if (!grassGameActive) return;

        GrassDialog hit = GrassDialog.findIntersecting(petBoundsOnScreen);
        if (hit == null) return;

        stats.addHappiness(30);
        stats.addEnergy(-10);

        if (stats.getHappiness() > 100) stats.setHappiness(100);
        if (stats.getEnergy() < 0) stats.setEnergy(0);

        save();
        updateLiveStats();
        maybeNotifyThresholds();

        TrayNotifier.showNotification("Dingus", "Touched grass!", TrayIcon.MessageType.INFO);

        try { hit.dispose(); } catch (Exception ignored) {}

        if (!grassGameShouldContinue()) stopTouchGrassGame();
    }

    // ── Sleep ───────────────────────────────────────────────────

    private static int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }


    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private void ensureUsageBaselines(long usedMiB, double cpuPct) {
        boolean changed = false;

        if (stats.getBaseRam() <= 0) {
            stats.setBaseRam(usedMiB);
            base = usedMiB;         // keep happiness decay baseline consistent too
            changed = true;
        }
        if (stats.getBaseCpu() <= 0) {
            stats.setBaseCpu(cpuPct);
            changed = true;
        }

        if (changed) SaveManager.save(stats);
    }

    private int computeSleepTargetEnergy(int startEnergy, int minutes) {
        minutes = clampInt(minutes, 1, 180);
        double gain = MAX_SLEEP_GAIN * (1.0 - Math.exp(-minutes / SLEEP_HALF_LIFE_MIN));
        int gainInt = (int) Math.round(gain);
        return clampInt(startEnergy + gainInt, 0, 100);
    }

    private void startSleepPrompt() {
        Integer minutes = SleepDurationDialog.promptMinutes(hostDialog);
        if (minutes == null) return;

        if (stats.getEnergy() >= 100) {
            TrayNotifier.showNotification("Dingus", "I’m not tired right now.", TrayIcon.MessageType.INFO);
            return;
        }

        TrayNotifier.showNotification("Dingus", "Goodnight... see you soon.", TrayIcon.MessageType.INFO);
        startSleepForMinutes(minutes);
    }

    private void startSleepForMinutes(int minutes) {
        if (sleepTimer != null) sleepTimer.stop();

        minutes = clampInt(minutes, 1, 180);

        final int startEnergy  = clampInt(stats.getEnergy(), 0, 100);
        final int targetEnergy = computeSleepTargetEnergy(startEnergy, minutes);
        final int totalSeconds = minutes * 60;

        PetTray.hide(hostDialog);

        final int[] elapsed = {0};
        sleepTimer = new Timer(1000, e -> {
            elapsed[0]++;

            double t = Math.min(1.0, elapsed[0] / (double) totalSeconds);
            int newEnergy = (int) Math.round(startEnergy + (targetEnergy - startEnergy) * t);
            newEnergy = clampInt(newEnergy, 0, 100);

            stats.setEnergy(newEnergy);
            SaveManager.save(stats);

            if (elapsed[0] >= totalSeconds) {
                sleepTimer.stop();

                stats.setEnergy(targetEnergy);
                SaveManager.save(stats);

                PetTray.show(hostDialog);
                TrayNotifier.showNotification("Dingus", "I woke up! Energy: " + targetEnergy + "%.", TrayIcon.MessageType.INFO);
                maybeNotifyThresholds();
            }
        });

        sleepTimer.start();
    }

    // ── Feed cooldown helpers ────────────────────────────────────

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

    // ── Dead menu ────────────────────────────────────────────────

    private JPanel buildDeadMenu() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "");
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(buildTitleBarLayer(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(18, 14, 14, 14));

        JLabel msg = new JLabel(stats.pronounSubject() + " died :(", SwingConstants.CENTER);
        msg.setForeground(Theme.TEXT_PRIMARY);
        msg.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        body.add(msg, BorderLayout.CENTER);

        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    private void enterDeadStateUI() {
        try { if (cooldownTimer != null) cooldownTimer.stop(); } catch (Exception ignored) {}
        try { if (usageTimer != null) usageTimer.stop(); } catch (Exception ignored) {}
        try { if (happinessDecayTimer != null) happinessDecayTimer.stop(); } catch (Exception ignored) {}
        try { if (sleepTimer != null) sleepTimer.stop(); } catch (Exception ignored) {}

        try { imageSaver.stop(); } catch (Exception ignored) {}

        grassGameActive = false;
        try { if (currentGrassTab != null) currentGrassTab.dispose(); } catch (Exception ignored) {}
        currentGrassTab = null;

        cardLayout.show(container, "dead");

        SwingUtilities.invokeLater(() -> {
            Rectangle u = Theme.getUsableScreen();
            hostDialog.setLocation(
                    u.x + (u.width - hostDialog.getWidth()) / 2,
                    u.y + (u.height - hostDialog.getHeight()) / 2
            );
        });

        panel.revalidate();
        panel.repaint();
    }

    private void checkDeathAndHandle() {
        if (stats.isDead()) return;

        boolean deadNow = stats.getHunger() < 20
                && stats.getHappiness() < 20
                && stats.getEnergy() < 20;

        if (!deadNow) return;

        TrayNotifier.showNotification("Dingus", stats.pronounSubject() + " died :(", TrayIcon.MessageType.ERROR);

        stats.setDead(true);
        stats.setName("Dead");
        stats.setCoins(0);
        stats.setHunger(0);
        stats.setHappiness(0);
        stats.setEnergy(0);

        SaveManager.save(stats);
        enterDeadStateUI();

        if (onExternalStatsChanged != null) SwingUtilities.invokeLater(onExternalStatsChanged);
    }

    // ── UI build ────────────────────────────────────────────────

    private JPanel buildMainMenu() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "");
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);

        wrapper.add(buildTitleBarLayer(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        String name = stats.getName();
        if (name == null || name.isBlank()) name = "DINGUS";
        nameLabel = Theme.mixedLabel(ellipsizeMixed("🐱 " + name, Theme.MENU_WIDTH - 60, 15),
                15, Theme.TEXT_PRIMARY);

        JPanel nameRow = new JPanel(new BorderLayout());
        nameRow.setOpaque(false);
        nameRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        nameRow.add(nameLabel, BorderLayout.WEST);
        body.add(nameRow, BorderLayout.NORTH);

        JPanel content = new JPanel() {
            @Override public Dimension getPreferredSize() {
                Insets in = getInsets();
                int w = 0, h = in.top + in.bottom;
                for (Component c : getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension p = c.getPreferredSize();
                    w = Math.max(w, p.width);
                    h += p.height;
                }
                return new Dimension(w + in.left + in.right, h);
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

        feedButton = makeButton("🍖 Feed", () -> {
            if (isFeedOnCooldown()) return;
            stats.addHunger(20);
            stats.addHappiness(5);
            stats.addCoins(5);
            save();
            lastFedTime = Instant.now().toEpochMilli();
            refreshFeedButton();
            updateLiveStats();
            maybeNotifyThresholds();
        });
        content.add(feedButton);
        refreshFeedButton();

        addButton(content, "🎾 Play", this::startTouchGrassGame);
        addButton(content, "😴 Sleep", this::startSleepPrompt);

        addButton(content, "🛒 Shop", () -> {
            ShopDialog shop = new ShopDialog(hostDialog, hostDialog, stats, () -> {
                refreshFromStats();
                if (onExternalStatsChanged != null) onExternalStatsChanged.run();
            });
            shop.setVisible(true);
        });

        addButton(content, "⚙ Settings", () -> {
            SettingsDialog settings = new SettingsDialog(hostDialog, stats, () -> {
                refreshFromStats();
                if (onExternalStatsChanged != null) onExternalStatsChanged.run();
            });
            settings.setVisible(true);
        });

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 4));
        bar.setUnitIncrement(10);
        bar.setUI(createThinScrollBarUI());

        body.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Titlebar overlay hitboxes + coins ────────────────────────

    private JLayeredPane buildTitleBarLayer() {
        final int x0  = 12, y0 = 9, dot = 10, gap = 6;

        JLayeredPane layer = new JLayeredPane() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(10, Theme.TITLEBAR_HEIGHT);
            }

            @Override public void doLayout() {
                Component red = find("tl_red");
                Component org = find("tl_orange");
                Component grn = find("tl_green");

                if (red != null) red.setBounds(x0, y0, dot, dot);
                if (org != null) org.setBounds(x0 + (dot + gap), y0, dot, dot);
                if (grn != null) grn.setBounds(x0 + 2 * (dot + gap), y0, dot, dot);

                if (mainMenuCoinLabel != null) {
                    int coinsX = x0 + (dot * 3) + (gap * 2) + 6;
                    int coinsY = 5;
                    int w = mixedWidth(mainMenuCoinLabel.getText(), Theme.FONT_SIZE_LABEL) + 6;
                    int h = getFontMetrics(Theme.font(Theme.FONT_SIZE_LABEL)).getHeight() + 2;
                    mainMenuCoinLabel.setBounds(coinsX, coinsY, w, h);
                }
            }

            private Component find(String name) {
                for (Component c : getComponents()) if (name.equals(c.getName())) return c;
                return null;
            }
        };
        layer.setOpaque(false);

        JButton red = makeTrafficHitbox(this::exitApp, "Exit");
        red.setName("tl_red");

        JButton orange = makeTrafficHitbox(() -> PetTray.hide(hostDialog), "Hide");
        orange.setName("tl_orange");

        JButton green = makeTrafficHitbox(null, null);
        green.setName("tl_green");
        green.setEnabled(false);

        layer.add(red, Integer.valueOf(2));
        layer.add(orange, Integer.valueOf(2));
        layer.add(green, Integer.valueOf(2));

        mainMenuCoinLabel = Theme.mixedLabel("🪙 " + stats.getCoins(),
                Theme.FONT_SIZE_LABEL, Theme.ACCENT_COINS);
        layer.add(mainMenuCoinLabel, Integer.valueOf(1));

        return layer;
    }

    private JButton makeTrafficHitbox(Runnable action, String tooltip) {
        JButton b = new JButton() { @Override protected void paintComponent(Graphics g) {} };
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tooltip != null) b.setToolTipText(tooltip);
        if (action != null) b.addActionListener(e -> action.run());
        return b;
    }

    private void exitApp() {
        save();
        PetTray.remove();
        System.exit(0);
    }

    private void updateLiveStats() {
        if (stats.isDead()) return;

        updateBar(hungerBar,    hungerLabel,    "Hunger",    stats.getHunger());
        updateBar(happinessBar, happinessLabel, "Happiness", stats.getHappiness());
        updateBar(energyBar,    energyLabel,    "Energy",    stats.getEnergy());

        if (mainMenuCoinLabel != null) {
            mainMenuCoinLabel.setText("🪙 " + stats.getCoins());
            mainMenuCoinLabel.revalidate();
            mainMenuCoinLabel.repaint();
        }

        panel.repaint();

        checkDeathAndHandle();
        maybePoopWhenHighHunger();
    }

    // ── POOP LOGIC (fixed) ───────────────────────────────────────

    private void maybePoopWhenHighHunger() {
        if (stats.isDead()) {
            imageSaver.stop();
            return;
        }

        double h = stats.getHunger();

        // Hunger not high anymore => stop saver, reset message latch
        if (h <= POOP_HUNGER_THRESHOLD) {
            poopNotified = false;
            if (imageSaver.isRunning()) imageSaver.stop();
            return;
        }

        // Hunger high => ensure saver is running
        if (imageSaver.isRunning()) return;

        boolean started = imageSaver.startRandomSaving(
                "images/poo.png",
                POOP_MIN_DELAY_SEC,
                POOP_MAX_DELAY_SEC,
                (filename) -> SwingUtilities.invokeLater(() -> {
                    // This runs when an image was actually saved.
                    if (!poopNotified) {
                        poopNotified = true;
                        TrayNotifier.showNotification("Dingus", "Dingus pooped on your desktop.", TrayIcon.MessageType.INFO);
                    }

                    stats.addHunger(-POOP_HUNGER_DROP);
                    stats.addHappiness(POOP_HAPPINESS_GAIN);

                    SaveManager.save(stats);

                    updateLiveStats();
                    maybeNotifyThresholds();

                    // Stop saver once we’ve dropped out of the “high hunger” zone
                    if (stats.getHunger() <= POOP_HUNGER_THRESHOLD) {
                        imageSaver.stop();
                    }
                })
        );

        if (!started) {
            // If it failed to start (e.g., poo.png missing), tell the dev in console.
            System.err.println("[PetMenu] imageSaver failed to start (check images/poo.png path).");
        }
    }

    // ── Mixed ellipsis ──────────────────────────────────────────

    private String ellipsizeMixed(String s, int maxWidth, int fontSize) {
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

    private int mixedWidth(String s, int fontSize) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        try { return Theme.mixedStringWidth(g2, s, fontSize); }
        finally { g2.dispose(); }
    }

    private JLabel styledLabel(String text) {
        return Theme.mixedLabel(text, Theme.FONT_SIZE_LABEL, Theme.TEXT_PRIMARY);
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

    private JProgressBar makeProgressBar(int value) {
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override public void updateUI() {
                setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
                    @Override protected void paintDeterminate(Graphics g, JComponent c) {
                        int w = (int)(c.getWidth() * ((double)((JProgressBar)c).getValue() / ((JProgressBar)c).getMaximum()));
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
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    private void updateBar(JProgressBar bar, JLabel label, String name, double value) {
        if (bar == null || label == null) return;
        bar.setValue((int) value);
        double v = Math.round(value * 100.0) / 100.0;
        label.setText(name + ": " + v + "%");
        bar.setForeground(Theme.progressColor((int) value));
        bar.repaint();
        label.repaint();
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
}