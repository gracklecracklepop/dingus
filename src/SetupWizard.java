import com.sun.management.OperatingSystemMXBean;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class SetupWizard extends JDialog {

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  THEME CONFIGURATION  ██████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── BACKGROUNDS ───────────────────────────────────────────────────────────
    private static final Color BG_MAIN              = new Color(40, 40, 40);      // Main window background
    private static final Color BG_MAIN_TRANSPARENT  = new Color(40, 40, 40, 240); // Main with transparency
    private static final Color BG_INPUT             = new Color(60, 60, 60);      // Text fields, combo boxes
    private static final Color BG_INPUT_BORDER      = new Color(80, 80, 80);      // Input borders
    private static final Color BG_DROPDOWN_ITEM     = new Color(50, 50, 50);      // Dropdown list items
    private static final Color BG_DROPDOWN_SELECTED = new Color(80, 120, 80);     // Dropdown selected item

    // ─── BUTTONS ───────────────────────────────────────────────────────────────
    private static final Color BTN_DEFAULT          = new Color(60, 60, 60);      // Normal button
    private static final Color BTN_DISABLED         = new Color(50, 50, 50);      // Disabled button
    private static final Color BTN_PRIMARY          = new Color(80, 150, 80);     // Start/Finish buttons (green)
    private static final Color BTN_SECONDARY        = new Color(100, 100, 100);   // Skip buttons (gray)
    private static final Color BTN_CLOSE            = new Color(150, 60, 60);     // Close/X button (red)

    // ─── TEXT COLORS ───────────────────────────────────────────────────────────
    private static final Color TEXT_PRIMARY         = new Color(255, 255, 255);   // Main text (white)
    private static final Color TEXT_SECONDARY       = new Color(200, 200, 200);   // Instructions text
    private static final Color TEXT_DISABLED        = new Color(128, 128, 128);   // Disabled text
    private static final Color TEXT_LABEL           = new Color(255, 255, 255);   // Form labels

    // ─── ACCENT COLORS ─────────────────────────────────────────────────────────
    private static final Color ACCENT_RAM           = new Color(150, 255, 150);   // RAM stats text (bright green)
    private static final Color ACCENT_CPU           = new Color(150, 200, 255);   // CPU stats text (light blue)
    private static final Color ACCENT_SUCCESS       = new Color(100, 200, 100);   // Success/complete
    private static final Color ACCENT_ERROR         = new Color(255, 100, 100);   // Error states

    // ─── PROGRESS BARS ─────────────────────────────────────────────────────────
    private static final Color PROGRESS_TRACK       = new Color(60, 60, 60);      // Progress bar background
    private static final Color PROGRESS_RAM         = new Color(100, 200, 100);   // RAM progress fill (green)
    private static final Color PROGRESS_CPU         = new Color(100, 150, 255);   // CPU progress fill (blue)

    // ─── FONTS ─────────────────────────────────────────────────────────────────
    private static final String FONT_PATH           = "images/Shape Bit.otf";     // Custom font file path
    private static final String FONT_FALLBACK       = "Arial";                    // Fallback if custom fails

    private static final int FONT_SIZE_TITLE        = 22;   // Main titles ("Welcome!")
    private static final int FONT_SIZE_HEADING      = 16;   // Section headings
    private static final int FONT_SIZE_BUTTON       = 14;   // Button text
    private static final int FONT_SIZE_LABEL        = 14;   // Form labels
    private static final int FONT_SIZE_BODY         = 12;   // Body text, inputs
    private static final int FONT_SIZE_SMALL        = 10;   // Small text

    // ─── SIZING ────────────────────────────────────────────────────────────────
    private static final int WINDOW_WIDTH           = 500;
    private static final int WINDOW_HEIGHT          = 400;
    private static final int CORNER_RADIUS          = 15;   // Rounded corner radius
    private static final int BUTTON_CORNER_RADIUS   = 7;    // Button corner radius

    // ─── TIMING ────────────────────────────────────────────────────────────────
    private static final int SCAN_DURATION_SECONDS  = 15;   // How long each scan runs

    // ═══════════════════════════════════════════════════════════════════════════
    // ████████████████████████  END THEME CONFIGURATION  ████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════


    // ─── Instance Variables ────────────────────────────────────────────────────
    private boolean finished = false;
    private PetStats newStats = new PetStats();

    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // RAM UI components
    private JLabel ramTitle;
    private JLabel ramLiveStats;
    private JProgressBar ramProgressBar;
    private JButton ramNextBtn;
    private JButton ramSkipBtn;
    private SwingWorker<Long, Object[]> ramWorker;

    // CPU UI components
    private JLabel cpuTitle;
    private JLabel cpuLiveStats;
    private JProgressBar cpuProgressBar;
    private JButton cpuNextBtn;
    private JButton cpuSkipBtn;
    private SwingWorker<Double, Object[]> cpuWorker;

    // ─── Static Initializer for UIManager ──────────────────────────────────────
    static {
        applyUIManagerDefaults();
    }

    private static void applyUIManagerDefaults() {
        // ComboBox colors
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", BG_DROPDOWN_SELECTED);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);
        UIManager.put("ComboBox.disabledBackground", BG_INPUT);
        UIManager.put("ComboBox.disabledForeground", TEXT_DISABLED);

        // TextField colors
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
        UIManager.put("TextField.selectionBackground", BG_DROPDOWN_SELECTED);
        UIManager.put("TextField.selectionForeground", TEXT_PRIMARY);

        // List colors (for dropdown popup)
        UIManager.put("List.background", BG_DROPDOWN_ITEM);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", BG_DROPDOWN_SELECTED);
        UIManager.put("List.selectionForeground", TEXT_PRIMARY);

        // PopupMenu colors
        UIManager.put("PopupMenu.background", BG_DROPDOWN_ITEM);
        UIManager.put("PopupMenu.foreground", TEXT_PRIMARY);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BG_INPUT_BORDER, 1));
    }

    // ─── Constructor ───────────────────────────────────────────────────────────

    public SetupWizard() {
        setTitle("First Time Setup");
        setModal(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_MAIN_TRANSPARENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainContainer.add(buildHeader(), BorderLayout.NORTH);

        cardPanel.setOpaque(false);
        cardPanel.add(buildIntroCard(), "intro");
        cardPanel.add(buildRamCheckCard(), "ram");
        cardPanel.add(buildCpuCheckCard(), "cpu");
        cardPanel.add(buildCustomizationCard(), "custom");

        mainContainer.add(cardPanel, BorderLayout.CENTER);
        add(mainContainer);

        cardLayout.show(cardPanel, "intro");
    }

    public boolean isFinished() {
        return finished;
    }

    public PetStats getGeneratedStats() {
        return newStats;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ██████████████████████████  UI BUILDING METHODS  ██████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("✨ Initial Setup");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(loadFont(FONT_SIZE_HEADING));
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = makeButton("X", () -> System.exit(0));
        closeBtn.setBackground(BTN_CLOSE);
        closeBtn.setPreferredSize(new Dimension(40, 30));
        header.add(closeBtn, BorderLayout.EAST);

        // Draggable header
        Point[] offset = {null};
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                offset[0] = e.getPoint();
            }
        });
        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - offset[0].x, loc.y + e.getY() - offset[0].y);
            }
        });

        return header;
    }

    // ─── Step 1: Intro Card ────────────────────────────────────────────────────

    private JPanel buildIntroCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(loadFont(FONT_SIZE_TITLE));

        JTextArea instructions = new JTextArea(
                "Before we create your pet, we need to measure your PC's baseline RAM and CPU usage " +
                        "so your pet knows when you're working hard!\n\n" +
                        "To get an accurate measurement:\n" +
                        "1. Close all browsers, games, and heavy background apps.\n" +
                        "2. Let your PC sit at the desktop for a moment.\n\n" +
                        "When you're ready, click Start to begin the baseline scans."
        );
        instructions.setWrapStyleWord(true);
        instructions.setLineWrap(true);
        instructions.setOpaque(false);
        instructions.setEditable(false);
        instructions.setFocusable(false);
        instructions.setForeground(TEXT_SECONDARY);
        instructions.setFont(loadFont(FONT_SIZE_BODY));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton startBtn = makeButton("Start Scan", () -> {
            cardLayout.show(cardPanel, "ram");
            runRamTest();
        });
        startBtn.setBackground(BTN_PRIMARY);
        startBtn.setPreferredSize(new Dimension(150, 40));

        JButton skipAllBtn = makeButton("Skip All ⏭", this::skipAllScans);
        skipAllBtn.setBackground(BTN_SECONDARY);
        skipAllBtn.setPreferredSize(new Dimension(120, 40));

        buttonPanel.add(startBtn);
        buttonPanel.add(skipAllBtn);

        p.add(title, BorderLayout.NORTH);
        p.add(instructions, BorderLayout.CENTER);
        p.add(buttonPanel, BorderLayout.SOUTH);

        return p;
    }

    private void skipAllScans() {
        newStats.setBaseRam(0);
        newStats.setBaseCpu(0);
        cardLayout.show(cardPanel, "custom");
    }

    // ─── Step 2: RAM Scan Card ─────────────────────────────────────────────────

    private JPanel buildRamCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        ramTitle = new JLabel("Measuring RAM Baseline...", SwingConstants.CENTER);
        ramTitle.setForeground(TEXT_PRIMARY);
        ramTitle.setFont(loadFont(FONT_SIZE_HEADING));

        ramLiveStats = new JLabel("Initializing scanner...", SwingConstants.CENTER);
        ramLiveStats.setForeground(ACCENT_RAM);
        ramLiveStats.setFont(loadFont(FONT_SIZE_BODY));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        centerPanel.setOpaque(false);

        ramProgressBar = new JProgressBar(0, 100);
        ramProgressBar.setStringPainted(true);
        ramProgressBar.setBackground(PROGRESS_TRACK);
        ramProgressBar.setForeground(PROGRESS_RAM);
        ramProgressBar.setBorderPainted(false);

        centerPanel.add(ramLiveStats);
        centerPanel.add(ramProgressBar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        ramNextBtn = makeButton("Next: CPU Test ➡", () -> {
            cardLayout.show(cardPanel, "cpu");
            runCpuTest();
        });
        ramNextBtn.setEnabled(false);
        ramNextBtn.setPreferredSize(new Dimension(160, 35));

        ramSkipBtn = makeButton("Skip ⏭", () -> {
            if (ramWorker != null) ramWorker.cancel(true);
            newStats.setBaseRam(0);
            cardLayout.show(cardPanel, "cpu");
            runCpuTest();
        });
        ramSkipBtn.setBackground(BTN_SECONDARY);
        ramSkipBtn.setPreferredSize(new Dimension(80, 35));

        buttonPanel.add(ramNextBtn);
        buttonPanel.add(ramSkipBtn);

        p.add(ramTitle, BorderLayout.NORTH);
        p.add(centerPanel, BorderLayout.CENTER);
        p.add(buttonPanel, BorderLayout.SOUTH);

        return p;
    }

    private void runRamTest() {
        ramWorker = new SwingWorker<Long, Object[]>() {
            @Override
            protected Long doInBackground() throws Exception {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

                long endTime = System.currentTimeMillis() + (SCAN_DURATION_SECONDS * 1000L);
                long nextSampleTime = System.currentTimeMillis() + 1000L;

                List<Long> usageSamples = new ArrayList<>();

                while (System.currentTimeMillis() < endTime) {
                    if (isCancelled()) return 0L;

                    long totalRam = osBean.getTotalMemorySize();
                    long freeRam = osBean.getFreeMemorySize();
                    long usedRam = totalRam - freeRam;
                    double usagePercent = (double) usedRam / totalRam * 100;

                    if (System.currentTimeMillis() >= nextSampleTime) {
                        usageSamples.add(usedRam);
                        nextSampleTime += 1000L;
                    }

                    long elapsed = (SCAN_DURATION_SECONDS * 1000L) - Math.max(0, endTime - System.currentTimeMillis());
                    int progress = (int) ((elapsed * 100) / (SCAN_DURATION_SECONDS * 1000L));

                    publish(new Object[]{progress, usagePercent, usedRam, totalRam});
                    Thread.sleep(100);
                }

                long sum = 0;
                for (long sample : usageSamples) {
                    sum += sample;
                }
                return usageSamples.isEmpty() ? 0 : (sum / usageSamples.size());
            }

            @Override
            protected void process(List<Object[]> chunks) {
                if (isCancelled()) return;
                Object[] latest = chunks.get(chunks.size() - 1);
                int progress = (Integer) latest[0];
                double usagePct = (Double) latest[1];
                long usedRam = (Long) latest[2];
                long totalRam = (Long) latest[3];

                ramProgressBar.setValue(progress);
                ramLiveStats.setText(String.format("Used: %.2f GB / %.2f GB (%.1f%%)",
                        usedRam / 1e9, totalRam / 1e9, usagePct));
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    long avgRamBytes = get();
                    long avgRamMb = avgRamBytes / (1024 * 1024);

                    newStats.setBaseRam(avgRamMb);

                    ramProgressBar.setValue(100);
                    ramTitle.setText("RAM Baseline Complete!");
                    ramTitle.setForeground(ACCENT_SUCCESS);
                    ramLiveStats.setText(String.format("Average Baseline RAM: %.2f GB", avgRamBytes / 1e9));
                    ramNextBtn.setEnabled(true);
                    ramSkipBtn.setVisible(false);
                } catch (Exception e) {
                    ramLiveStats.setText("Error measuring RAM.");
                    ramLiveStats.setForeground(ACCENT_ERROR);
                    ramNextBtn.setEnabled(true);
                }
            }
        };
        ramWorker.execute();
    }

    // ─── Step 3: CPU Scan Card ─────────────────────────────────────────────────

    private JPanel buildCpuCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        cpuTitle = new JLabel("Measuring CPU Baseline...", SwingConstants.CENTER);
        cpuTitle.setForeground(TEXT_PRIMARY);
        cpuTitle.setFont(loadFont(FONT_SIZE_HEADING));

        cpuLiveStats = new JLabel("Initializing scanner...", SwingConstants.CENTER);
        cpuLiveStats.setForeground(ACCENT_CPU);
        cpuLiveStats.setFont(loadFont(FONT_SIZE_BODY));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        centerPanel.setOpaque(false);

        cpuProgressBar = new JProgressBar(0, 100);
        cpuProgressBar.setStringPainted(true);
        cpuProgressBar.setBackground(PROGRESS_TRACK);
        cpuProgressBar.setForeground(PROGRESS_CPU);
        cpuProgressBar.setBorderPainted(false);

        centerPanel.add(cpuLiveStats);
        centerPanel.add(cpuProgressBar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        cpuNextBtn = makeButton("Next: Customize ➡", () -> cardLayout.show(cardPanel, "custom"));
        cpuNextBtn.setEnabled(false);
        cpuNextBtn.setPreferredSize(new Dimension(160, 35));

        cpuSkipBtn = makeButton("Skip ⏭", () -> {
            if (cpuWorker != null) cpuWorker.cancel(true);
            newStats.setBaseCpu(0);
            cardLayout.show(cardPanel, "custom");
        });
        cpuSkipBtn.setBackground(BTN_SECONDARY);
        cpuSkipBtn.setPreferredSize(new Dimension(80, 35));

        buttonPanel.add(cpuNextBtn);
        buttonPanel.add(cpuSkipBtn);

        p.add(cpuTitle, BorderLayout.NORTH);
        p.add(centerPanel, BorderLayout.CENTER);
        p.add(buttonPanel, BorderLayout.SOUTH);

        return p;
    }

    private void runCpuTest() {
        cpuProgressBar.setValue(0);
        cpuLiveStats.setText("Initializing scanner...");
        cpuLiveStats.setForeground(ACCENT_CPU);
        cpuTitle.setForeground(TEXT_PRIMARY);
        cpuNextBtn.setEnabled(false);
        cpuSkipBtn.setVisible(true);

        cpuWorker = new SwingWorker<Double, Object[]>() {
            @Override
            protected Double doInBackground() throws Exception {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

                long endTime = System.currentTimeMillis() + (SCAN_DURATION_SECONDS * 1000L);
                long nextSampleTime = System.currentTimeMillis() + 1000L;

                List<Double> usageSamples = new ArrayList<>();

                osBean.getCpuLoad();
                Thread.sleep(200);

                while (System.currentTimeMillis() < endTime) {
                    if (isCancelled()) return 0.0;

                    double cpuLoad = osBean.getCpuLoad();
                    double cpuPercent = cpuLoad * 100;

                    if (System.currentTimeMillis() >= nextSampleTime && cpuLoad >= 0) {
                        usageSamples.add(cpuPercent);
                        nextSampleTime += 1000L;
                    }

                    long elapsed = (SCAN_DURATION_SECONDS * 1000L) - Math.max(0, endTime - System.currentTimeMillis());
                    int progress = (int) ((elapsed * 100) / (SCAN_DURATION_SECONDS * 1000L));

                    int cores = osBean.getAvailableProcessors();
                    publish(new Object[]{progress, cpuPercent, cores});
                    Thread.sleep(100);
                }

                double sum = 0;
                for (double sample : usageSamples) {
                    sum += sample;
                }
                return usageSamples.isEmpty() ? 0 : (sum / usageSamples.size());
            }

            @Override
            protected void process(List<Object[]> chunks) {
                if (isCancelled()) return;
                Object[] latest = chunks.get(chunks.size() - 1);
                int progress = (Integer) latest[0];
                double cpuPct = (Double) latest[1];
                int cores = (Integer) latest[2];

                cpuProgressBar.setValue(progress);
                String cpuBar = generateBar(cpuPct);
                cpuLiveStats.setText(String.format("%s %.1f%% (%d cores)", cpuBar, cpuPct, cores));
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    double avgCpu = get();

                    newStats.setBaseCpu(avgCpu);

                    cpuProgressBar.setValue(100);
                    cpuTitle.setText("CPU Baseline Complete!");
                    cpuTitle.setForeground(ACCENT_SUCCESS);
                    cpuLiveStats.setText(String.format("Average Baseline CPU: %.1f%%", avgCpu));
                    cpuNextBtn.setEnabled(true);
                    cpuSkipBtn.setVisible(false);
                } catch (Exception e) {
                    cpuLiveStats.setText("Error measuring CPU.");
                    cpuLiveStats.setForeground(ACCENT_ERROR);
                    cpuNextBtn.setEnabled(true);
                }
            }
        };
        cpuWorker.execute();
    }

    private String generateBar(double percent) {
        int filled = (int) (percent / 10);
        filled = Math.max(0, Math.min(10, filled));
        return "[" + "█".repeat(filled) + "░".repeat(10 - filled) + "]";
    }

    // ─── Step 4: Customization Card ────────────────────────────────────────────

    private JPanel buildCustomizationCard() {
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 15));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = createStyledTextField("");

        JComboBox<String> genderBox = createStyledComboBox(new String[]{
                "Male (he/him)", "Female (she/her)", "Non-binary (they/them)"
        });

        JComboBox<String> colorBox = createStyledComboBox(new String[]{
                "Default (Orange)", "Void (Black)", "Ghost (White)"
        });

        p.add(styledLabel("Pet Name:"));
        p.add(nameField);

        p.add(styledLabel("Gender:"));
        p.add(genderBox);

        p.add(styledLabel("Sprite Color:"));
        p.add(colorBox);

        p.add(new JLabel()); // Spacer
        p.add(new JLabel()); // Spacer

        JButton finishBtn = makeButton("Start Game!", () -> {
            newStats.setName(nameField.getText());
            newStats.setGender((String) genderBox.getSelectedItem());
            newStats.setSpriteColor((String) colorBox.getSelectedItem());

            newStats.setHunger(100);
            newStats.setHappiness(100);
            newStats.setEnergy(100);
            newStats.setCoins(0);

            finished = true;
            dispose();
        });
        finishBtn.setBackground(BTN_PRIMARY);

        p.add(new JLabel()); // Spacer
        p.add(finishBtn);

        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // █████████████████████████  STYLING HELPER METHODS  ████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_LABEL);
        l.setFont(loadFont(FONT_SIZE_LABEL));
        return l;
    }

    private JTextField createStyledTextField(String defaultText) {
        JTextField field = new JTextField(defaultText) {
            @Override
            protected void paintComponent(Graphics g) {
                // Paint background manually to ensure it shows
                g.setColor(BG_INPUT);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        field.setOpaque(true);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(loadFont(FONT_SIZE_BODY));
        field.setSelectionColor(BG_DROPDOWN_SELECTED);
        field.setSelectedTextColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BG_INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);

        comboBox.setOpaque(true);
        comboBox.setBackground(BG_INPUT);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(loadFont(FONT_SIZE_BODY));
        comboBox.setBorder(BorderFactory.createLineBorder(BG_INPUT_BORDER, 1));

        // Custom UI with styled arrow button
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(BG_INPUT_BORDER);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(TEXT_PRIMARY);
                        FontMetrics fm = g.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth("▼")) / 2;
                        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                        g.drawString("▼", x, y);
                    }
                };
                btn.setBackground(BG_INPUT_BORDER);
                btn.setForeground(TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                return btn;
            }

            @Override
            protected void installDefaults() {
                super.installDefaults();
                LookAndFeel.installProperty(comboBox, "opaque", true);
            }
        });

        // Style the editor (the text display part)
        comboBox.setEditor(new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField editor = new JTextField() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(BG_INPUT);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                editor.setOpaque(true);
                editor.setBackground(BG_INPUT);
                editor.setForeground(TEXT_PRIMARY);
                editor.setCaretColor(TEXT_PRIMARY);
                editor.setFont(loadFont(FONT_SIZE_BODY));
                editor.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                return editor;
            }
        });

        // Style the dropdown list renderer
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setFont(loadFont(FONT_SIZE_BODY));
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                label.setOpaque(true);

                if (index == -1) {
                    // This is the selected item display (not in dropdown)
                    label.setBackground(BG_INPUT);
                    label.setForeground(TEXT_PRIMARY);
                } else if (isSelected) {
                    label.setBackground(BG_DROPDOWN_SELECTED);
                    label.setForeground(TEXT_PRIMARY);
                } else {
                    label.setBackground(BG_DROPDOWN_ITEM);
                    label.setForeground(TEXT_PRIMARY);
                }

                return label;
            }
        });

        return comboBox;
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled()) {
                    bg = BTN_DISABLED;
                } else if (getModel().isPressed()) {
                    bg = bg.darker();
                } else if (getModel().isRollover()) {
                    bg = bg.brighter();
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS + 1);

                g2.setColor(isEnabled() ? TEXT_PRIMARY : TEXT_DISABLED);
                g2.setFont(loadFont(FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };

        btn.setBackground(BTN_DEFAULT);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ███████████████████████████  FONT LOADING  ████████████████████████████████
    // ═══════════════════════════════════════════════════════════════════════════

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
}