package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.JsonHandler;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.ImageType;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.VideoCapture;

public class StartRecognition {

    private Debugger debugger;
    private Rect croppingRectangle;
    private JsonHandler jsonHandler;
    private Detector detector;
    private boolean runCamera = false;

    /**
     * This method initializes the process of start signal detection
     */
    public void start() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        jsonHandler = JsonHandler.getInstance();
        jsonHandler.setJsonFile("pittProperties.json");
        debugger = Debugger.getInstance();
        detector = Detector.getInstance();
        generateCroppingRectangle();
        runVideo();
    }

    /**
     * Reads the cropping properties from a file named pittProperties.json in the users home directory.
     * Based on the dimensions in this json file a Rectangle object is created and stored in an instance variable.
     */
    private void generateCroppingRectangle() {
        int x = (int) (long) jsonHandler.getInt("trafficLightDimensions.x");
        int y = (int) (long) jsonHandler.getInt("trafficLightDimensions.y");
        int width = (int) (long) jsonHandler.getInt("trafficLightDimensions.width");
        int height = (int) (long) jsonHandler.getInt("trafficLightDimensions.height");
        croppingRectangle = new Rect(x, y, width, height);
        debugger.log("Cropping Rect: x:" + x + " y:" + y + " | " + width + "x" + height, LogLevel.INFO);
    }

    private void runVideo() {
        runCamera = true;
        VideoCapture camera = new VideoCapture(0);

        // wait for camera
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Mat frame = new Mat();
        Mat croppedFrame = new Mat();
        while (runCamera) {
            camera.read(frame);
            debugger.log(frame, ImageType.ORIGINAL, LogLevel.DEBUG);

            croppedFrame = frame.submat(croppingRectangle);
            croppedFrame = detector.detect(croppedFrame);

            debugger.log(croppedFrame, ImageType.EDITED, LogLevel.DEBUG);
        }
    }

}