import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class uiTestMenu extends JFrame {

    private JProgressBar happinessBar;
    private JProgressBar hungerBar;
    private JProgressBar enrichmentBar;
    private JProgressBar healthBar;

    private JLabel statusLabel;
    private JLabel nameLabel;

    public uiTestMenu() {
        setupWindow();
        setupComponents();
    }

    private void setupWindow() {
        setTitle("Dingus Stats");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // Don't exit, just hide
        setAlwaysOnTop(true);
        setResizable(false);

        // Position next to main window (you can adjust this)
        setLocationRelativeTo(null);
    }

    private void setupComponents() {
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(45, 45, 45));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        nameLabel = new JLabel("🐱 Dingus");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Status: Happy and healthy!");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(nameLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Separator
        JSeparator separator1 = new JSeparator();
        separator1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator1.setForeground(new Color(80, 80, 80));
        mainPanel.add(separator1);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Stats
        mainPanel.add(createStatPanel("❤️ Health", healthBar = createProgressBar(new Color(46, 204, 113))));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createStatPanel("😊 Happiness", happinessBar = createProgressBar(new Color(255, 215, 0))));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createStatPanel("🍖 Hunger", hungerBar = createProgressBar(new Color(231, 76, 60))));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainPanel.add(createStatPanel("🧠 Enrichment", enrichmentBar = createProgressBar(new Color(52, 152, 219))));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Separator
        JSeparator separator2 = new JSeparator();
        separator2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator2.setForeground(new Color(80, 80, 80));
        mainPanel.add(separator2);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(360, 100));

        JButton feedButton = createActionButton("🍕 Feed", new Color(230, 126, 34));
        JButton playButton = createActionButton("🎾 Play", new Color(155, 89, 182));
        JButton enrichButton = createActionButton("🌐 Explore", new Color(26, 188, 156));
        JButton closeButton = createActionButton("❌ Close", new Color(192, 57, 43));

        // Button actions
        feedButton.addActionListener(e -> {
            // Simulate feeding
            hungerBar.setValue(Math.max(0, hungerBar.getValue() - 15));
            happinessBar.setValue(Math.min(100, happinessBar.getValue() + 5));
            updateStatus();
        });

        playButton.addActionListener(e -> {
            // Simulate playing
            happinessBar.setValue(Math.min(100, happinessBar.getValue() + 10));
            enrichmentBar.setValue(Math.min(100, enrichmentBar.getValue() + 20));
            updateStatus();
        });

        enrichButton.addActionListener(e -> {
            // Open random URL
            boolean success = webUrlGrabber.openURL("https://images.unsplash.com/photo-1533460004989-cef01064af7e?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1yZWxhdGVkfDIzfHx8ZW58MHx8fHx8&fm=jpg&q=60&w=3000");
            if (success) {
                enrichmentBar.setValue(Math.min(100, enrichmentBar.getValue() + 25));
                updateStatus();
            }
        });

        closeButton.addActionListener(e -> setVisible(false));

        buttonPanel.add(feedButton);
        buttonPanel.add(playButton);
        buttonPanel.add(enrichButton);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel);

        add(mainPanel);

        // Initialize with default values
        healthBar.setValue(100);
        happinessBar.setValue(75);
        hungerBar.setValue(20);
        enrichmentBar.setValue(50);
    }

    private JPanel createStatPanel(String label, JProgressBar bar) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(bar.getValue() + "%");
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        valueLabel.setForeground(new Color(200, 200, 200));

        // Update value label when bar changes
        bar.addChangeListener(e -> valueLabel.setText(bar.getValue() + "%"));

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(bar, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);

        return panel;
    }

    private JProgressBar createProgressBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(200, 20));
        bar.setBackground(new Color(60, 60, 60));
        bar.setForeground(color);
        bar.setBorderPainted(false);
        return bar;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        Color darkerColor = bgColor.darker();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(darkerColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void updateStatus() {
        int happiness = happinessBar.getValue();
        int hunger = hungerBar.getValue();
        int health = healthBar.getValue();

        if (health < 20) {
            statusLabel.setText("Status: 💀 Critical condition!");
        } else if (hunger > 80) {
            statusLabel.setText("Status: 🍖 Starving!");
        } else if (happiness < 25) {
            statusLabel.setText("Status: 😢 Very sad...");
        } else if (happiness > 75 && hunger < 30) {
            statusLabel.setText("Status: 😊 Happy and healthy!");
        } else {
            statusLabel.setText("Status: 😐 Doing okay");
        }
    }

    public void refreshStats() {
        // Method to update stats from external Pet object
        // You can expand this later
        updateStatus();
    }
}