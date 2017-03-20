package ch.hslu.pren.team8.debugger;

import ch.hslu.pren.team8.common.*;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

/**
 * Created by gebs on 3/17/17.
 */
public class Debugger {
    private static Debugger instance;

    private Debugger() {

    }

    public static Debugger getInstance() {
        if (instance == null)
            instance = new Debugger();

        return instance;
    }

    public static void log(String message) {
        LogMessageText msg = new LogMessageText();
        msg.setType(MessageType.LogMessage);
        msg.setLogText(message);
        new DebuggerSender(msg);
    }

    public static void log(BufferedImage img, ImageType type) {
        LogMessageImage msg = new LogMessageImage();
        msg.setType(MessageType.ImageMessage);
        msg.setImage(img);
        msg.setImageType(type);
        new DebuggerSender(msg);
    }

    public static void log(Mat img, ImageType type) {
        log(Util.toBufferedImage(img),type);
    }
}
