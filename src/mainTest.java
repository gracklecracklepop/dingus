import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class mainTest extends JFrame {

    private JLabel catLabel;
    private JButton statsButton;
    private uiTestMenu statsUI;

    // For dragging
    private Point dragOffset;

    public mainTest() {
        setupWindow();
        setupComponents();
        setupDragging();

        // Create stats UI (but don't show yet)
        statsUI = new uiTestMenu();

        setVisible(true);
    }

    private void setupWindow() {
        setTitle("Dingus");
        setSize(250, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setUndecorated(true); // Remove window decorations

        // Make window transparent
        setBackground(new Color(0, 0, 0, 0));

        // Center on screen initially
        setLocationRelativeTo(null);

        // Set layout
        setLayout(new BorderLayout());
    }

    private void setupComponents() {
        // Main panel with transparent background
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());

        // Cat image label
        catLabel = new JLabel();
        catLabel.setHorizontalAlignment(JLabel.CENTER);

        // Try to load cat image
        try {
            ImageIcon catIcon = new ImageIcon("resources/cat.png");
            // Scale if needed
            Image scaledImage = catIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            catLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // If no image, create a simple placeholder
            catLabel.setText("🐱");
            catLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
            catLabel.setForeground(Color.WHITE);
        }

        // Stats button
        statsButton = new JButton("📊 Stats");
        statsButton.setFocusPainted(false);
        statsButton.setFont(new Font("Arial", Font.BOLD, 14));
        statsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleButton(statsButton);

        // Button action
        statsButton.addActionListener(e -> {
            statsUI.setVisible(true);
            statsUI.toFront();
        });

        // Add components
        mainPanel.add(catLabel, BorderLayout.CENTER);
        mainPanel.add(statsButton, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(60, 60, 60, 220));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setOpaque(true);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(80, 80, 80, 220));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60, 220));
            }
        });
    }

    private void setupDragging() {
        // Make window draggable
        catLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }
        });

        catLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point current = e.getLocationOnScreen();
                setLocation(current.x - dragOffset.x, current.y - dragOffset.y);
            }
        });
    }

    public static void main(String[] args) {
        // Enable transparency for Swing
        System.setProperty("sun.java2d.noddraw", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new mainTest();
        });
    }
}