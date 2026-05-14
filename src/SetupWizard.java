import com.sun.management.OperatingSystemMXBean;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

public class SetupWizard extends JDialog {

    private boolean finished = false;
    private final PetStats newStats = new PetStats();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // IMPORTANT: tutorial must always be shown at least once
    private boolean tutorialSeen = false;

    private JLabel ramTitle, ramLiveStats;
    private JProgressBar ramProgressBar;
    private JButton ramNextBtn, ramSkipBtn;
    private SwingWorker<Long, Object[]> ramWorker;

    private JLabel cpuTitle, cpuLiveStats;
    private JProgressBar cpuProgressBar;
    private JButton cpuNextBtn, cpuSkipBtn;
    private SwingWorker<Double, Object[]> cpuWorker;

    private JLabel bedPosLabel;

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
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "✨ Initial Setup");
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(
                Theme.TITLEBAR_HEIGHT + 10, 15, 15, 15
        ));

        cardPanel.setOpaque(false);
        cardPanel.add(buildIntroCard(),         "intro");
        cardPanel.add(buildTutorialCard(),      "tutorial");
        cardPanel.add(buildRamCheckCard(),      "ram");
        cardPanel.add(buildCpuCheckCard(),      "cpu");
        cardPanel.add(buildCustomizationCard(), "custom");
        cardPanel.add(buildBedPlacementCard(),  "bedplace");

        mainContainer.add(cardPanel, BorderLayout.CENTER);
        add(mainContainer);

        cardLayout.show(cardPanel, "intro");
    }

    @Override public void dispose() {
        try { if (ramWorker != null) ramWorker.cancel(true); } catch (Exception ignored) {}
        try { if (cpuWorker != null) cpuWorker.cancel(true); } catch (Exception ignored) {}
        super.dispose();
    }

    public boolean isFinished() { return finished; }
    public PetStats getGeneratedStats() { return newStats; }

    // ── Intro ───────────────────────────────────────────────────

    private JPanel buildIntroCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_TITLE));

        JTextArea instructions = new JTextArea(
                "Welcome to Dingus!\n\n" +
                        "You’ll see a quick tutorial first (controls + features).\n" +
                        "After that, you can optionally run RAM/CPU baseline scans.\n\n" +
                        "Click Continue to begin."
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

        JButton continueBtn = makeButton("Continue ➡", () -> cardLayout.show(cardPanel, "tutorial"));
        continueBtn.setBackground(Theme.BTN_PRIMARY);
        continueBtn.setPreferredSize(new Dimension(160, 40));

        // NOTE: No “skip scans” from intro — tutorial cannot be skipped
        buttonPanel.add(continueBtn);

        p.add(title, BorderLayout.NORTH);
        p.add(instructions, BorderLayout.CENTER);
        p.add(buttonPanel, BorderLayout.SOUTH);
        return p;
    }

    // ── Tutorial (ALWAYS shown before scans/customization) ──────

    private JPanel buildTutorialCard() {
        JPanel p = new JPanel(new BorderLayout(10, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Hands-on Tutorial", SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        p.add(title, BorderLayout.NORTH);

        JTextArea guide = new JTextArea(
                "GETTING STARTED\n" +
                        "• Drag Dingus to move around your desktop.\n" +
                        "• Use the tray icon to hide/show Dingus.\n\n" +

                        "THE MENU\n" +
                        "• Open the menu with the round button on Dingus.\n" +
                        "• 🍖 Feed: restores Hunger and gives coins (cooldown).\n" +
                        "• 🎾 Play: starts Touch Grass — drag Dingus onto the grass tab to score.\n" +
                        "• 😴 Sleep: choose minutes; Dingus hides and regains Energy over time.\n" +
                        "• 🛒 Shop: buy hats and cosmetics (owned forever).\n" +
                        "• ⚙ Settings: change name/gender/color and rerun baselines.\n\n" +

                        "PETTING (SPARKLES)\n" +
                        "• Move your mouse left↔right across Dingus.\n" +
                        "• Every 5 back-and-forth strokes: +1 Happiness and ✨ sparkles appear.\n\n" +

                        "ACCESSORIES\n" +
                        "• Equip hats you own.\n" +
                        "• Customize hat size + color.\n" +
                        "• Use “Set Hat Position” to place the hat separately for sitting/bed/drag poses.\n\n" +

                        "DEATH (PERMANENT)\n" +
                        "• If Hunger, Happiness, and Energy all drop below 20, Dingus permanently dies.\n" +
                        "• You get one final notification.\n" +
                        "• Future launches show a dead screen.\n\n" +

                        "Next: you can run baseline scans (recommended) or skip them."
        );
        guide.setWrapStyleWord(true);
        guide.setLineWrap(true);
        guide.setOpaque(false);
        guide.setEditable(false);
        guide.setFocusable(false);
        guide.setForeground(Theme.TEXT_SECONDARY);
        guide.setFont(Theme.font(Theme.FONT_SIZE_BODY));

        JScrollPane scroll = new JScrollPane(guide);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(Theme.SCROLLBAR_WIDTH, 0));

        p.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setOpaque(false);

        JButton back = makeButton("⬅ Back", () -> cardLayout.show(cardPanel, "intro"));
        back.setBackground(Theme.BTN_DEFAULT);

        JButton skipScans = makeButton("Skip Scans ⏭", () -> {
            tutorialSeen = true;          // lock tutorial as “seen”
            newStats.setBaseRam(0);
            newStats.setBaseCpu(0);
            cardLayout.show(cardPanel, "custom");
        });
        skipScans.setBackground(Theme.BTN_SECONDARY);

        JButton startScan = makeButton("Start Scan", () -> {
            tutorialSeen = true;          // lock tutorial as “seen”
            cardLayout.show(cardPanel, "ram");
            runRamTest();
        });
        startScan.setBackground(Theme.BTN_PRIMARY);

        buttonPanel.add(back);
        buttonPanel.add(skipScans);
        buttonPanel.add(startScan);

        p.add(buttonPanel, BorderLayout.SOUTH);
        return p;
    }

    // ── RAM Scan ────────────────────────────────────────────────

    private JPanel buildRamCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ramTitle = new JLabel("Measuring RAM Baseline...", SwingConstants.CENTER);
        ramTitle.setForeground(Theme.TEXT_PRIMARY);
        ramTitle.setFont(Theme.font(Theme.FONT_SIZE_HEADING));

        ramLiveStats = new JLabel("Initializing scanner...", SwingConstants.CENTER);
        ramLiveStats.setForeground(Theme.TEXT_PRIMARY);
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

        ramNextBtn = makeButton("Next: CPU ➡", () -> {
            cardLayout.show(cardPanel, "cpu");
            runCpuTest();
        });
        ramNextBtn.setEnabled(false);
        ramNextBtn.setPreferredSize(new Dimension(140, 35));

        ramSkipBtn = makeButton("Skip ⏭", () -> {
            // Tutorial cannot be skipped because RAM card is only reachable from tutorial,
            // but keep this guard anyway:
            if (!tutorialSeen) { cardLayout.show(cardPanel, "tutorial"); return; }

            if (ramWorker != null) ramWorker.cancel(true);
            newStats.setBaseRam(0);
            cardLayout.show(cardPanel, "cpu");
            runCpuTest();
        });
        ramSkipBtn.setBackground(Theme.BTN_SECONDARY);
        ramSkipBtn.setPreferredSize(new Dimension(90, 35));

        btns.add(ramNextBtn);
        btns.add(ramSkipBtn);

        p.add(ramTitle, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private void runRamTest() {
        ramWorker = new SwingWorker<>() {
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
                    publish(new Object[]{
                            (int)((elapsed * 100) / (Theme.SCAN_DURATION_SECONDS * 1000L)), pct, used, total
                    });
                    Thread.sleep(100);
                }

                long sum = 0; for (long s : samples) sum += s;
                return samples.isEmpty() ? 0 : (sum / samples.size());
            }

            @Override protected void process(List<Object[]> chunks) {
                if (isCancelled() || !isDisplayable()) return;
                Object[] l = chunks.get(chunks.size() - 1);
                ramProgressBar.setValue((Integer) l[0]);
                ramLiveStats.setText(String.format("Used: %.2f GB / %.2f GB (%.1f%%)",
                        (Long)l[2]/1e9, (Long)l[3]/1e9, (Double)l[1]));
            }

            @Override protected void done() {
                if (!isDisplayable() || isCancelled()) return;
                try {
                    long avg = get();
                    newStats.setBaseRam(avg / (1024 * 1024));
                    ramProgressBar.setValue(100);
                    ramTitle.setText("RAM Baseline Complete!");
                    ramTitle.setForeground(Theme.ACCENT_SUCCESS);
                    ramLiveStats.setText(String.format("Average Baseline RAM: %.2f GB", avg / 1e9));
                    ramNextBtn.setEnabled(true);
                    ramSkipBtn.setVisible(false);
                } catch (CancellationException ignored) {
                } catch (Exception e) {
                    ramLiveStats.setText("Error measuring RAM.");
                    ramLiveStats.setForeground(Theme.ACCENT_ERROR);
                    ramNextBtn.setEnabled(true);
                }
            }
        };
        ramWorker.execute();
    }

    // ── CPU Scan ────────────────────────────────────────────────

    private JPanel buildCpuCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        cpuNextBtn = makeButton("Next: Customize ➡", () -> {
            if (!tutorialSeen) { cardLayout.show(cardPanel, "tutorial"); return; }
            cardLayout.show(cardPanel, "custom");
        });
        cpuNextBtn.setEnabled(false);
        cpuNextBtn.setPreferredSize(new Dimension(170, 35));

        cpuSkipBtn = makeButton("Skip ⏭", () -> {
            if (!tutorialSeen) { cardLayout.show(cardPanel, "tutorial"); return; }
            if (cpuWorker != null) cpuWorker.cancel(true);
            newStats.setBaseCpu(0);
            cardLayout.show(cardPanel, "custom");
        });
        cpuSkipBtn.setBackground(Theme.BTN_SECONDARY);
        cpuSkipBtn.setPreferredSize(new Dimension(90, 35));

        btns.add(cpuNextBtn);
        btns.add(cpuSkipBtn);

        p.add(cpuTitle, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private void runCpuTest() {
        cpuWorker = new SwingWorker<>() {
            @Override protected Double doInBackground() throws Exception {
                OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long end = System.currentTimeMillis() + (Theme.SCAN_DURATION_SECONDS * 1000L);
                long next = System.currentTimeMillis() + 1000L;
                List<Double> samples = new ArrayList<>();
                os.getCpuLoad(); Thread.sleep(200);

                while (System.currentTimeMillis() < end) {
                    if (isCancelled()) return 0.0;
                    double load = os.getCpuLoad();
                    double pct = load * 100;

                    if (System.currentTimeMillis() >= next && load >= 0) { samples.add(pct); next += 1000L; }

                    long elapsed = (Theme.SCAN_DURATION_SECONDS * 1000L) - Math.max(0, end - System.currentTimeMillis());
                    publish(new Object[]{
                            (int)((elapsed * 100) / (Theme.SCAN_DURATION_SECONDS * 1000L)), pct, os.getAvailableProcessors()
                    });
                    Thread.sleep(100);
                }

                double sum = 0; for (double s : samples) sum += s;
                return samples.isEmpty() ? 0 : (sum / samples.size());
            }

            @Override protected void process(List<Object[]> chunks) {
                if (isCancelled() || !isDisplayable()) return;
                Object[] l = chunks.get(chunks.size() - 1);
                cpuProgressBar.setValue((Integer) l[0]);
                cpuLiveStats.setText(String.format("%s %.1f%% (%d cores)",
                        generateBar((Double)l[1]), (Double)l[1], (Integer)l[2]));
            }

            @Override protected void done() {
                if (!isDisplayable() || isCancelled()) return;
                try {
                    double avg = get();
                    newStats.setBaseCpu(avg);

                    cpuProgressBar.setValue(100);
                    cpuTitle.setText("CPU Baseline Complete!");
                    cpuTitle.setForeground(Theme.ACCENT_SUCCESS);
                    cpuLiveStats.setText(String.format("Average Baseline CPU: %.1f%%", avg));
                    cpuNextBtn.setEnabled(true);
                    cpuSkipBtn.setVisible(false);
                } catch (CancellationException ignored) {
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

    // ── Customization ───────────────────────────────────────────

    private JPanel buildCustomizationCard() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 15));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = createStyledTextField("");
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new LimitFilter(10));

        JComboBox<String> genderBox = createStyledComboBox(new String[]{
                "Male (he/him)", "Female (she/her)", "Non-binary (they/them)"
        });
        JComboBox<String> colorBox = createStyledComboBox(new String[]{
                "Default (Orange)", "Void (Black)", "Ghost (White)"
        });

        p.add(styledLabel("Pet Name (max 10):")); p.add(nameField);
        p.add(styledLabel("Gender:"));           p.add(genderBox);
        p.add(styledLabel("Sprite Color:"));     p.add(colorBox);
        p.add(new JLabel());                     p.add(new JLabel());

        JButton nextBtn = makeButton("Next: Place Bed ➡", () -> {
            if (!tutorialSeen) { cardLayout.show(cardPanel, "tutorial"); return; }

            newStats.setName(nameField.getText());
            newStats.setGender((String) genderBox.getSelectedItem());
            newStats.setSpriteColor((String) colorBox.getSelectedItem());
            newStats.setHunger(70);
            newStats.setHappiness(70);
            newStats.setEnergy(70);

            cardLayout.show(cardPanel, "bedplace");
        });
        nextBtn.setBackground(Theme.BTN_PRIMARY);

        JButton skipPlacement = makeButton("Skip Placement", () -> {
            if (!tutorialSeen) { cardLayout.show(cardPanel, "tutorial"); return; }
            finished = true;
            dispose();
        });
        skipPlacement.setBackground(Theme.BTN_SECONDARY);

        p.add(skipPlacement);
        p.add(nextBtn);
        return p;
    }

    // ── Bed Placement ───────────────────────────────────────────

    private JPanel buildBedPlacementCard() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Place your bed", SwingConstants.CENTER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.font(Theme.FONT_SIZE_HEADING));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel info = new JLabel("Click to preview + confirm bed placement (desktop stays visible).");
        info.setForeground(Theme.TEXT_SECONDARY);
        info.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        bedPosLabel = new JLabel("Bed: not set");
        bedPosLabel.setForeground(Theme.TEXT_PRIMARY);
        bedPosLabel.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        bedPosLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton placeBedBtn = makeButton("Choose Bed Location", () -> {
            BufferedImage bedPreview = loadBedForColor(newStats.getSpriteColor());
            Point topLeft = BedPlacementOverlay.pickBedTopLeft(this, bedPreview, BedDialog.BED_WIDTH, BedDialog.BED_HEIGHT);
            if (topLeft != null) {
                newStats.setBedPos(topLeft.x, topLeft.y);
                bedPosLabel.setText("Bed: (" + topLeft.x + ", " + topLeft.y + ")");
            }
        });
        placeBedBtn.setBackground(Theme.BTN_SECONDARY);
        placeBedBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(info);
        center.add(Box.createVerticalStrut(10));
        center.add(placeBedBtn);
        center.add(Box.createVerticalStrut(10));
        center.add(bedPosLabel);

        p.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        bottom.setOpaque(false);

        JButton back = makeButton("⬅ Back", () -> cardLayout.show(cardPanel, "custom"));
        back.setBackground(Theme.BTN_DEFAULT);

        JButton finishBtn = makeButton("Start Game!", () -> {
            finished = true;
            dispose();
        });
        finishBtn.setBackground(Theme.BTN_PRIMARY);

        bottom.add(back);
        bottom.add(finishBtn);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private BufferedImage loadBedForColor(String spriteColor) {
        try {
            String path = switch (spriteColor) {
                case "Void (Black)"  -> "dingus - Copy/blackbed.png";
                case "Ghost (White)" -> "dingus - Copy/whitebed.png";
                default -> "dingus - Copy/orangebed.png";
            };
            return ImageIO.read(new File(path));
        } catch (Exception e) {
            return null;
        }
    }

    // ── Styling Helpers ─────────────────────────────────────────

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_LABEL);
        l.setFont(Theme.font(Theme.FONT_SIZE_LABEL));
        return l;
    }

    private JTextField createStyledTextField(String defaultText) {
        JTextField field = new JTextField(defaultText);
        field.setOpaque(true);
        field.setBackground(Theme.BG_INPUT);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setFont(Theme.font(Theme.FONT_SIZE_BODY));
        field.setSelectionColor(Theme.BG_DROPDOWN_SELECTED);
        field.setSelectedTextColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2),
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
        cb.setBorder(BorderFactory.createLineBorder(Theme.BG_INPUT_BORDER, 2));

        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("▾");
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                return btn;
            }
        });

        cb.setEditor(new BasicComboBoxEditor() {
            @Override protected JTextField createEditorComponent() {
                JTextField editor = new JTextField();
                editor.setOpaque(true);
                editor.setBackground(Theme.BG_INPUT);
                editor.setForeground(Theme.TEXT_PRIMARY);
                editor.setCaretColor(Theme.TEXT_PRIMARY);
                editor.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                editor.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                return editor;
            }
        });

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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
            @Override public Dimension getPreferredSize() {
                int h = 40;     // wizard buttons are taller
                int padX = 18;

                BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                try {
                    int w = Theme.mixedStringWidth(g2, getText(), Theme.FONT_SIZE_BUTTON);
                    return new Dimension(w + padX * 2, h);
                } finally {
                    g2.dispose();
                }
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS);

                int textW = Theme.mixedStringWidth(g2, getText(), Theme.FONT_SIZE_BUTTON);
                FontMetrics fm = g2.getFontMetrics(Theme.font(Theme.FONT_SIZE_BUTTON));
                int x = (getWidth() - textW) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);
                Theme.drawMixedString(g2, getText(), x, y, Theme.FONT_SIZE_BUTTON);

                g2.dispose();
            }
        };

        btn.setBackground(Theme.BTN_DEFAULT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(e -> action.run());
        return btn;
    }

    private static class LimitFilter extends DocumentFilter {
        private final int max;
        LimitFilter(int max) { this.max = max; }

        @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            if (fb.getDocument().getLength() + string.length() <= max) super.insertString(fb, offset, string, attr);
        }

        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;
            int cur = fb.getDocument().getLength();
            int next = cur - length + text.length();
            if (next <= max) super.replace(fb, offset, length, text, attrs);
        }
    }
}