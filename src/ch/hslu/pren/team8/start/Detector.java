package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.Util;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Peter Gisler on 23.03.17
 */
public class Detector {

    private static Detector instance;

    private double sensitivity = 50.0;
    private int blurFactor = 15;

    private void Detector() {
        // private constructor for implementing singleton pattern
    }

    public static Detector getInstance() {
        if (instance == null) {
            instance = new Detector();
        }
        return instance;
    }

    public double getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getBlurFactor() {
        return blurFactor;
    }

    public void setBlurFactor(int blurFactor) {
        this.blurFactor = blurFactor;
    }

    /**
     * Detect red circles in provided input Image
     *
     * @param inputImage The input image to be processed
     * @return the processed image with marked circles
     */
    public Mat detect(Mat inputImage) {
        // blur image
        Imgproc.medianBlur(inputImage, inputImage, blurFactor);

        // convert input image to HSV
        Mat image = Util.toHsv(inputImage);

        // threshold image for everything that is not red
        thresholdColor(image);

        Mat circles = new Mat();
        Imgproc.HoughCircles(image, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, image.rows() / 8.0, 100.0, sensitivity, 0, 0);

        // Loop over all detected circles and outline them on the original image
        if (!circles.empty()) {
            for (int index = 0; index < circles.cols(); index++) {
                Point center = new Point(circles.get(0, index)[0], circles.get(0, index)[1]);
                double radius = circles.get(0, index)[2];
                Core.circle(inputImage, center, (int) radius, new Scalar(0, 255, 0), 5);
            }
        }

        return inputImage;
    }

    private void thresholdColor(Mat image) {
        Mat lowerHue = new Mat();
        Mat upperHue = new Mat();
        Core.inRange(image, new Scalar(0, 70, 50), new Scalar(10, 255, 255), lowerHue);
        Core.inRange(image, new Scalar(170, 70, 50), new Scalar(180, 255, 255), upperHue);

        // combine the two partial images
        Core.addWeighted(lowerHue, 1.0, upperHue, 1.0, 0.0, image);
        Imgproc.GaussianBlur(image, image, new Size(15.0, 15.0), 2.0, 2.0);
    }


}
