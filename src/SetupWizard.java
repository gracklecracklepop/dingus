import com.sun.management.OperatingSystemMXBean;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class SetupWizard extends JDialog {

    private boolean finished = false;
    private PetStats newStats = new PetStats();

    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    private JLabel ramTitle, ramLiveStats;
    private JProgressBar ramProgressBar;
    private JButton ramNextBtn, ramSkipBtn;
    private SwingWorker<Long, Object[]> ramWorker;

    private JLabel cpuTitle, cpuLiveStats;
    private JProgressBar cpuProgressBar;
    private JButton cpuNextBtn, cpuSkipBtn;
    private SwingWorker<Double, Object[]> cpuWorker;

    static {
        Theme.applyUIManagerDefaults();
    }

    public SetupWizard() {
        setTitle("First Time Setup");
        setModal(true);
        setSize(Theme.WIZARD_WIDTH, Theme.WIZARD_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));

        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_MAIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainContainer.add(buildHeader(), BorderLayout.NORTH);

        cardPanel.setOpaque(false);
        cardPanel.add(buildIntroCard(),        "intro");
        cardPanel.add(buildRamCheckCard(),     "ram");
        cardPanel.add(buildCpuCheckCard(),     "cpu");
        cardPanel.add(buildCustomizationCard(),"custom");

        // Guide card — shown after customization, before the game starts
        GuidePanel guidePanel = new GuidePanel(() -> {
            // "Back" in the guide goes back to customization in the wizard
            cardLayout.show(cardPanel, "custom");
        });
        // Wrap guide in a panel with a "Start Game" button at the bottom
        cardPanel.add(buildGuideWizardCard(guidePanel), "guide");

        mainContainer.add(cardPanel, BorderLayout.CENTER);
        add(mainContainer);
        cardLayout.show(cardPanel, "intro");
    }

    public boolean isFinished() { return finished; }
    public PetStats getGeneratedStats() { return newStats; }

    // ── Header ──────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("✨ Initial Setup");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = makeButton("X", () -> System.exit(0));
        closeBtn.setBackground(Theme.BTN_CLOSE);
        closeBtn.setPreferredSize(new Dimension(40, 30));
        header.add(closeBtn, BorderLayout.EAST);

        Point[] offset = {null};
        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { offset[0] = e.getPoint(); }
        });
        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - offset[0].x, loc.y + e.getY() - offset[0].y);
            }
        });
        return header;
    }

    // ── Step 1: Intro ───────────────────────────────────────────

    private JPanel buildIntroCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_TITLE));

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
        instructions.setForeground(Theme.TEXT_SECONDARY);
        instructions.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton startBtn = makeButton("Start Scan", () -> {
            cardLayout.show(cardPanel, "ram"); runRamTest();
        });
        startBtn.setBackground(Theme.BTN_PRIMARY);
        startBtn.setPreferredSize(new Dimension(150, 40));

        JButton skipAllBtn = makeButton("Skip All ⏭", this::skipAllScans);
        skipAllBtn.setBackground(Theme.BTN_SECONDARY);
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

    // ── Step 2: RAM Scan ────────────────────────────────────────

    private JPanel buildRamCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        ramTitle = new JLabel("Measuring RAM Baseline...", SwingConstants.CENTER);
        ramTitle.setForeground(Theme.TEXT_PRIMARY);
        ramTitle.setFont(Theme.font(Theme.FONT_SIZE_HEADING));

        ramLiveStats = new JLabel("Initializing scanner...", SwingConstants.CENTER);
        ramLiveStats.setForeground(Theme.ACCENT_RAM);
        ramLiveStats.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 10));
        center.setOpaque(false);

        ramProgressBar = new JProgressBar(0, 100);
        ramProgressBar.setStringPainted(true);
        ramProgressBar.setBackground(Theme.PROGRESS_TRACK);
        ramProgressBar.setForeground(Theme.PROGRESS_RAM);
        ramProgressBar.setBorderPainted(false);

        center.add(ramLiveStats);
        center.add(ramProgressBar);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btns.setOpaque(false);

        ramNextBtn = makeButton("Next: CPU Test ➡", () -> {
            cardLayout.show(cardPanel, "cpu"); runCpuTest();
        });
        ramNextBtn.setEnabled(false);
        ramNextBtn.setPreferredSize(new Dimension(160, 35));

        ramSkipBtn = makeButton("Skip ⏭", () -> {
            if (ramWorker != null) ramWorker.cancel(true);
            newStats.setBaseRam(0);
            cardLayout.show(cardPanel, "cpu"); runCpuTest();
        });
        ramSkipBtn.setBackground(Theme.BTN_SECONDARY);
        ramSkipBtn.setPreferredSize(new Dimension(80, 35));

        btns.add(ramNextBtn);
        btns.add(ramSkipBtn);

        p.add(ramTitle, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private void runRamTest() {
        ramWorker = new SwingWorker<Long, Object[]>() {
            @Override protected Long doInBackground() throws Exception {
                OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long end = System.currentTimeMillis() + (Theme.SCAN_DURATION_SECONDS * 1000L);
                long next = System.currentTimeMillis() + 1000L;
                List<Long> samples = new ArrayList<>();

                while (System.currentTimeMillis() < end) {
                    if (isCancelled()) return 0L;
                    long total = os.getTotalMemorySize(), free = os.getFreeMemorySize(), used = total - free;
                    double pct = (double) used / total * 100;
                    if (System.currentTimeMillis() >= next) { samples.add(used); next += 1000L; }
                    long elapsed = (Theme.SCAN_DURATION_SECONDS * 1000L) - Math.max(0, end - System.currentTimeMillis());
                    publish(new Object[]{(int)((elapsed * 100) / (Theme.SCAN_DURATION_SECONDS * 1000L)), pct, used, total});
                    Thread.sleep(100);
                }
                long sum = 0; for (long s : samples) sum += s;
                return samples.isEmpty() ? 0 : (sum / samples.size());
            }
            @Override protected void process(List<Object[]> chunks) {
                if (isCancelled()) return;
                Object[] l = chunks.get(chunks.size() - 1);
                ramProgressBar.setValue((Integer) l[0]);
                ramLiveStats.setText(String.format("Used: %.2f GB / %.2f GB (%.1f%%)",
                        (Long)l[2]/1e9, (Long)l[3]/1e9, (Double)l[1]));
            }
            @Override protected void done() {
                if (isCancelled()) return;
                try {
                    long avg = get(); newStats.setBaseRam(avg / (1024 * 1024));
                    ramProgressBar.setValue(100);
                    ramTitle.setText("RAM Baseline Complete!");
                    ramTitle.setForeground(Theme.ACCENT_SUCCESS);
                    ramLiveStats.setText(String.format("Average Baseline RAM: %.2f GB", avg / 1e9));
                    ramNextBtn.setEnabled(true); ramSkipBtn.setVisible(false);
                } catch (Exception e) {
                    ramLiveStats.setText("Error measuring RAM.");
                    ramLiveStats.setForeground(Theme.ACCENT_ERROR);
                    ramNextBtn.setEnabled(true);
                }
            }
        };
        ramWorker.execute();
    }

    // ── Step 3: CPU Scan ────────────────────────────────────────

    private JPanel buildCpuCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        cpuTitle = new JLabel("Measuring CPU Baseline...", SwingConstants.CENTER);
        cpuTitle.setForeground(Theme.TEXT_PRIMARY);
        cpuTitle.setFont(Theme.font(Theme.FONT_SIZE_HEADING));

        cpuLiveStats = new JLabel("Initializing scanner...", SwingConstants.CENTER);
        cpuLiveStats.setForeground(Theme.ACCENT_CPU);
        cpuLiveStats.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 10));
        center.setOpaque(false);

        cpuProgressBar = new JProgressBar(0, 100);
        cpuProgressBar.setStringPainted(true);
        cpuProgressBar.setBackground(Theme.PROGRESS_TRACK);
        cpuProgressBar.setForeground(Theme.PROGRESS_CPU);
        cpuProgressBar.setBorderPainted(false);

        center.add(cpuLiveStats);
        center.add(cpuProgressBar);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btns.setOpaque(false);

        cpuNextBtn = makeButton("Next: Customize ➡", () -> cardLayout.show(cardPanel, "custom"));
        cpuNextBtn.setEnabled(false);
        cpuNextBtn.setPreferredSize(new Dimension(160, 35));

        cpuSkipBtn = makeButton("Skip ⏭", () -> {
            if (cpuWorker != null) cpuWorker.cancel(true);
            newStats.setBaseCpu(0);
            cardLayout.show(cardPanel, "custom");
        });
        cpuSkipBtn.setBackground(Theme.BTN_SECONDARY);
        cpuSkipBtn.setPreferredSize(new Dimension(80, 35));

        btns.add(cpuNextBtn);
        btns.add(cpuSkipBtn);

        p.add(cpuTitle, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private void runCpuTest() {
        cpuProgressBar.setValue(0);
        cpuLiveStats.setText("Initializing scanner...");
        cpuLiveStats.setForeground(Theme.ACCENT_CPU);
        cpuTitle.setForeground(Theme.TEXT_PRIMARY);
        cpuNextBtn.setEnabled(false);
        cpuSkipBtn.setVisible(true);

        cpuWorker = new SwingWorker<Double, Object[]>() {
            @Override protected Double doInBackground() throws Exception {
                OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long end = System.currentTimeMillis() + (Theme.SCAN_DURATION_SECONDS * 1000L);
                long next = System.currentTimeMillis() + 1000L;
                List<Double> samples = new ArrayList<>();
                os.getCpuLoad(); Thread.sleep(200);

                while (System.currentTimeMillis() < end) {
                    if (isCancelled()) return 0.0;
                    double load = os.getCpuLoad(), pct = load * 100;
                    if (System.currentTimeMillis() >= next && load >= 0) { samples.add(pct); next += 1000L; }
                    long elapsed = (Theme.SCAN_DURATION_SECONDS * 1000L) - Math.max(0, end - System.currentTimeMillis());
                    publish(new Object[]{(int)((elapsed * 100) / (Theme.SCAN_DURATION_SECONDS * 1000L)), pct, os.getAvailableProcessors()});
                    Thread.sleep(100);
                }
                double sum = 0; for (double s : samples) sum += s;
                return samples.isEmpty() ? 0 : (sum / samples.size());
            }
            @Override protected void process(List<Object[]> chunks) {
                if (isCancelled()) return;
                Object[] l = chunks.get(chunks.size() - 1);
                cpuProgressBar.setValue((Integer) l[0]);
                cpuLiveStats.setText(String.format("%s %.1f%% (%d cores)",
                        generateBar((Double)l[1]), (Double)l[1], (Integer)l[2]));
            }
            @Override protected void done() {
                if (isCancelled()) return;
                try {
                    double avg = get(); newStats.setBaseCpu(avg);
                    cpuProgressBar.setValue(100);
                    cpuTitle.setText("CPU Baseline Complete!");
                    cpuTitle.setForeground(Theme.ACCENT_SUCCESS);
                    cpuLiveStats.setText(String.format("Average Baseline CPU: %.1f%%", avg));
                    cpuNextBtn.setEnabled(true); cpuSkipBtn.setVisible(false);
                } catch (Exception e) {
                    cpuLiveStats.setText("Error measuring CPU.");
                    cpuLiveStats.setForeground(Theme.ACCENT_ERROR);
                    cpuNextBtn.setEnabled(true);
                }
            }
        };
        cpuWorker.execute();
    }

    private String generateBar(double pct) {
        int f = Math.max(0, Math.min(10, (int)(pct / 10)));
        return "[" + "█".repeat(f) + "░".repeat(10 - f) + "]";
    }

    // ── Step 4: Customization ───────────────────────────────────

    private JPanel buildCustomizationCard() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 15));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = createStyledTextField("");

        JComboBox<String> genderBox = createStyledComboBox(new String[]{
                "Male (he/him)", "Female (she/her)", "Non-binary (they/them)"
        });
        JComboBox<String> colorBox = createStyledComboBox(new String[]{
                "Default (Orange)", "Void (Black)", "Ghost (White)"
        });

        p.add(styledLabel("Pet Name:"));     p.add(nameField);
        p.add(styledLabel("Gender:"));       p.add(genderBox);
        p.add(styledLabel("Sprite Color:")); p.add(colorBox);
        p.add(new JLabel());                 p.add(new JLabel());

        JButton guideBtn = makeButton("📖 How to Play", () ->
                cardLayout.show(cardPanel, "guide"));
        guideBtn.setBackground(Theme.BTN_SECONDARY);

        JButton finishBtn = makeButton("Start Game!", () -> {
            newStats.setName(nameField.getText());
            newStats.setGender((String) genderBox.getSelectedItem());
            newStats.setSpriteColor((String) colorBox.getSelectedItem());
            newStats.setHunger(100); newStats.setHappiness(100);
            newStats.setEnergy(100); newStats.setCoins(0);
            finished = true; dispose();
        });
        finishBtn.setBackground(Theme.BTN_PRIMARY);

        p.add(guideBtn);
        p.add(finishBtn);
        return p;
    }

    // ── Step 5: Guide (wraps GuidePanel with a finish button) ───

    private JPanel buildGuideWizardCard(GuidePanel guidePanel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(guidePanel, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Styling Helpers ─────────────────────────────────────────

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
                BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 1),
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
        cb.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 1));

        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("▼") {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(Theme.BG_INPUT_BORDER);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Theme.TEXT_PRIMARY);
                        FontMetrics fm = g.getFontMetrics();
                        g.drawString("▼", (getWidth() - fm.stringWidth("▼")) / 2,
                                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    }
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                return btn;
            }
            @Override protected void installDefaults() {
                super.installDefaults();
                LookAndFeel.installProperty(comboBox, "opaque", true);
            }
        });

        cb.setEditor(new BasicComboBoxEditor() {
            @Override protected JTextField createEditorComponent() {
                JTextField editor = new JTextField() {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(Theme.BG_INPUT);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                editor.setOpaque(true);
                editor.setBackground(Theme.BG_INPUT);
                editor.setForeground(Theme.TEXT_PRIMARY);
                editor.setCaretColor(Theme.TEXT_PRIMARY);
                editor.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                editor.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
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
                if (index == -1)             { label.setBackground(Theme.BG_INPUT);             label.setForeground(Theme.TEXT_PRIMARY); }
                else if (isSelected)         { label.setBackground(Theme.BG_DROPDOWN_SELECTED); label.setForeground(Theme.TEXT_PRIMARY); }
                else                         { label.setBackground(Theme.BG_DROPDOWN_ITEM);     label.setForeground(Theme.TEXT_PRIMARY); }
                return label;
            }
        });

        return cb;
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = bg.darker();
                else if (getModel().isRollover()) bg = bg.brighter();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);
                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setBackground(Theme.BTN_DEFAULT);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }
}