package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.Util;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.LogLevel;
import ch.hslu.pren.team8.kommunikation.Communicator;
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

    private static Detector instance;
    private Debugger debugger;
    private boolean runDebugger;

    private Map<String, Scalar[]> hueRanges;
    private HashMap<String, Integer> spotCounter;
    private List<Map<String, Integer>> spotCounterHistory;

    private static Communicator communicator = Communicator.getInstance();

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
            instance.debugger = Debugger.getInstance(runDebugger);
            instance.runDebugger = runDebugger;
        }
        return instance;
    }

    /**
     * Detect circles of specific colors in the provided input image
     *
     * @param inputImage The input image to be processed
     * @return the processed image with marked circles
     */
    public Mat detect(Mat inputImage) {
        // convert input image to HSV
        Mat image = Util.bgrToHsv(inputImage);

        if (hueRanges == null) {
            initializeHueRanges();
        }

        spotCounterHistory.add(new HashMap<>(spotCounter));

        // loop over every color range to detect
        for (Map.Entry<String, Scalar[]> entry : hueRanges.entrySet()) {
            String rangeName = entry.getKey();
            Mat maskedImage = thresholdColor(image, entry.getValue());

            Util.showImage("masked image", maskedImage);

            Mat circles = new Mat();
            Imgproc.HoughCircles(maskedImage, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, image.rows() / 8.0, 100.0, 5.0, 6, 12);

            spotCounter.replace(rangeName, circles.cols());
            markCircles(inputImage, circles);
        }

        String logMessage = "RED: " + spotCounter.get("red") + " | GREEN: " + spotCounter.get("green");
        log(logMessage);

        if (spotCounterHistory.size() >= 1) {
            HashMap<String, Integer> lastHistoryEntry = (HashMap<String, Integer>) spotCounterHistory.get(spotCounterHistory.size() - 1);
            if (lastHistoryEntry.get("red") > spotCounter.get("red") && lastHistoryEntry.get("green") < spotCounter.get("green")) {
                log("**** GO, GO, GO ****");
                communicator.publishStartSignal();
            }
        }

        return inputImage;
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
        Scalar[] redHueRange = new Scalar[]{new Scalar(160, 70, 20), new Scalar(190, 255, 255)};
        Scalar[] greenHueRange = new Scalar[]{new Scalar(40, 100, 20), new Scalar(80, 255, 255)};

        hueRanges = new HashMap<>();
        hueRanges.put("red", redHueRange);
        hueRanges.put("green", greenHueRange);

        spotCounter = new HashMap<>();
        spotCounter.put("red", 0);
        spotCounter.put("green", 0);

        spotCounterHistory = new ArrayList<>();
    }

    /**
     * Marks circles in the provided image object.
     *
     * @param image   The image to mark circles in
     * @param circles The circles to mark
     */
    private void markCircles(Mat image, Mat circles) {
        if (!circles.empty()) {
            for (int col = 0; col < circles.cols(); col++) {
                double[] circle = circles.get(0, col);
                Point center = new Point(circle[0], circle[1]);
                Core.circle(image, center, (int) circle[2], new Scalar(255, 0, 0), 2);
            }
        }
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