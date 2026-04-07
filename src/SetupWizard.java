import javax.swing.*;
import java.awt.*;

public class SetupWizard extends JDialog {
    private boolean finished = false;
    private PetStats newStats = new PetStats();

    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout);

    public SetupWizard() {
        setTitle("✨ First Time Setup");
        setModal(true); // Blocks the rest of the app until finished
        setSize(400, 300);
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

        JLabel title = new JLabel("Establishing System Baseline...", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        JButton nextBtn = new JButton("Next ➡");
        nextBtn.setEnabled(false);

        p.add(title, BorderLayout.NORTH);
        p.add(progressBar, BorderLayout.CENTER);
        p.add(nextBtn, BorderLayout.SOUTH);

        // Run the RAM/measuring test in the background so it doesn't freeze the GUI
        new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                // TODO: Replace this simulation with your actual measuring.test() logic
                // If using your existing code:
                // measuring.test();
                // while(!measuring.done) { Thread.sleep(100); }

                for (int i = 0; i <= 100; i += 5) {
                    Thread.sleep(50); // Simulating work
                    setProgress(i);
                }

                // Example of getting real RAM allocation
                long maxRamMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
                newStats.setBaseRam(maxRamMb);
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                progressBar.setValue(getProgress());
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                title.setText("Baseline complete! (" + newStats.getBaseRam() + " MB)");
                nextBtn.setEnabled(true);
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

        // Sprite Color Dropdown
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