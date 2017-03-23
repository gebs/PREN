package ch.hslu.pren.team8.debugger;

import com.sun.istack.internal.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by gebs on 3/17/17.
 */
public class LogMessageImage extends LogMessageBase implements Serializable {
    static final long serialVersionUID = -7588980448693010399L;
    transient BufferedImage image;
    byte[] imageBytes;
    ImageType imageType;

    public LogMessageImage(LogLevel logLevel,MessageType messageType, BufferedImage image,ImageType imageType){
        super(logLevel,messageType);
        this.image = image;
        this.imageType = imageType;
        this.imageBytes = convertBufferedImagetoByteArray(image);
    }




    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Nullable
    private byte[] convertBufferedImagetoByteArray(BufferedImage img) {
        ByteArrayOutputStream bScrn = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "JPG", bScrn);
            byte[] imgByte = bScrn.toByteArray();
            bScrn.flush();
            bScrn.close();
            return imgByte;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }
}
