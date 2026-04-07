import com.sun.management.OperatingSystemMXBean;
import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class SetupWizard extends JDialog {
    private boolean finished = false;
    private PetStats newStats = new PetStats();

    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout);

    // RAM UI components declared here so they can be updated when the test actually starts
    private JLabel ramTitle;
    private JLabel ramLiveStats;
    private JProgressBar ramProgressBar;
    private JButton ramNextBtn;

    public SetupWizard() {
        setTitle("✨ First Time Setup");
        setModal(true); // Blocks the rest of the app until finished
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Add the three steps in order
        container.add(buildIntroCard(), "intro");
        container.add(buildRamCheckCard(), "ram");
        container.add(buildCustomizationCard(), "custom");

        add(container);

        // Start on the intro screen
        cardLayout.show(container, "intro");
    }

    public boolean isFinished() {
        return finished;
    }

    public PetStats getGeneratedStats() {
        return newStats;
    }

    // --- STEP 1: The Intro / Warning Prompt ---
    private JPanel buildIntroCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JTextArea instructions = new JTextArea(
                "Before we create your pet, we need to measure your PC's baseline RAM usage so your pet knows when it's taking up too much space!\n\n" +
                        "To get an accurate measurement:\n" +
                        "1. Close all browsers, games, and heavy background apps.\n" +
                        "2. Let your PC sit at the desktop for a moment.\n\n" +
                        "When you're ready, click Start to begin the 15-second scan."
        );
        instructions.setWrapStyleWord(true);
        instructions.setLineWrap(true);
        instructions.setOpaque(false);
        instructions.setEditable(false);
        instructions.setFocusable(false);
        instructions.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JButton startBtn = new JButton("I'm Ready - Start Scan");
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        startBtn.setBackground(new Color(100, 150, 255));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.setPreferredSize(new Dimension(200, 40));

        startBtn.addActionListener(e -> {
            cardLayout.show(container, "ram");
            runRamTest(); // Execute the RAM test ONLY after clicking this button
        });

        p.add(title, BorderLayout.NORTH);
        p.add(instructions, BorderLayout.CENTER);
        p.add(startBtn, BorderLayout.SOUTH);

        return p;
    }

    // --- STEP 2: The RAM Scan Screen ---
    private JPanel buildRamCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        ramTitle = new JLabel("Establishing System RAM Baseline...", SwingConstants.CENTER);
        ramTitle.setFont(new Font("SansSerif", Font.BOLD, 16));

        ramLiveStats = new JLabel("Initializing scanner...", SwingConstants.CENTER);
        ramLiveStats.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        ramProgressBar = new JProgressBar(0, 100);
        ramProgressBar.setStringPainted(true);

        centerPanel.add(ramLiveStats);
        centerPanel.add(ramProgressBar);

        ramNextBtn = new JButton("Next ➡");
        ramNextBtn.setEnabled(false);
        ramNextBtn.addActionListener(e -> cardLayout.show(container, "custom"));

        p.add(ramTitle, BorderLayout.NORTH);
        p.add(centerPanel, BorderLayout.CENTER);
        p.add(ramNextBtn, BorderLayout.SOUTH);

        return p;
    }

    // The actual background process for the RAM scan
    private void runRamTest() {
        new SwingWorker<Long, Object[]>() {
            @Override
            protected Long doInBackground() throws Exception {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

                int durationSeconds = 15;
                long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
                long nextSampleTime = System.currentTimeMillis() + 1000L;

                List<Long> usageSamples = new ArrayList<>();

                while (System.currentTimeMillis() < endTime) {
                    long totalRam = osBean.getTotalMemorySize();
                    long freeRam  = osBean.getFreeMemorySize();
                    long usedRam  = totalRam - freeRam;
                    double usagePercent = (double) usedRam / totalRam * 100;

                    if (System.currentTimeMillis() >= nextSampleTime) {
                        usageSamples.add(usedRam);
                        nextSampleTime += 1000L;
                    }

                    long elapsed = (durationSeconds * 1000L) - Math.max(0, endTime - System.currentTimeMillis());
                    int progress = (int) ((elapsed * 100) / (durationSeconds * 1000L));

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
                try {
                    long avgRamBytes = get();
                    long avgRamMb = avgRamBytes / (1024 * 1024);

                    newStats.setBaseRam(avgRamMb);

                    ramProgressBar.setValue(100);
                    ramTitle.setText("Baseline complete!");
                    ramLiveStats.setText(String.format("Average Baseline RAM: %.2f GB", avgRamBytes / 1e9));
                    ramNextBtn.setEnabled(true);
                } catch (Exception e) {
                    ramLiveStats.setText("Error measuring RAM.");
                    ramNextBtn.setEnabled(true);
                }
            }
        }.execute();
    }

    // --- STEP 3: Customization ---
    private JPanel buildCustomizationCard() {
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 15));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField("Dingus");
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male (he/him)", "Female (she/her)", "Non-binary (they/them)"});
        JComboBox<String> colorBox = new JComboBox<>(new String[]{"Default (Orange)", "Void (Black)", "Ghost (White)"});

        p.add(new JLabel("Pet Name:"));
        p.add(nameField);

        p.add(new JLabel("Gender:"));
        p.add(genderBox);

        p.add(new JLabel("Sprite Color:"));
        p.add(colorBox);

        p.add(new JLabel()); // Spacer
        p.add(new JLabel()); // Spacer

        JButton finishBtn = new JButton("Start Game!");
        finishBtn.setBackground(new Color(100, 200, 100));
        finishBtn.setForeground(Color.WHITE);
        finishBtn.setFocusPainted(false);

        p.add(new JLabel()); // Spacer
        p.add(finishBtn);

        finishBtn.addActionListener(e -> {
            newStats.setName(nameField.getText());
            newStats.setGender((String) genderBox.getSelectedItem());
            newStats.setSpriteColor((String) colorBox.getSelectedItem());

            newStats.setHunger(100);
            newStats.setHappiness(100);
            newStats.setEnergy(100);
            newStats.setCoins(0);

            finished = true;
            dispose(); // Close wizard
        });

        return p;
    }
}