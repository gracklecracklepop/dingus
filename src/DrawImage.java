import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawImage extends JPanel {
    private BufferedImage normalImage;
    private BufferedImage dragImage;
    private BufferedImage currentImage;

    private JPanel menuPanel;
    private JButton menuToggleButton;
    private boolean menuVisible = false;
    private JFrame parentFrame;

    // Grass images list
    private List<String> grassImages;
    private Random random;

    // System tray
    private static TrayIcon trayIcon;
    private static SystemTray systemTray;
    private static boolean isVisible = true;

    // Constants for sizes
    private static final int PET_WIDTH = 360;
    private static final int PET_HEIGHT = 270;
    private static final int MENU_WIDTH = 150;
    private static final int BUTTON_SIZE = 30;

    public DrawImage(JFrame frame) {
        this.parentFrame = frame;
        //setPreferredSize(new Dimension(PET_WIDTH, PET_HEIGHT));
        setLayout(null);

        try {
            normalImage = ImageIO.read(new File("images/catsitting.png"));
            dragImage = ImageIO.read(new File("images/catscruff.jpg"));
            currentImage = normalImage;
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeGrassImages();
        createMenuPanel();
        createToggleButton();
    }

    private void initializeGrassImages() {
        random = new Random();
        grassImages = new ArrayList<>();

        // Grass and nature images
        grassImages.add("https://www.thisoldhouse.com/wp-content/uploads/2020/08/iStock_511747120-scaled.jpg");
        grassImages.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSw15kIqSjCMWOaDlOx-rG_3J1J7oXXIRZCoA&s");
        grassImages.add("https://media.istockphoto.com/id/161820827/photo/morning-dew-drops-on-green-leafs.jpg?s=612x612&w=0&k=20&c=qxLfhCiF7yuICQr_Nm9c1ORLl3HDaSk8_F-xs_P5S-w=");
        grassImages.add("https://www.monrovia.com/media/catalog/product/cache/7b381462074fb6871f01f9faa7ed2e11/r/e/rest_2_2_2226.webp");
        grassImages.add("https://plantsexpress.com/cdn/shop/products/Mexican-Feather-Grass-3.jpg?v=1684511963");
        grassImages.add("https://www.marthastewart.com/thmb/BCdVOmdJCZ0v61Qd2wtyqctPbOY=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/most-common-types-grasses-fine-fescue-getty-0423-1711845ef74b4c10a20579384a5eb1be.jpg");
        grassImages.add("https://www.pennington.com/all-products/grass-seed/resources/-/media/Project/OneWeb/Pennington/Images/blog/seed/Your-First-Lawn--Start-Here/lawn_slice.jpg");
        grassImages.add("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Spelt_grass_grown_outdoors._With_a_deeper_green_color_than_wheat.jpg/250px-Spelt_grass_grown_outdoors._With_a_deeper_green_color_than_wheat.jpg");
        grassImages.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcxBAIn7Yzwr8bWSIPPnFhCW4zLE87cNETZQ&s");
        grassImages.add("https://media.istockphoto.com/id/2177658794/photo/home-backyard-with-green-grass-and-sunrays.jpg?s=612x612&w=0&k=20&c=en1jkzChxMkainb7d3hzQoGgLrLrJtMMsH4-okMIY7c=");
        grassImages.add("https://cdn.britannica.com/40/120840-004-BDE9C7D7/Bermuda-grass.jpg");
        grassImages.add("https://cdn.create.vista.com/api/media/small/285879962/stock-photo-selective-focus-trees-green-grass-park-summertime");
    }

    private String getRandomGrassImage() {
        return grassImages.get(random.nextInt(grassImages.size()));
    }

    private void createToggleButton() {
        menuToggleButton = new JButton("☰") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(60, 60, 60, 220));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(80, 80, 80, 220));
                } else {
                    g2d.setColor(new Color(50, 50, 50, 200));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        menuToggleButton.setBounds(5, 5, BUTTON_SIZE, BUTTON_SIZE);
        menuToggleButton.setBorderPainted(false);
        menuToggleButton.setContentAreaFilled(false);
        menuToggleButton.setFocusPainted(false);
        menuToggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuToggleButton.addActionListener(e -> toggleMenu());

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
                g2d.setColor(new Color(40, 40, 40, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("🐱 Menu");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuPanel.add(titleLabel);
        menuPanel.add(Box.createVerticalStrut(10));

        JButton feedButton = createMenuButton("🍖 Feed");
        JButton playButton = createMenuButton("🎾 Play");
        JButton sleepButton = createMenuButton("😴 Sleep");
        JButton settingsButton = createMenuButton("⚙️ Settings");
        JButton hideButton = createMenuButton("👁 Hide");
        JButton exitButton = createMenuButton("❌ Exit");

        feedButton.addActionListener(e -> {
            System.out.println("Feeding pet!");
        });

        playButton.addActionListener(e -> {
            System.out.println("Playing with pet!");
            String randomImage = getRandomGrassImage();
            System.out.println("Opening: " + randomImage);
            openURL(randomImage);
        });

        sleepButton.addActionListener(e -> {
            System.out.println("Pet is sleeping!");
        });

        settingsButton.addActionListener(e -> {
            System.out.println("Opening settings!");
        });

        // Hide to tray button
        hideButton.addActionListener(e -> {
            hideToTray(parentFrame);
        });

        exitButton.addActionListener(e -> {
            // Clean up tray icon before exit
            if (systemTray != null && trayIcon != null) {
                systemTray.remove(trayIcon);
            }
            System.exit(0);
        });

        menuPanel.add(feedButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(playButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(sleepButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(settingsButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(hideButton);
        menuPanel.add(Box.createVerticalStrut(5));
        menuPanel.add(exitButton);
    }

    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            menuToggleButton.setText("✕");
            parentFrame.setSize(PET_WIDTH + MENU_WIDTH, PET_HEIGHT);
            parentFrame.setLocation(parentFrame.getX() - MENU_WIDTH, parentFrame.getY());
            parentFrame.add(menuPanel, BorderLayout.WEST);
        } else {
            menuToggleButton.setText("☰");
            parentFrame.remove(menuPanel);
            parentFrame.setSize(PET_WIDTH, PET_HEIGHT);
            parentFrame.setLocation(parentFrame.getX() + MENU_WIDTH, parentFrame.getY());
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

    // ==================== SYSTEM TRAY METHODS ====================

    /**
     * Load the application icon from file or create a default one
     */
    private static Image loadAppIcon() {
        try {
            // Try to load custom icon from file
            // Place your icon file at "images/icon.png" (recommended size: 64x64 or 128x128)
            File iconFile = new File("images/icon.png");
            if (iconFile.exists()) {
                return ImageIO.read(iconFile);
            }

            // Alternative: try loading the cat image as icon
            File catIcon = new File("images/catsitting.png");
            if (catIcon.exists()) {
                return ImageIO.read(catIcon);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a default icon if no file found
        return createDefaultIcon();
    }

    /**
     * Creates a simple default cat icon programmatically
     */
    private static Image createDefaultIcon() {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a simple cat face
        // Head
        g2d.setColor(new Color(255, 200, 100));
        g2d.fillOval(8, 16, 48, 44);

        // Ears
        int[] earLeftX = {8, 16, 24};
        int[] earLeftY = {24, 4, 24};
        g2d.fillPolygon(earLeftX, earLeftY, 3);

        int[] earRightX = {40, 48, 56};
        int[] earRightY = {24, 4, 24};
        g2d.fillPolygon(earRightX, earRightY, 3);

        // Inner ears
        g2d.setColor(new Color(255, 150, 150));
        int[] innerEarLeftX = {12, 16, 20};
        int[] innerEarLeftY = {22, 10, 22};
        g2d.fillPolygon(innerEarLeftX, innerEarLeftY, 3);

        int[] innerEarRightX = {44, 48, 52};
        int[] innerEarRightY = {22, 10, 22};
        g2d.fillPolygon(innerEarRightX, innerEarRightY, 3);

        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(20, 32, 8, 10);
        g2d.fillOval(36, 32, 8, 10);

        // Eye shine
        g2d.setColor(Color.WHITE);
        g2d.fillOval(22, 34, 3, 3);
        g2d.fillOval(38, 34, 3, 3);

        // Nose
        g2d.setColor(new Color(255, 150, 150));
        int[] noseX = {32, 28, 36};
        int[] noseY = {44, 50, 50};
        g2d.fillPolygon(noseX, noseY, 3);

        // Mouth
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawArc(24, 48, 8, 6, 0, -180);
        g2d.drawArc(32, 48, 8, 6, 0, -180);

        // Whiskers
        g2d.drawLine(8, 44, 20, 46);
        g2d.drawLine(8, 48, 20, 48);
        g2d.drawLine(8, 52, 20, 50);
        g2d.drawLine(56, 44, 44, 46);
        g2d.drawLine(56, 48, 44, 48);
        g2d.drawLine(56, 52, 44, 50);

        g2d.dispose();
        return image;
    }

    /**
     * Setup the system tray with icon and menu
     */
    private static void setupSystemTray(JFrame frame) {
        // Check if system tray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported on this system");
            return;
        }

        systemTray = SystemTray.getSystemTray();
        Image trayImage = loadAppIcon();

        // Scale image for tray (usually 16x16 or 24x24 depending on OS)
        Dimension trayIconSize = systemTray.getTrayIconSize();
        trayImage = trayImage.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);

        // Create popup menu for tray
        PopupMenu popup = new PopupMenu();

        MenuItem showItem = new MenuItem("Show Pet");
        MenuItem hideItem = new MenuItem("Hide Pet");
        MenuItem feedItem = new MenuItem("Feed");
        MenuItem playItem = new MenuItem("Play");
        MenuItem exitItem = new MenuItem("Exit");

        // Show pet
        showItem.addActionListener(e -> {
            showFromTray(frame);
        });

        // Hide pet
        hideItem.addActionListener(e -> {
            hideToTray(frame);
        });

        // Feed action
        feedItem.addActionListener(e -> {
            System.out.println("Feeding pet from tray!");
            // Show a notification
            trayIcon.displayMessage("Pet Cat", "Yummy! Thanks for the food! 🍖", TrayIcon.MessageType.INFO);
        });

        // Play action
        playItem.addActionListener(e -> {
            System.out.println("Playing from tray!");
            trayIcon.displayMessage("Pet Cat", "Let's play! 🎾", TrayIcon.MessageType.INFO);
        });

        // Exit application
        exitItem.addActionListener(e -> {
            systemTray.remove(trayIcon);
            System.exit(0);
        });

        // Add items to popup menu
        popup.add(showItem);
        popup.add(hideItem);
        popup.addSeparator();
        popup.add(feedItem);
        popup.add(playItem);
        popup.addSeparator();
        popup.add(exitItem);

        // Create tray icon
        trayIcon = new TrayIcon(trayImage, "Pet Cat", popup);
        trayIcon.setImageAutoSize(true);

        // Double-click to show/hide
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (isVisible) {
                        hideToTray(frame);
                    } else {
                        showFromTray(frame);
                    }
                }
            }
        });


    }

    /**
     * Hide the frame and show only in system tray
     */
    private static void hideToTray(JFrame frame) {
        frame.setVisible(false);
        isVisible = false;
    }

    /**
     * Show the frame from system tray
     */
    private static void showFromTray(JFrame frame) {
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        isVisible = true;
    }

    // ==================== MAIN METHOD ====================

    public static void main(String[] args) {
        // Use system look and feel for better tray integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Cat");
            frame.setUndecorated(true);
            frame.setBackground(new Color(0, 0, 0, 0));
            frame.setLayout(new BorderLayout());

            // Set custom application icon (replaces Java coffee cup)
            Image appIcon = loadAppIcon();
            frame.setIconImage(appIcon);

            // Also set icon for all windows
            List<Image> icons = new ArrayList<>();
            icons.add(appIcon.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            icons.add(appIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            icons.add(appIcon.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
            icons.add(appIcon.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
            frame.setIconImages(icons);

            DrawImage panel = new DrawImage(frame);
            panel.setOpaque(false);

            final Point[] clickOffset = {null};
            final boolean[] isDragging = {false};

            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isClickOnButton(e.getPoint())) {
                        return;
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

            // Don't exit on close - minimize to tray instead
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    hideToTray(frame);
                }
            });

            // Setup system tray
            setupSystemTray(frame);

            frame.setVisible(true);
            frame.setResizable(false);
            frame.setAlwaysOnTop(true);
        });
    }
}