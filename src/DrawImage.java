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

    public DrawImage() {
        try {
            normalImage = ImageIO.read(new File("images/pngimg.com - cat_PNG115412.png"));
            dragImage = ImageIO.read(new File("images/catscruff.jpg"));
            currentImage = normalImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        DrawImage panel = new DrawImage();

        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        panel.setOpaque(false);

        final Point[] clickOffset = {null};

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clickOffset[0] = e.getPoint();
                panel.setDragging(true);  // Change to drag image
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panel.setDragging(false);  // Change back to normal
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
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width - frame.getWidth(), screenSize.height - frame.getHeight() - 15);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
    }
}