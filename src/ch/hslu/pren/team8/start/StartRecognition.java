package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.JsonHandler;
import ch.hslu.pren.team8.common.Util;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.ImageType;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.net.URL;

public class StartRecognition {

    private boolean runCamera = false;
    private boolean runDebugger = false;

    private Debugger debugger;
    private Rect croppingRectangle;
    private JsonHandler jsonHandler;
    private Detector detector;

    private final static int PI_IMAGE_WIDTH = 640;
    private final static int PI_IMAGE_HEIGHT = 480;

    /**
     * This method initializes the process of start signal detection
     */
    public void start() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        jsonHandler = JsonHandler.getInstance();
        jsonHandler.setJsonFile("pittProperties.json");
        detector = Detector.getInstance(runDebugger);

        if (runDebugger) {
            debugger = Debugger.getInstance(runDebugger);
        }

        generateCroppingRectangle();

        if (runCamera) {
            runVideo();
        } else {
            runStatic();
        }
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

        if (x + width > PI_IMAGE_WIDTH) {
            width = PI_IMAGE_WIDTH - x;
        }

        if (y + height > PI_IMAGE_HEIGHT) {
            height = PI_IMAGE_HEIGHT - x;
        }

        String logMessage = "Cropping Rect: x:" + x + " y:" + y + " | " + width + "x" + height;

        if (runDebugger) {
            debugger.log(logMessage, LogLevel.INFO);
        } else {
            System.out.println(logMessage);
        }

        croppingRectangle = new Rect(x, y, width, height);
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
        Mat croppedFrame;
        Mat rgbImage = new Mat();
        while (runCamera) {
            camera.read(frame);
            Imgproc.cvtColor(frame, rgbImage, Imgproc.COLOR_BGR2RGB);
            if (runDebugger) {
                debugger.log(rgbImage, ImageType.ORIGINAL, LogLevel.DEBUG);
            }

            croppedFrame = frame.submat(croppingRectangle);
            croppedFrame = detector.detect(croppedFrame);

            if (runDebugger) {
                debugger.log(croppedFrame, ImageType.EDITED, LogLevel.DEBUG);
            }
        }
    }

    private void runStatic() {
        String basePathStart = "/Images/startTest/test_";
        String basePathEnd = ".png";

        URL[] urls = new URL[45];

        for (int i = 1; i <= 45; i++) {
            urls[i - 1] = this.getClass().getResource(basePathStart + i + basePathEnd);
        }

        Mat workingCopy;
        int counter = 1;
        for (URL url : urls) {
            File file = new File(url.getFile());
            Mat inputImage = Highgui.imread(file.getAbsolutePath());

            System.out.println(counter);
            workingCopy = inputImage.submat(croppingRectangle);
            workingCopy = detector.detect(workingCopy);

            Util.showImage("Working copy #" + counter++, workingCopy);
        }
    }
}