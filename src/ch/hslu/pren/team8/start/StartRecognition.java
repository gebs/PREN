package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.JsonHandler;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.ImageType;
import ch.hslu.pren.team8.debugger.LogLevel;
import ch.hslu.pren.team8.kommunikation.CommunicatorInterface;
import ch.hslu.pren.team8.kommunikation.CommunicatorNonPi;
import ch.hslu.pren.team8.kommunikation.CommunicatorPi;
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

    private JsonHandler jsonHandler;
    private Debugger debugger;
    private Detector detector;
    private CommunicatorInterface communicator;
    private Rect croppingRectangle;

    private final static int PI_IMAGE_FULL_WIDTH = 640;
    private final static int PI_IMAGE_FULL_HEIGHT = 480;
    private final static float PI_IMAGE_DEFAULT_CROPPING_FACTOR_X = 1.0f;
    private final static float PI_IMAGE_DEFAULT_CROPPING_FACTOR_Y = 1.0f;

    /**
     * This method initializes the process of start signal detection
     */
    public void start() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        jsonHandler = JsonHandler.getInstance();
        boolean configFileExists = jsonHandler.setJsonFile("pittProperties.json");

        if (configFileExists) {
            runDebugger = jsonHandler.getBoolean("runDebugger");
            runCamera = jsonHandler.getBoolean("runCamera");
        }

        debugger = Debugger.getInstance(runDebugger);
        detector = Detector.getInstance(runDebugger);

        if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
            communicator = CommunicatorNonPi.getInstance();
        } else {
            communicator = CommunicatorPi.getInstance();
        }

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
    private Rect getCroppingRectangle() {
        if (croppingRectangle == null) {
            int x, y, width, height;

            if (jsonHandler.hasValidFile()) {
                x = (int) (long) jsonHandler.getInt("trafficLightDimensions.x");
                y = (int) (long) jsonHandler.getInt("trafficLightDimensions.y");
                width = (int) (long) jsonHandler.getInt("trafficLightDimensions.width");
                height = (int) (long) jsonHandler.getInt("trafficLightDimensions.height");

                if (x + width > PI_IMAGE_FULL_WIDTH) {
                    width = PI_IMAGE_FULL_WIDTH - x;
                }

                if (y + height > PI_IMAGE_FULL_HEIGHT) {
                    height = (PI_IMAGE_FULL_HEIGHT - y);
                }

                //String logMessage = "Cropping Rect: x:" + x + " y:" + y + " | " + width + "x" + height;
                //log(logMessage, LogLevel.INFO);

            } else {
                x = 0;
                y = 0;
                width = (int) (PI_IMAGE_FULL_WIDTH * PI_IMAGE_DEFAULT_CROPPING_FACTOR_X);
                height = (int) (PI_IMAGE_FULL_HEIGHT * PI_IMAGE_DEFAULT_CROPPING_FACTOR_Y);
            }

            croppingRectangle = new Rect(x, y, width, height);
        }

        return croppingRectangle;
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
        boolean doRun = false;

        while (!doRun) {
            camera.read(frame);
            workingFrame = (getCroppingRectangle() != null) ? frame.submat(getCroppingRectangle()) : frame;
            doRun = detector.detect(workingFrame);
        }

        camera.release();
        communicator.publishStartSignal();
    }

    private void runStatic() {
        int testSeries = 2;
        int imageCount = 12;

        String basePathStart = "/Images/startTest" + testSeries + "/test_";
        String basePathEnd = ".png";


        URL[] urls = new URL[imageCount];

        for (int i = 1; i <= imageCount; i++) {
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
            Mat frame = Highgui.imread(file.getAbsolutePath());

            log("Image #" + counter);

            workingFrame = (getCroppingRectangle() != null) ? frame.submat(getCroppingRectangle()) : frame;

            doStart = detector.detect(workingFrame, counter++);

            log("");
        }

        if (doStart) {
            communicator.publishStartSignal();
        }
    }

    /**
     * Logs a message with a default log level.
     *
     * @param message to log
     */
    private void log(String message) {
        log(message, LogLevel.INFO);
    }

    /**
     * Logs a message with any log level either to the debugger or to the console.
     *
     * @param message  to log
     * @param logLevel for debugger
     */
    private void log(String message, LogLevel logLevel) {
        if (runDebugger) {
            debugger.log(message, logLevel);
        } else {
            System.out.println(message);
        }
    }
}