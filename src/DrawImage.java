import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DrawImage extends JPanel {
    private BufferedImage image;

    public DrawImage() {
        try {
            image = ImageIO.read(new File("images/pngimg.com - cat_PNG115412.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Cat");
        DrawImage panel = new DrawImage();

        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        panel.setOpaque(false);

        // Track the offset where the user clicked
        final Point[] clickOffset = {null};

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clickOffset[0] = e.getPoint(); // record where on the image you grabbed
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point frameLocation = frame.getLocation();
                frame.setLocation(
                        frameLocation.x + e.getX() - clickOffset[0].x,
                        frameLocation.y + e.getY() - clickOffset[0].y
                );
            }
        });

        frame.add(panel);
        frame.setSize(360, 270);
        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

// Position at bottom right
        frame.setLocation(
                screenSize.width - frame.getWidth(),
                screenSize.height - frame.getHeight()-15
        );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
    }
}