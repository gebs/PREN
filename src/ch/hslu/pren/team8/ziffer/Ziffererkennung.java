package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.common.*;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.bitwise_or;
import static org.opencv.core.Core.inRange;
import static org.opencv.highgui.Highgui.imread;

/**
 * Klasse in welcher die ganze Ziffererkennung abläuft
 */
public class Ziffererkennung {

    private final boolean useCamera = false;
    private boolean runCamera = false;

    public Ziffererkennung() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void Start() {
        startWithFiles();
    }

    public void StartWithCamera() {
        //Init Kamera
        VideoCapture camera = new VideoCapture(0);

        /* Wait till camera is online*/
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (runCamera) {

        }
    }

    public void startWithFiles() {

        Mat oimg = imread("/home/gebs/Projects/PREN 2/PREN/resources/Images/hZiffer5.jpeg");
        Mat optiimage = optimizeImage(oimg);
        Mat redmask = getRedMask(optiimage);
        ArrayList<Rect> foundRectangles = new ArrayList<>();
        ArrayList<MatOfPoint2f> rectanglepoints = new ArrayList<>();

        findBoundingBox(redmask,foundRectangles,rectanglepoints);

        ArrayList<Point> points = new ArrayList<>();

        for (MatOfPoint2f p : rectanglepoints){
            points.addAll(p.toList());
        }

        Util.drawPoints(redmask,points);
       // Util.drawRectangles(redmask,foundRectangles);

        displayImage("Test", Util.toBufferedImage(redmask), Util.toBufferedImage(oimg));
    }

    public Mat optimizeImage(Mat oImage) {
        Mat hsv_img = new Mat();
        Imgproc.medianBlur(oImage, oImage, 3);
        Imgproc.cvtColor(oImage, hsv_img, Imgproc.COLOR_BGR2HSV);
        return hsv_img;
    }

    public Mat getRedMask(Mat hsv_img) {

        Mat redmask1 = new Mat();
        Mat redmask2 = new Mat();

        inRange(hsv_img, new Scalar(0, 100, 100), new Scalar(10, 255, 255), redmask1);
        inRange(hsv_img, new Scalar(160, 100, 100), new Scalar(179, 255, 255), redmask2);

        Mat retVal = new Mat();
        addWeighted(redmask1,1.0,redmask2,1.0,0.0,retVal);

        return retVal;
    }

    private void findBoundingBox(Mat img, ArrayList<Rect> foundRectangles, ArrayList<MatOfPoint2f> rectanglepoints) {

        Mat boundingBox = img.clone();
        java.util.List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(boundingBox, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);


        for (MatOfPoint contour : contours) {
            MatOfPoint2f approxcurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double approxdistance = Imgproc.arcLength(contour2f, true) * 0.02;

            if (approxdistance < 1)
                continue;

            Imgproc.approxPolyDP(contour2f, approxcurve, approxdistance, true);

            rectanglepoints.add(approxcurve);

            MatOfPoint points = new MatOfPoint(approxcurve.toArray());
            foundRectangles.add(Imgproc.boundingRect(points));
        }
    }

    private static void displayImage(String title, Image img2, Image orginal) {
        ImageIcon icon2 = new ImageIcon(img2);
        ImageIcon icon = new ImageIcon(orginal);
        JFrame frame = new JFrame(title);
        frame.setSize(400, 400);
        JLabel lbl2 = new JLabel(icon2);
        JLabel lbl = new JLabel(icon);
        JPanel panel = new JPanel();
        panel.add(lbl2);
        panel.add(lbl);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
