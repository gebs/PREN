package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.ImageType;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static org.opencv.core.Core.bitwise_or;
import static org.opencv.core.Core.inRange;
import static org.opencv.highgui.Highgui.imread;

/**
 * Klasse in welcher die ganze Ziffererkennung abläuft
 */
public class Ziffererkennung {

    private boolean runCamera = true;
    private Debugger debugger;
    private AnalysisResultStorage storage = AnalysisResultStorage.getInstance();


    public Ziffererkennung() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void Start() {

        runCamera = System.getProperty("user.name").equals("pi") || System.getProperty("user.name").equals("root");
        debugger = Debugger.getInstance(runCamera);
        if (runCamera) {
            startWithCamera();
        }
        else {
            startWithFile();
        }
    }

    private void startWithCamera() {
        debugger.log("Program started with Camera",LogLevel.DEBUG);
        //Init Kamera
        VideoCapture camera = new VideoCapture(0);


        /* Wait till camera is online*/
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (runCamera) {
            if (!storage.hasEnoughtResults()) {
                Mat frame = new Mat();
                camera.read(frame);
                if (frame.size().height == 0) {
                    debugger.log("No Image from Camera", LogLevel.ERROR);
                }
                processImage(frame);
            }
            else if (!storage.isProcessStarted()) {
                storage.processResults();
            }
            else {
                runCamera = false;
            }
        }
    }


    private void startWithFile() {
        Mat oimg = imread("resources/PI_IMage.png");
        processImage(oimg);
    }

    private void processImage(Mat img) {
      //  debugger.log("progstarted", LogLevel.DEBUG);
        debugger.log(img,ImageType.ORIGINAL,LogLevel.DEBUG);
        Mat rgbImage = new Mat();

        if (runCamera) {
            Imgproc.cvtColor(img, rgbImage, Imgproc.COLOR_BGR2RGB);
        }
        else {
            rgbImage = img;
        }

        Mat optiimage = optimizeImage(rgbImage);

        Mat redmask = getRedMask(optiimage);

        debugger.log(redmask,ImageType.EDITED,LogLevel.DEBUG);

        ArrayList<Rect> foundRectangles = new ArrayList<>();
        ArrayList<RectanglePoints> rectanglepoints = new ArrayList<>();

        findBoundingBox(redmask, foundRectangles, rectanglepoints);

        if (rectanglepoints.size() > 1) {
            debugger.log(redmask,ImageType.EDITED,LogLevel.DEBUG);
            //debugger.log("Enought Rectangles found starting analysis Worker",LogLevel.ERROR);
            new AnalysisWorker(img, rectanglepoints, runCamera);
        }
    }

    private Mat optimizeImage(Mat oImage) {
        // debugger.log("Optimize Image", LogLevel.DEBUG);
        Mat hsv_img = new Mat();
        Imgproc.medianBlur(oImage, oImage, 3);
        Imgproc.cvtColor(oImage, hsv_img, Imgproc.COLOR_BGR2HSV);

        return hsv_img;
    }

    private Mat getRedMask(Mat hsv_img) {
        Mat redmask1 = new Mat();
        Mat redmask2 = new Mat();

        inRange(hsv_img, new Scalar(0, 150, 50), new Scalar(10, 255, 255), redmask1);
        inRange(hsv_img, new Scalar(170, 65, 50), new Scalar(180, 255, 255), redmask2);

        Mat retVal = new Mat();
        bitwise_or(redmask1, redmask2, retVal);


        //morphological opening (remove small objects from the foreground)
        Imgproc.erode(retVal, retVal, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
        Imgproc.dilate(retVal, retVal, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));

        //morphological closing (fill small holes in the foreground)
        Imgproc.dilate(retVal, retVal, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
        Imgproc.erode(retVal, retVal, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));


        return retVal;
    }

    private void findBoundingBox(Mat img, ArrayList<Rect> foundRectangles, ArrayList<RectanglePoints> rectanglepoints) {
        Mat boundingBox = img.clone();
        java.util.List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(boundingBox, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        int id = 0;
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approxcurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double approxdistance = Imgproc.arcLength(contour2f, true) * 0.02;

            if (approxdistance < 1) {
                continue;
            }

            Imgproc.approxPolyDP(contour2f, approxcurve, approxdistance, true);
            MatOfPoint points = new MatOfPoint(approxcurve.toArray());

            Rect rect = Imgproc.boundingRect(points);
            Core.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(255, 0, 0, 255), 3);
            rectanglepoints.add(new RectanglePoints(++id, approxcurve));


            foundRectangles.add(Imgproc.boundingRect(points));
        }
     /*   debugger.log(foundRectangles.size() + " Rectangles found; " + rectanglepoints.size() + " Rectanglepoints
     found",
                LogLevel.DEBUG);*/
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
