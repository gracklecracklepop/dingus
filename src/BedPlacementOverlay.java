import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class BedPlacementOverlay extends JDialog {

    private final Rectangle screenBounds;
    private final BufferedImage bedImg;
    private final int bedW, bedH;

    private Point hoverScreen = null;
    private Point chosenTopLeft = null;

    private Rectangle previewRect = null;
    private Rectangle pinnedRect = null;

    private final JButton confirmBtn = makeMenuStyleButton("Confirm");
    private final JButton cancelBtn  = makeMenuStyleButton("Cancel");

    private Point candidateTopLeft = null;

    private BedPlacementOverlay(Window owner, BufferedImage bedImg, int bedW, int bedH) {
        super(owner);
        this.screenBounds = getAllScreenBounds();
        this.bedImg = bedImg;
        this.bedW = bedW;
        this.bedH = bedH;

        setUndecorated(true);
        setModal(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
        setBounds(screenBounds);

        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                // Instruction panel
                int boxW = Math.min(560, getWidth() - 30);
                int boxH = 92;
                int boxX = 12;
                int boxY = 12;

                g2.setColor(new Color(Theme.BG_MAIN.getRed(), Theme.BG_MAIN.getGreen(), Theme.BG_MAIN.getBlue(), 240));
                g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                g2.drawString("Place your bed:", boxX + 12, boxY + 28);

                g2.setFont(Theme.font(Theme.FONT_SIZE_SMALL));
                if (pinnedRect == null) {
                    g2.drawString("Move mouse → preview follows. Click to pin. ESC cancels.", boxX + 12, boxY + 50);
                    g2.drawString("The pet will spawn relative to the bed automatically.", boxX + 12, boxY + 70);
                } else {
                    g2.drawString("Pinned. Click Confirm to save (or click elsewhere to reposition).", boxX + 12, boxY + 50);
                    g2.drawString("ESC cancels.", boxX + 12, boxY + 70);
                }

                // Draw preview bed
                Rectangle r = (pinnedRect != null) ? pinnedRect : previewRect;
                if (r != null) {
                    Composite old = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pinnedRect != null ? 0.95f : 0.60f));

                    if (bedImg != null) {
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                        g2.drawImage(bedImg, r.x - screenBounds.x, r.y - screenBounds.y, r.width, r.height, null);
                    } else {
                        g2.setColor(new Color(Theme.BG_INPUT.getRed(), Theme.BG_INPUT.getGreen(), Theme.BG_INPUT.getBlue(), 220));
                        g2.fillRect(r.x - screenBounds.x, r.y - screenBounds.y, r.width, r.height);
                    }

                    g2.setComposite(old);

                    g2.setColor(Theme.BG_INPUT_BORDER);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect(r.x - screenBounds.x, r.y - screenBounds.y, r.width - 1, r.height - 1);
                }

                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        confirmBtn.setVisible(false);
        cancelBtn.setVisible(false);

        confirmBtn.setSize(110, 32);
        cancelBtn.setSize(110, 32);

        root.add(confirmBtn);
        root.add(cancelBtn);

        root.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int y = 12 + 92 + 10;
                confirmBtn.setLocation(12, y);
                cancelBtn.setLocation(12 + 110 + 10, y);
            }
        });

        root.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                hoverScreen = e.getLocationOnScreen();
                previewRect = computeRectCentered(hoverScreen);
                repaint();
            }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { mouseMoved(e); }
        });

        root.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                hoverScreen = e.getLocationOnScreen();
                Rectangle r = computeRectCentered(hoverScreen);
                pinnedRect = r;
                candidateTopLeft = new Point(r.x, r.y);

                confirmBtn.setVisible(true);
                cancelBtn.setVisible(true);
                repaint();
            }
        });

        confirmBtn.addActionListener(e -> {
            if (candidateTopLeft != null) chosenTopLeft = new Point(candidateTopLeft);
            dispose();
        });

        cancelBtn.addActionListener(e -> {
            chosenTopLeft = null;
            dispose();
        });

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        root.getActionMap().put("esc", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                chosenTopLeft = null;
                dispose();
            }
        });
    }

    // SAME style used in hat overlay buttons (menu-style ink outline)
    private JButton makeMenuStyleButton(String text) {
        int btnHeight = 22;
        int totalHeight = 32;

        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), btnHeight,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);

                g2.setColor(Theme.BTN_ACCENT);
                g2.drawRoundRect(0, 0, getWidth() - 1, btnHeight - 1,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);

                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);

                int textWidth = Theme.mixedStringWidth(g2, getText(), Theme.FONT_SIZE_BUTTON);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();

                int x = (getWidth() - textWidth) / 2;
                int y = (btnHeight + fm.getAscent() - fm.getDescent()) / 2;

                Theme.drawMixedString(g2, getText(), x, y, Theme.FONT_SIZE_BUTTON);

                g2.dispose();
            }
        };

        btn.setBackground(Theme.BTN_DEFAULT);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(110, totalHeight));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setRolloverEnabled(true);
        return btn;
    }

    private static Rectangle getAllScreenBounds() {
        Rectangle all = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            all = all.union(gd.getDefaultConfiguration().getBounds());
        }
        return all;
    }

    private Rectangle computeRectCentered(Point mouseScreen) {
        int x = mouseScreen.x - bedW / 2;
        int y = mouseScreen.y - bedH / 2;

        x = Math.max(screenBounds.x, Math.min(screenBounds.x + screenBounds.width - bedW, x));
        y = Math.max(screenBounds.y, Math.min(screenBounds.y + screenBounds.height - bedH, y));

        return new Rectangle(x, y, bedW, bedH);
    }

    public static Point pickBedTopLeft(Window owner, BufferedImage bedPreview, int bedW, int bedH) {
        BedPlacementOverlay o = new BedPlacementOverlay(owner, bedPreview, bedW, bedH);
        o.setVisible(true);
        return o.chosenTopLeft;
    }
}