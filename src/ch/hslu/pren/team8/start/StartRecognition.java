package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.JsonHandler;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.VideoCapture;

import javax.swing.*;

public class StartRecognition {

    private Rect croppingRectangle;
    private JsonHandler jsonHandler;

    /**
     * This method initializes the process of start signal detection
     */
    public void start() {
        System.out.println("Start-Erkennung: GO...");
        jsonHandler = JsonHandler.getInstance();
        generateCroppingRectangle();
    }

    private void generateCroppingRectangle() {
        int x = (int) (long) jsonHandler.getInt("trafficLightDimensions.x");
        int y = (int) (long) jsonHandler.getInt("trafficLightDimensions.y");
        int width = (int) (long) jsonHandler.getInt("trafficLightDimensions.width");
        int height = (int) (long) jsonHandler.getInt("trafficLightDimensions.height");
        croppingRectangle = new Rect(x, y, width, height);
    }

    public void runWithVideo() {
        Mat frame = new Mat();
        VideoCapture capture = new VideoCapture(0);

        JFrame jframe = new JFrame("VideoJFrame");
        jframe.setSize(1500, 1000);

        JLabel vidlabel = new JLabel();
        JPanel vidpanel = new JPanel();
        vidpanel.add(vidlabel);

        jframe.add(vidpanel);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jframe.setVisible(true);
    }

}