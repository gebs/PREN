package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.JsonHandler;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.ImageType;
import ch.hslu.pren.team8.debugger.LogLevel;
import ch.hslu.pren.team8.ziffer.Ziffererkennung;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.net.URL;

public class StartRecognition {

    private final static boolean RUN_CAMERA = false;
    private final static boolean RUN_DEBUGGER = false;

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
        boolean configFileExists = jsonHandler.setJsonFile("pittProperties.json");
        detector = Detector.getInstance(RUN_DEBUGGER);

        if (RUN_DEBUGGER) {
            debugger = Debugger.getInstance(RUN_DEBUGGER);
        }

        if (configFileExists) {
            generateCroppingRectangle();
        }

        if (RUN_CAMERA) {
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

        if (RUN_DEBUGGER) {
            debugger.log(logMessage, LogLevel.INFO);
        } else {
            System.out.println(logMessage);
        }

        croppingRectangle = new Rect(x, y, width, height);
    }

    private void runVideo() {
        VideoCapture camera = new VideoCapture(0);

        // wait for camera
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Mat frame = new Mat();
        Mat workingFrame;
        Mat rgbImage = new Mat();
        boolean doStart = false;

        while (!doStart) {
            camera.read(frame);
            Imgproc.cvtColor(frame, rgbImage, Imgproc.COLOR_BGR2RGB);
            if (RUN_DEBUGGER) {
                debugger.log(rgbImage, ImageType.ORIGINAL, LogLevel.DEBUG);
            }

            if (croppingRectangle != null) {
                workingFrame = frame.submat(croppingRectangle);
            } else {
                workingFrame = frame;
            }

            doStart = detector.detect(workingFrame);

            if (RUN_DEBUGGER) {
                debugger.log(workingFrame, ImageType.EDITED, LogLevel.DEBUG);
            }
        }

        camera.release();
        startDigitRecognition();
    }

    private void runStatic() {
        String basePathStart = "/Images/startTest/test_";
        String basePathEnd = ".png";

        URL[] urls = new URL[38];

        for (int i = 1; i <= 38; i++) {
            urls[i - 1] = this.getClass().getResource(basePathStart + i + basePathEnd);
        }

        boolean doStart = false;

        Mat workingFrame;
        int counter = 1;
        for (URL url : urls) {
            if (doStart) {
                continue;
            }

            File file = new File(url.getFile());
            Mat inputImage = Highgui.imread(file.getAbsolutePath());

            System.out.println("Image #" + counter++);

            if (croppingRectangle != null) {
                workingFrame = inputImage.submat(croppingRectangle);
            } else {
                workingFrame = inputImage;
            }

            doStart = detector.detect(workingFrame);

            System.out.println("");
        }

        if (doStart) {
            startDigitRecognition();
        }
    }

    public void startDigitRecognition() {

        new Ziffererkennung().Start();
    }
}