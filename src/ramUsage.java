import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


    public static void main(String[] args) throws InterruptedException {
        try {
            BufferedImage image = ImageIO.read(new File("src/catsitting.png"));
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

