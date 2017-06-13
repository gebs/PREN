package ch.hslu.pren.team8.debugger;

import ch.hslu.pren.team8.common.*;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;

/**
 * Created by gebs on 3/17/17.
 */
public class Debugger {

    private static Debugger instance;
    private DebuggerServer server;
    private static int PORT = 6955;

    private Debugger(boolean startDebugger) {

        //server = new DebuggerServer("192.168.43.22","ADI");
        if (startDebugger) {
            log("Debugger started",LogLevel.DEBUG);
            java.awt.EventQueue.invokeLater(new Runnable() {
                DebuggerFinder df;
                boolean found = false;

                @Override
                public void run() {
                    df = new DebuggerFinder(PORT);
                    while (!found) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        if (df.getServer() != null) {
                            server = df.getServer();
                            found = true;
                        } else if (server != null) {
                            found = true;
                        }
                    }
                    try {
                        df.stop();
                        while (!df.canContinue()) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });

        }
    }

    public static Debugger getInstance(boolean startDebugger) {
        if (instance == null)
            instance = new Debugger(startDebugger);

        return instance;
    }

    public void log(String message, LogLevel logLevel) {
        if (server != null) {
            LogMessageText msg = new LogMessageText(logLevel, MessageType.LogMessage, message);
            new DebuggerSender(msg, server, PORT);
        }
        else{
            System.out.println(message);
        }

    }

    public void log(BufferedImage img, ImageType type, LogLevel logLevel) {
        if (server != null) {
            LogMessageImage msg = new LogMessageImage(logLevel, MessageType.ImageMessage, img, type);
            new DebuggerSender(msg, server, PORT);
        }
    }

    public void log(Mat img, ImageType type, LogLevel logLevel) {
        if (img.size().height > 0 && img.size().width > 0) {
            Mat bgr = new Mat();
            if (type == ImageType.ORIGINAL && false) {
                Imgproc.cvtColor(img, bgr, Imgproc.COLOR_BGR2RGB);
            }else{
                bgr = img;
            }
            log(Util.toBufferedImage(bgr), type, logLevel);
        }
    }
}
