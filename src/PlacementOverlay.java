import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class PlacementOverlay extends JDialog {

    private Point picked;

    private PlacementOverlay(Window owner, String message) {
        super(owner);
        setUndecorated(true);
        setModal(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        Rectangle usable = Theme.getUsableScreen();
        setBounds(usable);

        JPanel pane = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0,0,0,60));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // hint box
                int w = Math.min(480, getWidth() - 40);
                int h = 90;
                int x = (getWidth() - w) / 2;
                int y = 30;

                g2.setColor(Theme.BG_MAIN);
                g2.fillRoundRect(x, y, w, h, 10, 10);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(x, y, w, h, 10, 10);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                FontMetrics fm = g2.getFontMetrics();
                int ty = y + 28;

                g2.drawString(message, x + 14, ty);
                g2.setFont(Theme.font(Theme.FONT_SIZE_SMALL));
                g2.drawString("Click anywhere to place. Press ESC to cancel.", x + 14, ty + fm.getHeight() + 6);

                g2.dispose();
            }
        };
        pane.setOpaque(false);
        setContentPane(pane);

        pane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                picked = e.getLocationOnScreen();
                dispose();
            }
        });

        // ESC cancel
        pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        pane.getActionMap().put("esc", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                picked = null;
                dispose();
            }
        });
    }

    public static Point pickPoint(Window owner, String message) {
        PlacementOverlay o = new PlacementOverlay(owner, message);
        o.setVisible(true);
        return o.picked;
    }
}