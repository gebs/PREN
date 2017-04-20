package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.JsonHandler;
import ch.hslu.pren.team8.common.Util;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.json.simple.JSONObject;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter Gisler on 23.03.17
 */
public class Detector {

    private static Detector instance;
    private Debugger debugger;
    private boolean runDebugger;

    private HashMap<String, Scalar[]> hueRanges;
    private HashMap<String, Integer> spotCounter;
    private ArrayList<HashMap<String, Integer>> spotCounterHistory;

    private void Detector() {
        // private constructor for implementing singleton pattern
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
     * Detect red circles in provided input Image
     *
     * @param inputImage The input image to be processed
     * @return the processed image with marked circles
     */
    public Mat detect(Mat inputImage) {
        // blur image
        Imgproc.medianBlur(inputImage, inputImage, 7);

        // convert input image to HSV
        Mat image = Util.toHsv(inputImage);

        if (hueRanges == null) {
            initializeHueRanges();
        }

        spotCounterHistory.add(spotCounter);

        // loop over every color range to detect
        for (Map.Entry<String, Scalar[]> entry : hueRanges.entrySet()) {
            String rangeName = entry.getKey();
            Mat maskedImage = thresholdColor(image, entry.getValue());

            Mat circles = new Mat();
            Imgproc.HoughCircles(maskedImage, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, image.rows() / 8.0, 100.0, 5.0, 1, 8);

            spotCounter.replace(rangeName, circles.cols());
            markCircles(inputImage, circles);
        }

        String logMessage = "RED: " + spotCounter.get("red") + " | GREEN: " + spotCounter.get("green");
        log(logMessage);

        HashMap<String, Integer> lastHistoryEntry = spotCounterHistory.get(0);
        if (lastHistoryEntry.get("red") > spotCounter.get("red") && lastHistoryEntry.get("green") < spotCounter.get("green")) {
            log("**** GO, GO, GO ****");
        }

        return inputImage;
    }

    private Mat thresholdColor(Mat image, Scalar[] hueRange) {
        Mat range = new Mat();
        Core.inRange(image, hueRange[0], hueRange[1], range);
        Imgproc.GaussianBlur(range, range, new Size(15.0, 15.0), 2.0, 2.0);
        return range;
    }

    private void initializeHueRanges() {
        Scalar[] redHueRange = new Scalar[]{new Scalar(170, 70, 50), new Scalar(180, 255, 255)};
        Scalar[] greenHueRange = new Scalar[]{new Scalar(50, 100, 80), new Scalar(70, 255, 255)};

        hueRanges = new HashMap<>();
        hueRanges.put("red", redHueRange);
        hueRanges.put("green", greenHueRange);

        spotCounter = new HashMap<>();
        spotCounter.put("red", 0);
        spotCounter.put("green", 0);

        spotCounterHistory = new ArrayList<>();
    }

    private void markCircles(Mat image, Mat circles) {
        if (!circles.empty()) {
            for (int col = 0; col < circles.cols(); col++) {
                double[] circle = circles.get(0, col);
                Point center = new Point(circle[0], circle[1]);
                Core.circle(image, center, (int) circle[2], new Scalar(255, 0, 0), 2);
            }
        }
    }

    private void log(String message) {
        log(message, LogLevel.INFO);
    }

    private void log(String message, LogLevel level) {
        if (runDebugger) {
            debugger.log(message, level);
        } else {
            System.out.println(message);
        }
    }
}