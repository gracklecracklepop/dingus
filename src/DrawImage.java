import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
        frame.add(panel);
        frame.setSize(360 , 270);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }
}