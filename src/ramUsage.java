import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


    public static void main(String[] args) throws InterruptedException {
        try {
            BufferedImage image = ImageIO.read(new File("src/pngimg.com - cat_PNG115412.png"));
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

