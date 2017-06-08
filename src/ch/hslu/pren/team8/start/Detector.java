package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.Util;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter Gisler on 23.03.17
 */
public class Detector {

    public final static int MISSING_RED_LIGHT_LIMIT = 5;
    public final static int MAIN_CIRCLE_LOWER_LIMIT = 30;
    private static int framesWithoutMainCircle = 0;
    private static Map<String, Integer> circles = new HashMap<>();

    private static Detector instance;
    private Debugger debugger;
    private boolean runDebugger;


    private Map<String, Scalar[]> hueRanges;
    private HashMap<String, Integer> spotCounter;
    private List<Map<String, Integer>> spotCounterHistory;

    /**
     * Private constructor for implementing singleton pattern
     */
    private Detector() {
        initializeHueRanges();
    }

    public static Detector getInstance(boolean runDebugger) {
        if (instance == null) {
            instance = new Detector();
            instance.initializeHueRanges();
            instance.runDebugger = runDebugger;
            if (runDebugger) {
                instance.debugger = Debugger.getInstance(true);
            }
        }
        return instance;
    }

    public boolean detect(Mat inputImage) {
        return detect(inputImage, 0);
    }

    /**
     * Detect circles of specific colors in the provided input image
     *
     * @param inputImage The input image to be processed
     * @return true if the start signal was detected
     */
    public boolean detect(Mat inputImage, int imgIndex) {
        boolean doStart = false;

        // convert input image to HSV
        Mat image = Util.bgrToHsv(inputImage);

        if (hueRanges == null) {
            initializeHueRanges();
        }

        spotCounterHistory.add(new HashMap<>(spotCounter));

        // loop over every color range to detect
        for (Map.Entry<String, Scalar[]> entry : hueRanges.entrySet()) {
            Mat maskedImage = thresholdColor(image, entry.getValue());

            Mat circles = new Mat();
            Imgproc.HoughCircles(maskedImage, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, image.rows() / 8.0, 150.0, 8.0, 3, 8);

            checkCircles(circles);
        }

        if (framesWithoutMainCircle > MISSING_RED_LIGHT_LIMIT) {
            log("***** START *****");
            framesWithoutMainCircle = 0;
            doStart = true;
        }

        return doStart;
    }

    /**
     * Color spots matching the provided hueRange will be masked in the image object.
     *
     * @param image    The image to mask the colors in
     * @param hueRange The color definition for masking
     * @return The masked image
     */
    private Mat thresholdColor(Mat image, Scalar[] hueRange) {
        Mat range = new Mat();
        Core.inRange(image, hueRange[0], hueRange[1], range);
        Imgproc.GaussianBlur(range, range, new Size(15.0, 15.0), 2.0, 2.0);
        return range;
    }

    /**
     * The hue ranges for the red and green spots to detect are initialised.
     * The counter and history for collecting the detected spots for both hue ranges are initialised also.
     */
    private void initializeHueRanges() {
        Scalar[] redHueRange = new Scalar[]{new Scalar(165, 80, 80), new Scalar(185, 255, 255)};

        hueRanges = new HashMap<>();
        hueRanges.put("red", redHueRange);

        spotCounter = new HashMap<>();
        spotCounter.put("red", 0);

        spotCounterHistory = new ArrayList<>();
    }

    /**
     * Marks circles in the provided image object.
     *
     * @param image       The image to mark circles in
     * @param circles     The circles to mark
     * @param circleColor The color for drawing the circles
     */
    private void markCircles(Mat image, Mat circles, Scalar circleColor) {
        if (!circles.empty()) {
            for (int col = 0; col < circles.cols(); col++) {
                double[] circle = circles.get(0, col);
                String circleIdentifier = getCircleIdentifier(circle);
                addCircle(circleIdentifier);
                Point center = new Point(circle[0], circle[1]);
                Core.circle(image, center, (int) circle[2], circleColor, 2);
            }
        }
    }

    private void checkCircles(Mat circles) {
        boolean foundMainCircle = false;
        Map.Entry<String, Integer> mainCircle = getMainCircle();

        if (!circles.empty()) {
            for (int col = 0; col < circles.cols(); col++) {
                double[] circle = circles.get(0, col);
                String circleIdentifier = getCircleIdentifier(circle);
                addCircle(circleIdentifier);

                if (mainCircle != null) {
                    foundMainCircle = foundMainCircle || circleIdentifier.equals(mainCircle.getKey());
                }
            }
        }

        if (foundMainCircle) {
            framesWithoutMainCircle = 0;
        } else if (mainCircle != null && mainCircle.getValue() > MAIN_CIRCLE_LOWER_LIMIT) {
            framesWithoutMainCircle++;
        }
    }

    private String getCircleIdentifier(double[] circle) {
        long x = Math.round(circle[0] / 10.0) * 10;
        long y = Math.round(circle[1] / 10.0) * 10;

        return x + "," + y;
    }

    private void addCircle(String circleIdentifier) {
        int newCircleCount = 1;

        if (circles.containsKey(circleIdentifier)) {
            newCircleCount = circles.get(circleIdentifier) + 1;
        }

        circles.put(circleIdentifier, newCircleCount);
    }

    private Map.Entry<String, Integer> getMainCircle() {
        Map.Entry<String, Integer> mainCircle = null;
        for (Map.Entry<String, Integer> entry : circles.entrySet()) {
            if (mainCircle == null || entry.getValue() > mainCircle.getValue()) {
                mainCircle = entry;
            }
        }
        return mainCircle;
    }

    /**
     * Log a message with default log level (LogLevel.INFO).
     *
     * @param message The message to log
     */
    private void log(String message) {
        log(message, LogLevel.INFO);
    }

    /**
     * Log a message, either via debugger object or just plain commandline output.
     *
     * @param message log content
     * @param level   log level for debugger logging
     */
    private void log(String message, LogLevel level) {
        if (runDebugger) {
            debugger.log(message, level);
        } else {
            System.out.println(message);
        }
    }

}