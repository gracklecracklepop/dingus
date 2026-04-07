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

    public SetupWizard() {
        setTitle("✨ First Time Setup");
        setModal(true); // Blocks the rest of the app until finished
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        container.add(buildRamCheckCard(), "ram");
        container.add(buildCustomizationCard(), "custom");

        add(container);
    }

    public boolean isFinished() {
        return finished;
    }

    public PetStats getGeneratedStats() {
        return newStats;
    }

    private JPanel buildRamCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Establishing System RAM Baseline...", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Live stats label for the "hacker" feel
        JLabel liveStats = new JLabel("Preparing to measure...", SwingConstants.CENTER);
        liveStats.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        centerPanel.add(liveStats);
        centerPanel.add(progressBar);

        JButton nextBtn = new JButton("Next ➡");
        nextBtn.setEnabled(false);

        p.add(title, BorderLayout.NORTH);
        p.add(centerPanel, BorderLayout.CENTER);
        p.add(nextBtn, BorderLayout.SOUTH);

        // Run the RAM test in the background
        new SwingWorker<Long, Object[]>() {
            @Override
            protected Long doInBackground() throws Exception {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

                int durationSeconds = 15; // Set to 15 seconds for GUI setup (adjust if you want 30)
                long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
                long nextSampleTime = System.currentTimeMillis() + 1000L;

                List<Long> usageSamples = new ArrayList<>();

                while (System.currentTimeMillis() < endTime) {
                    long totalRam = osBean.getTotalMemorySize();
                    long freeRam  = osBean.getFreeMemorySize();
                    long usedRam  = totalRam - freeRam;
                    double usagePercent = (double) usedRam / totalRam * 100;

                    // Collect one sample per second
                    if (System.currentTimeMillis() >= nextSampleTime) {
                        usageSamples.add(usedRam);
                        nextSampleTime += 1000L;
                    }

                    // Calculate progress bar percentage
                    long elapsed = (durationSeconds * 1000L) - Math.max(0, endTime - System.currentTimeMillis());
                    int progress = (int) ((elapsed * 100) / (durationSeconds * 1000L));

                    // Send live data to the GUI thread
                    publish(new Object[]{progress, usagePercent, usedRam, totalRam});

                    Thread.sleep(100); // 100ms refresh rate for smooth UI
                }

                // Calculate the average RAM used
                long sum = 0;
                for (long sample : usageSamples) {
                    sum += sample;
                }
                return usageSamples.isEmpty() ? 0 : (sum / usageSamples.size());
            }

            @Override
            protected void process(List<Object[]> chunks) {
                // Get the most recent update
                Object[] latest = chunks.get(chunks.size() - 1);
                int progress = (Integer) latest[0];
                double usagePct = (Double) latest[1];
                long usedRam = (Long) latest[2];
                long totalRam = (Long) latest[3];

                progressBar.setValue(progress);
                liveStats.setText(String.format("Used: %.2f GB / %.2f GB (%.1f%%)",
                        usedRam / 1e9, totalRam / 1e9, usagePct));
            }

            @Override
            protected void done() {
                try {
                    long avgRamBytes = get();
                    long avgRamMb = avgRamBytes / (1024 * 1024); // Convert to MB to save in JSON

                    newStats.setBaseRam(avgRamMb); // Saves the baseline to your stats

                    progressBar.setValue(100);
                    title.setText("Baseline complete!");
                    liveStats.setText(String.format("Average Baseline RAM: %.2f GB", avgRamBytes / 1e9));
                    nextBtn.setEnabled(true);
                } catch (Exception e) {
                    liveStats.setText("Error measuring RAM.");
                    nextBtn.setEnabled(true);
                }
            }
        }.execute();

        nextBtn.addActionListener(e -> cardLayout.show(container, "custom"));
        return p;
    }

    private JPanel buildCustomizationCard() {
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 15));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField("Dingus");
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Non-binary", "Robot"});

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

            // Set starting defaults
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