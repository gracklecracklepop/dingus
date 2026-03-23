import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DrawImage extends JPanel {
    private BufferedImage normalImage;
    private BufferedImage dragImage;
    private BufferedImage currentImage;

    private JPanel menuPanel;
    private JButton menuToggleButton;
    private boolean menuVisible = false;
    private JFrame parentFrame;

    // Constants for sizes
    private static final int PET_WIDTH = 360;
    private static final int PET_HEIGHT = 270;
    private static final int MENU_WIDTH = 150;
    private static final int BUTTON_SIZE = 30;

    public DrawImage(JFrame frame) {
        this.parentFrame = frame;
        //setPreferredSize(new Dimension(PET_WIDTH, PET_HEIGHT));
        setLayout(null);  // Null layout to position button manually

        try {
            normalImage = ImageIO.read(new File("images/catsitting.png"));
            dragImage = ImageIO.read(new File("images/catscruff.jpg"));
            currentImage = normalImage;
        } catch (IOException e) {
            e.printStackTrace();
        }

        createMenuPanel();
        createToggleButton();
    }

    private void createToggleButton() {
        menuToggleButton = new JButton("☰") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(60, 60, 60, 220));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(80, 80, 80, 220));
                } else {
                    g2d.setColor(new Color(50, 50, 50, 200));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Button border
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        menuToggleButton.setBounds(5, 5, BUTTON_SIZE, BUTTON_SIZE);  // Top-left corner
        menuToggleButton.setBorderPainted(false);
        menuToggleButton.setContentAreaFilled(false);
        menuToggleButton.setFocusPainted(false);
        menuToggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        menuToggleButton.addActionListener(e -> toggleMenu());

        // Prevent button clicks from triggering drag
        menuToggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }
        });

        add(menuToggleButton);
    }

    private void createMenuPanel() {
        menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Rounded background
                g2d.setColor(new Color(40, 40, 40, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.dispose();
            }
        };
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("🐱 Menu");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuPanel.add(titleLabel);
        menuPanel.add(Box.createVerticalStrut(10));

        // Menu buttons
        JButton feedButton = createMenuButton("🍖 Feed");
        JButton playButton = createMenuButton("🎾 Play");
        JButton sleepButton = createMenuButton("😴 Sleep");
        JButton settingsButton = createMenuButton("⚙️ Settings");
        JButton exitButton = createMenuButton("❌ Exit");

        // Add action listeners
        feedButton.addActionListener(e -> {
            System.out.println("Feeding pet!");
            // Add your feed logic here
        });

        playButton.addActionListener(e -> {
            System.out.println("Playing with pet!");
            // Add your play logic here
        });

        sleepButton.addActionListener(e -> {
            System.out.println("Pet is sleeping!");
            // Add your sleep logic here
        });

        settingsButton.addActionListener(e -> {
            System.out.println("Opening settings!");
            // Add your settings logic here
        });

        exitButton.addActionListener(e -> System.exit(0));

        menuPanel.add(feedButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(playButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(sleepButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(settingsButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(exitButton);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(80, 80, 80));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(100, 100, 100));
                } else {
                    g2d.setColor(new Color(60, 60, 60));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(MENU_WIDTH - 30, 30));
        button.setMaximumSize(new Dimension(MENU_WIDTH - 30, 30));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public void toggleMenu() {
        menuVisible = !menuVisible;

        if (menuVisible) {
            menuToggleButton.setText("✕");  // Change to close icon

            // Expand frame to include menu
            parentFrame.setSize(PET_WIDTH + MENU_WIDTH, PET_HEIGHT);
            // Move frame left to keep pet in same position
            parentFrame.setLocation(
                    parentFrame.getX() - MENU_WIDTH,
                    parentFrame.getY()
            );
            // Add menu panel
            parentFrame.add(menuPanel, BorderLayout.WEST);
        } else {
            menuToggleButton.setText("☰");  // Change back to menu icon

            // Remove menu panel
            parentFrame.remove(menuPanel);
            // Shrink frame
            parentFrame.setSize(PET_WIDTH, PET_HEIGHT);
            // Move frame right
            parentFrame.setLocation(
                    parentFrame.getX() + MENU_WIDTH,
                    parentFrame.getY()
            );
        }

        parentFrame.revalidate();
        parentFrame.repaint();
    }

    public void setDragging(boolean isDragging) {
        currentImage = isDragging ? dragImage : normalImage;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), this);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Cat");
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setLayout(new BorderLayout());

        DrawImage panel = new DrawImage(frame);
        panel.setOpaque(false);

        final Point[] clickOffset = {null};
        final boolean[] isDragging = {false};

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if click is on the toggle button area
                if (isClickOnButton(e.getPoint())) {
                    return;  // Don't start dragging if clicking button
                }

                clickOffset[0] = e.getPoint();
                isDragging[0] = true;
                panel.setDragging(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging[0]) {
                    isDragging[0] = false;
                    panel.setDragging(false);
                }
            }

            private boolean isClickOnButton(Point p) {
                // Check if point is within the button bounds
                return p.x >= 5 && p.x <= 35 && p.y >= 5 && p.y <= 35;
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging[0] && clickOffset[0] != null) {
                    Point frameLocation = frame.getLocation();
                    frame.setLocation(
                            frameLocation.x + e.getX() - clickOffset[0].x,
                            frameLocation.y + e.getY() - clickOffset[0].y
                    );
                }
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.setSize(360, 270);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width - frame.getWidth(), screenSize.height - frame.getHeight() - 45);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
    }
}