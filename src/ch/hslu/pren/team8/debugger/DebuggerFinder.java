package ch.hslu.pren.team8.debugger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by gebs on 3/20/17.
 */
public class DebuggerFinder implements Runnable {

    private static final int BROADCAST_SLEEP_TIME = 1000; //Time in ms to wait for the next broadcast
    private static final int BYTE_DATA_SIZE = 1024; //size of the UDP packets
    private static final String DISCOVER_MESSAGE = "LOF_DISCOVER"; //Message for sending broadcast (same as in Class ServerBroadcast)
    private static final String RESPONSE_MESSAGE = "LOF_RESPONSE"; //Message for receiving broadcast (same as in Class ServerBroadcast)
    private static final String BROADCAST_IP = "255.255.255.255"; //Broadcast IP
    private final int port;

    private byte[] sendData = new byte[BYTE_DATA_SIZE]; //data of the UDP packet
    private InetAddress broadcastIP; //IP variable
    private DatagramPacket sendPacket; //Packet to send
    private DatagramSocket socket; //Datagram socket for UDP communication
    private boolean isRunning = true;
    private boolean canContinue = false;

    private DebuggerServer server = null;

    Thread thread;
    AnswerHandler answer;


    public DebuggerFinder(int port) {
        this.port = port;
        sendData = DISCOVER_MESSAGE.getBytes();
        try {
            broadcastIP = InetAddress.getByName(BROADCAST_IP);
        } catch (UnknownHostException ex) {
            System.out.println("Could not resolve Multicast-IP: " + ex);
        }
        sendPacket = new DatagramPacket(sendData, sendData.length, broadcastIP, this.port);
        if (this.thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * This method handles the Thread. It starts also the a new Thread of the
     * class AnswerHandler and then starts sending broadcasts to the network.
     */
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
            answer = new AnswerHandler();
            new Thread(answer).start();
            while (isRunning) {
                //This loop sends a multicast, update the game list and waits defined time.
                socket.send(sendPacket);
                //System.out.println("Broadcast sent to " + group.getHostAddress());
                Thread.sleep(BROADCAST_SLEEP_TIME);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error while starting socket: " + ex);
        }
        finally {
            socket.close();
            this.canContinue = true;
        }
    }
    public boolean canContinue() {
        return canContinue;
    }
    public void stop() throws InterruptedException {
        answer.stop();
        this.isRunning = false;
    }

    /**
     * This class handles all answers receiving from other clients. The clients
     * are saved in the ArrayList 'games'.
     */
    public class AnswerHandler implements Runnable {
        boolean isRunning = true;
        boolean canContinue = false;
        /**
         * The object runs a thread, which listens to the open datagram socket
         * and safes all answers to the LOF_DISCOVER message.
         */
        @Override
        public void run() {
            byte[] receiveData;
            DatagramPacket receivePacket;
            while (isRunning) {
                canContinue = false;
                receiveData = new byte[BYTE_DATA_SIZE];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    socket.receive(receivePacket);
                    String s = new String(receivePacket.getData());
                    String[] message = s.split(";");
                    String address = receivePacket.getAddress().toString();
                    address = address.substring(address.lastIndexOf("/") + 1);
                    System.out.println(message[0].trim());
                    //Only Messages with the correct answer statement will be saved in the ArrayList games.
                    if (message[0].trim().equals(RESPONSE_MESSAGE.trim())) {
                        //System.out.println("received: " + message[0]);
                        addDebugger(message, address);
                    }
                    canContinue = true;
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Error while receiving UDP Packet: " + ex);
                }
            }
        }
        public void stop() throws InterruptedException {
            isRunning = false;
            while (!canContinue) {
                thread.sleep(1000);
            }
        }
    }


    public synchronized void addDebugger(String[] message, String address) {
        // gameName;gameVersion;sizeX;sizeY;serverIP
        this.server = new DebuggerServer(address,message[1]);
    }

    public DebuggerServer getServer() {
        return server;
    }
}
