package ch.hslu.pren.team8.debugger;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by gebs on 3/17/17.
 */
public class DebuggerSender implements Runnable {
    private Thread thread;
    private LogMessageBase message;
    private InetAddress serverIP;
    private DebuggerServer server;
    private int serverPort;

    public DebuggerSender(LogMessageBase message, DebuggerServer server,int port) {
        this.message = message;
        this.server = server;
        this.serverPort = port;

        try {
            this.serverIP = InetAddress.getByName(server.getIpAddress());
        } catch (UnknownHostException ex) {
            System.err.println("Error while resolving hostname in Class MessageSender: " + ex);
        }

        if (thread == null)
            thread = new Thread(this);

        thread.start();
    }

    @Override
    public void run() {
        if (message.getClass() == LogMessageText.class) {
            try (Socket server = new Socket(serverIP, serverPort); ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
                out.writeObject(this.message);
                out.flush();
                System.out.println("Sent " + message + " to " + serverIP.getHostAddress());
            } catch (IOException iox) {
                System.err.println("Couldn't send message to Logger Server. Saved Log in local file: " + iox);
            }
        }
        else if (message.getClass() == LogMessageImage.class){
            LogMessageImage imsg = (LogMessageImage)message;
            try (Socket server = new Socket(serverIP, serverPort); ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
                OutputStream os = server.getOutputStream();
                ByteArrayOutputStream bScrn = new ByteArrayOutputStream();
                ImageIO.write(imsg.getImage(),"JPG",bScrn);
                byte[] imgByte = bScrn.toByteArray();
                bScrn.flush();
                bScrn.close();

                out.writeObject(this.message);
                os.write(imgByte);


                System.out.println("Sent " + message + " to " + serverIP.getHostAddress());
            } catch (IOException iox) {
                System.err.println("Couldn't send message to Logger Server. Saved Log in local file: " + iox);
            }

        }
    }
}
