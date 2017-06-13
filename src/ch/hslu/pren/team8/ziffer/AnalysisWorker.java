package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.common.Util;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.ImageType;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.bitwise_or;
import static org.opencv.core.Core.inRange;
import static org.opencv.core.Core.line;
import static org.opencv.highgui.Highgui.imread;
import static org.opencv.highgui.Highgui.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.threshold;
import static org.opencv.imgproc.Imgproc.warpPerspective;

/**
 * Klasse zur Erkennung der gefundenen Zahl
 * Created by gebs on 5/7/17.
 */
public class AnalysisWorker implements Runnable {
    private Debugger debugger = Debugger.getInstance(true);
    private Thread thread;
    private Mat srcImg;
    private List<RectanglePoints> rectanglepoints;
    private boolean runCamera = true;

    AnalysisWorker(Mat srcImg, List<RectanglePoints> points, boolean runCamera) {
        this.srcImg = srcImg;
        this.rectanglepoints = points;
        this.runCamera = runCamera;

        if (this.thread == null) {
            this.thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        ArrayList<RectanglePoints> points = findEdgePoints(rectanglepoints);

        Mat persCorrect = PerspectiveCorrection(srcImg, points);

        if (runCamera) {
            Imgproc.cvtColor(persCorrect, persCorrect, Imgproc.COLOR_BGR2RGB);
        }

        Mat rdtest = new Mat();
        Imgproc.cvtColor(persCorrect, rdtest, COLOR_BGR2HSV);

        Mat redmask2 = getRedMask(rdtest);

        persCorrect.setTo(new Scalar(255, 255, 255), redmask2);

        Mat binaryImage = convertToBW(persCorrect);

        debugger.log(binaryImage, ImageType.EDITED, LogLevel.ERROR);

        Mat skel = generateSkel(binaryImage);

        List<RomanNumeralLine> lines = getHoughTransform(skel, 1, Math.PI / 180, 40);

        int foundNumber = getRomanNumeralNumber(lines);

        debugger.log("Found Number: " + foundNumber, LogLevel.ERROR);

        if (foundNumber != 0) {
            AnalysisResultStorage.getInstance().put(foundNumber);
        }

    }


    /**
     * Erzeugt eine Maske mit den Rot-Bereichen des Bildes
     * @param hsv_img Ein Bild im HSV Farbschema
     * @return Maske als BinaryImage
     */
    private Mat getRedMask(Mat hsv_img) {
        //  debugger.log("Finding Redmask", LogLevel.DEBUG);
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

    private ArrayList<RectanglePoints> findEdgePoints(List<RectanglePoints> recps) {
        ArrayList<RectanglePoints> points = new ArrayList<>();
        for (RectanglePoints rp : recps) {
            java.util.List<Point> testpoints = rp.getPoints().toList();
            if (rp.getId() == 1) {
                Point foundp = testpoints.get(0);
                //Find Point TopLeft
                for (Point p : testpoints) {
                    if (p.y > foundp.y) {
                        foundp = p;
                    }
                }
                points.add(new RectanglePoints(foundp, PointPosition.TOPLEFT));
                foundp = testpoints.get(0);
                //Find Point BottomLeft
                for (Point p : testpoints) {
                    if (p.y + p.x < foundp.y + foundp.x) {
                        foundp = p;
                    }
                }
                points.add(new RectanglePoints(foundp, PointPosition.BOTTOMLEFT));
            }
            else if (rp.getId() == 2) {
                Point foundp = testpoints.get(0);
                //Find Point BottomRight
                for (Point p : testpoints) {
                    if (p.y < foundp.y) {
                        foundp = p;
                    }
                }
                points.add(new RectanglePoints(foundp, PointPosition.BOTTOMRIGHT));
                foundp = testpoints.get(0);
                //Find Point TopRight
                for (Point p : testpoints) {
                    if (p.y + p.x > foundp.y + foundp.x) {
                        foundp = p;
                    }
                }
                points.add(new RectanglePoints(foundp, PointPosition.TOPRIGTH));
            }
        }
        return points;
    }

    private static Mat PerspectiveCorrection(Mat oimg, ArrayList<RectanglePoints> rectanglepoints) {


        if (rectanglepoints.stream().filter((rp) -> rp != null && rp.getPoint() != null).count() > 4) {
            return new Mat();
        }


        RectanglePoints tl = rectanglepoints.stream().filter((rp) -> rp.getPosition() == PointPosition.TOPLEFT)
                .findFirst().orElse(new RectanglePoints());
        RectanglePoints tr = rectanglepoints.stream().filter((rp) -> rp.getPosition() == PointPosition.TOPRIGTH)
                .findFirst().orElse(new RectanglePoints());
        RectanglePoints bl = rectanglepoints.stream().filter((rp) -> rp.getPosition() == PointPosition.BOTTOMLEFT)
                .findFirst().orElse(new RectanglePoints());
        RectanglePoints br = rectanglepoints.stream().filter((rp) -> rp.getPosition() == PointPosition.BOTTOMRIGHT)
                .findFirst().orElse(new RectanglePoints());

        if (tl.getPoint() == null || tr.getPoint() == null || bl.getPoint() == null || br.getPoint() == null) {
            return new Mat();
        }

        MatOfPoint2f rec = new MatOfPoint2f();

        java.util.List<Point> pts = new ArrayList<>();
        pts.add(bl.getPoint());
        pts.add(br.getPoint());
        pts.add(tr.getPoint());
        pts.add(tl.getPoint());

        rec.fromList(pts);

        double widthA = Math.sqrt((Math.pow((br.getPoint().x - bl.getPoint().x), 2)) + (Math
                .pow((br.getPoint().y - bl.getPoint().y), 2)));
        double widthB = Math.sqrt((Math.pow((tr.getPoint().x - tl.getPoint().x), 2)) + (Math
                .pow((tr.getPoint().y - tl.getPoint().y), 2)));
        double maxwidth = Math.max(widthA, widthB);

        double heightA = Math.sqrt((Math.pow((tr.getPoint().x - br.getPoint().x), 2)) + (Math
                .pow((tr.getPoint().y - br.getPoint().y), 2)));
        double heightB = Math.sqrt((Math.pow((tl.getPoint().x - bl.getPoint().x), 2)) + (Math
                .pow((tl.getPoint().y - bl.getPoint().y), 2)));
        double maxHeight = Math.max(heightA, heightB);

        MatOfPoint2f dst = new MatOfPoint2f();
        java.util.List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(maxwidth - 1, 0));
        points.add(new Point(maxwidth - 1, maxHeight - 1));
        points.add(new Point(0, maxHeight - 1));

        dst.fromList(points);

        Mat m = getPerspectiveTransform(rec, dst);
        Mat wraped = new Mat();
        warpPerspective(oimg, wraped, m, new Size(maxwidth, maxHeight));

        double prop = oimg.size().width / oimg.size().height;

        Mat scaled = new Mat();
        resize(wraped, scaled, new Size(prop * 300, 300));

        //wraped.copyTo(scaled);
        return scaled;
    }

    private Mat convertToBW(Mat srcImg) {
        Mat bgrimg = new Mat();
        Imgproc.cvtColor(srcImg, bgrimg, Imgproc.COLOR_HSV2BGR);
        Mat gray = new Mat();
        Imgproc.cvtColor(bgrimg, gray, Imgproc.COLOR_BGR2GRAY);

        Mat binaryImage = new Mat();
        threshold(gray, binaryImage, 65, 255, THRESH_BINARY_INV);

        Imgproc.erode(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
        Imgproc.dilate(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));

        //morphological closing (fill small holes in the foreground)
        Imgproc.dilate(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
        Imgproc.erode(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));


        return binaryImage;
    }

    private Mat generateSkel(Mat img) {
        UUID random = UUID.randomUUID();
        String basePath = "";
        if (runCamera) {
            basePath = "/home/pi/";
        }
        else {
            basePath = "resources/";
        }

        imwrite(basePath + random.toString() + ".png", img);

        Mat skel = new Mat();
        try {
            File f = new File(basePath + random.toString() + ".png");

            Process p = new ProcessBuilder(basePath + "voronoi", "thin", "zhang_suen_fast", f.getAbsolutePath())
                    .start();
            p.waitFor();
            skel = imread(basePath + random.toString() + "_thin.png", 0);


            f.delete();
            f = new File(basePath + random.toString() + "_thin.png");
            f.delete();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return skel;
    }

    private List<RomanNumeralLine> getHoughTransform(Mat image, double rho, double theta, int threshold) {
        Mat result = image.clone();
        bitwise_not(result, result);
        Mat lines = new Mat();
        List<RomanNumeralLine> rnlines = new ArrayList<>();
        Imgproc.HoughLinesP(image, lines, rho, theta, threshold, image.size().height / 4, 60);
        for (int i = 0; i < lines.cols(); i++) {
            double data[] = lines.get(0, i);
            Point pt1, pt2;
            pt1 = new Point(data[0], data[1]);
            pt2 = new Point(data[2], data[3]);
            line(result, pt1, pt2, new Scalar(0, 0, 255), 2);
            double angle = Math.atan2(pt2.y - pt1.y, pt2.x - pt1.x) * 180.0 / Math.PI;
            if (angle < 0) {
                angle = angle + 180;
            }
            if ((angle >= 10 || angle <= -10) && rnlines.stream().filter(rn -> rn.isNear(pt1, pt2)).count() == 0) {
                rnlines.add(new RomanNumeralLine(angle, pt1, pt2));
            }
        }

        return rnlines;
    }

    private int getRomanNumeralNumber(List<RomanNumeralLine> lines) {
        if (lines.size() == 1 && lines.stream().filter(RomanNumeralLine::isStraightLine).count() == lines.size()) {
            return 1;
        }
        else if (lines.size() == 2 && lines.stream().filter(RomanNumeralLine::isStraightLine).count() == lines.size()) {
            return 2;
        }
        else if (lines.size() == 3 && lines.stream().filter(RomanNumeralLine::isStraightLine).count() == lines.size()) {
            return 3;
        }
        else if (lines.size() == 3 && lines.stream().filter(RomanNumeralLine::isStraightLine).count() == 1 && lines
                .stream().filter(RomanNumeralLine::isVLine).count() == 2) {
            return 4;
        }
        else if (lines.size() == 2 && lines.stream().filter(RomanNumeralLine::isVLine).count() == lines.size()) {
            return 5;
        }
        else {
            return 0;
        }
    }
}
