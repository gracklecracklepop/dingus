import com.sun.management.OperatingSystemMXBean;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class SetupWizard extends JDialog {
    private boolean finished = false;
    private PetStats newStats = new PetStats();

    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // RAM UI components declared here so they can be updated when the test actually starts
    private JLabel ramTitle;
    private JLabel ramLiveStats;
    private JProgressBar ramProgressBar;
    private JButton ramNextBtn;

    public SetupWizard() {
        setTitle("First Time Setup");
        setModal(true); // Blocks the rest of the app until finished
        setSize(500, 400); // Slightly larger to fit the custom fonts nicely
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Remove standard Windows borders and remove from the taskbar
        setUndecorated(true);
        setType(Window.Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0)); // Transparent dialog background

        // Main container that draws the rounded dark background
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 40, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add custom Draggable Header
        mainContainer.add(buildHeader(), BorderLayout.NORTH);

        // Setup the card panel (transparent so background shows through)
        cardPanel.setOpaque(false);
        cardPanel.add(buildIntroCard(), "intro");
        cardPanel.add(buildRamCheckCard(), "ram");
        cardPanel.add(buildCustomizationCard(), "custom");

        mainContainer.add(cardPanel, BorderLayout.CENTER);
        add(mainContainer);

        // Start on the intro screen
        cardLayout.show(cardPanel, "intro");
    }

    public boolean isFinished() {
        return finished;
    }

    public PetStats getGeneratedStats() {
        return newStats;
    }

    // --- CUSTOM DRAGGABLE HEADER ---
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("✨ Initial Setup");
        title.setForeground(Color.WHITE);
        title.setFont(loadFont(16));
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = makeButton("X", () -> System.exit(0));
        closeBtn.setPreferredSize(new Dimension(40, 30));
        header.add(closeBtn, BorderLayout.EAST);

        // Make window draggable via the header
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

    // --- STEP 1: The Intro / Warning Prompt ---
    private JPanel buildIntroCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Welcome!", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(loadFont(22));

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
        instructions.setForeground(new Color(200, 200, 200));
        instructions.setFont(loadFont(12));

        JButton startBtn = makeButton("Start Scan", () -> {
            cardLayout.show(cardPanel, "ram");
            runRamTest();
        });
        startBtn.setPreferredSize(new Dimension(200, 40));

        p.add(title, BorderLayout.NORTH);
        p.add(instructions, BorderLayout.CENTER);
        p.add(startBtn, BorderLayout.SOUTH);

        return p;
    }

    // --- STEP 2: The RAM Scan Screen ---
    private JPanel buildRamCheckCard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        ramTitle = new JLabel("Measuring Baseline...", SwingConstants.CENTER);
        ramTitle.setForeground(Color.WHITE);
        ramTitle.setFont(loadFont(16));

        ramLiveStats = new JLabel("Initializing scanner...", SwingConstants.CENTER);
        ramLiveStats.setForeground(new Color(150, 255, 150)); // Hacker green text
        ramLiveStats.setFont(loadFont(12));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        centerPanel.setOpaque(false);

        ramProgressBar = new JProgressBar(0, 100);
        ramProgressBar.setStringPainted(true);
        ramProgressBar.setBackground(new Color(60, 60, 60));
        ramProgressBar.setForeground(new Color(100, 200, 100));
        ramProgressBar.setBorderPainted(false);

        centerPanel.add(ramLiveStats);
        centerPanel.add(ramProgressBar);

        ramNextBtn = makeButton("Next ➡", () -> cardLayout.show(cardPanel, "custom"));
        ramNextBtn.setEnabled(false);

        p.add(ramTitle, BorderLayout.NORTH);
        p.add(centerPanel, BorderLayout.CENTER);
        p.add(ramNextBtn, BorderLayout.SOUTH);

        return p;
    }

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
                ramLiveStats.setText(String.format("Used: %.2f GB / %.2f GB (%.1f%%)", usedRam / 1e9, totalRam / 1e9, usagePct));
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
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField("Dingus");
        styleInput(nameField);

        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male (he/him)", "Female (she/her)", "Non-binary (they/them)"});
        styleInput(genderBox);

        JComboBox<String> colorBox = new JComboBox<>(new String[]{"Default (Orange)", "Void (Black)", "Ghost (White)"});
        styleInput(colorBox);

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
            dispose(); // Close wizard
        });

        // Give the start button a greenish tint
        finishBtn.setBackground(new Color(80, 150, 80));

        p.add(new JLabel()); // Spacer
        p.add(finishBtn);

        return p;
    }

    // --- UTILITIES & STYLING ---

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(loadFont(14));
        return l;
    }

    private void styleInput(JComponent comp) {
        comp.setBackground(new Color(60, 60, 60));
        comp.setForeground(Color.WHITE);
        comp.setFont(loadFont(12));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setCaretColor(Color.WHITE);
            comp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
    }

    private JButton makeButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                if (!isEnabled()) bg = new Color(50, 50, 50);
                else if (getModel().isPressed())  bg = bg.darker();
                else if (getModel().isRollover()) bg = bg.brighter();

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 8);
                g2.setColor(isEnabled() ? Color.WHITE : Color.GRAY);
                g2.setFont(loadFont(14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };

        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
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
            return new Font("Arial", Font.PLAIN, fontSize); // fallback
        }
    }
}