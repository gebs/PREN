package ch.hslu.pren.team8.debugger;

import ch.hslu.pren.team8.common.*;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

/**
 * Created by gebs on 3/17/17.
 */
public class Debugger {

    private static Debugger instance;
    private DebuggerServer server;
    private static int PORT = 6955;

    private Debugger() {

        server = new DebuggerServer("127.0.0.1","Localhost");

        /*java.awt.EventQueue.invokeLater(new Runnable() {
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
                    if (df.getServer() != null ) {
                        server = df.getServer();
                        found = true;
                    }else if (server != null) {
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
        });*/


    }

    public static Debugger getInstance() {
        if (instance == null)
            instance = new Debugger();

        return instance;
    }

    public void log(String message, LogLevel logLevel) {
        if (server != null) {
            LogMessageText msg = new LogMessageText(logLevel, MessageType.LogMessage, message);
            new DebuggerSender(msg, server, PORT);
        }
    }

    public void log(BufferedImage img, ImageType type, LogLevel logLevel) {
        if (server != null) {
            LogMessageImage msg = new LogMessageImage(logLevel, MessageType.ImageMessage, img, type);
            new DebuggerSender(msg, server, PORT);
        }
    }

    public void log(Mat img, ImageType type, LogLevel logLevel) {
        log(Util.toBufferedImage(img), type, logLevel);
    }
}
