package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.common.Util;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.ImageType;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.opencv.core.Core.bitwise_or;
import static org.opencv.core.Core.inRange;
import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.norm;
import static org.opencv.core.Core.rectangle;
import static org.opencv.core.Core.split;

/**
 * Created by gebs on 6/14/17.
 */
public class DetectorWorker implements Runnable {

    private Mat srcimg;
    private Debugger debugger = Debugger.getInstance(true);
    private boolean runCamera;
    private String name;

    DetectorWorker(Mat srcimg, boolean runCamera, String name) {
        this.srcimg = srcimg;
        this.runCamera = runCamera;
        this.name = name;
    }

    @Override
    public void run() {

        debugger.log(srcimg, ImageType.ORIGINAL, LogLevel.DEBUG);

        Mat optiimage = optimizeImage(srcimg);

        Mat redmask = getRedMask(optiimage);
          Util.saveImage(redmask, "RedMask");

        debugger.log(redmask, ImageType.EDITED, LogLevel.DEBUG);

        ArrayList<Rect> foundRectangles = new ArrayList<>();
        ArrayList<RectanglePoints> rectanglepoints = new ArrayList<>();

        findBoundingBox(redmask, foundRectangles, rectanglepoints);

        /*for (Rect rect: foundRectangles){
            rectangle(srcimg, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar
            (255, 0, 0, 255), 1);
        }*/
        // Util.saveImage(optiimage, "Source");
        // ;



        if (rectanglepoints.size() > 1 && Util
                .distanceBtwPoints(new Point(foundRectangles.get(0).x, foundRectangles.get(0).y),
                        new Point(foundRectangles.get(1).x, foundRectangles.get(1).y)) < 300) {


            // debugger.log(redmask,ImageType.EDITED,LogLevel.DEBUG);
            //debugger.log("Enought Rectangles found starting analysis Worker",LogLevel.ERROR);
            new AnalysisWorker(srcimg, rectanglepoints, runCamera, name);
        }

    }

    private void processImage(Mat img) {

    }

    private Mat optimizeImage(Mat oImage) {
        Mat hsv_img = new Mat();
        Imgproc.medianBlur(oImage, oImage, 3);
        Imgproc.cvtColor(oImage, hsv_img, Imgproc.COLOR_BGR2HSV);


        return hsv_img;
    }

    private Mat histEqColor(Mat src) {
        Mat ycrcb = new Mat();
        Imgproc.cvtColor(src, ycrcb, Imgproc.COLOR_BGR2YCrCb);
        List<Mat> channels = new ArrayList<>();
        split(ycrcb, channels);
        Mat dst = new Mat();
        Imgproc.equalizeHist(channels.get(0), dst);
        channels.set(0, dst);
        merge(channels, ycrcb);
        Mat result = new Mat();
        Imgproc.cvtColor(ycrcb, result, Imgproc.COLOR_YCrCb2BGR);
        return result;
    }

    private Mat getRedMask(Mat hsv_img) {
        Mat redmask1 = new Mat();
        Mat redmask2 = new Mat();

        inRange(hsv_img, new Scalar(0, 120, 30), new Scalar(10, 255, 255), redmask1);
        inRange(hsv_img, new Scalar(155, 37, 38), new Scalar(185, 255, 255), redmask2);
      //  inRange(hsv_img, new Scalar(0, 130, 40), new Scalar(10, 255, 255), redmask1);
      //  inRange(hsv_img, new Scalar(155, 38, 45), new Scalar(185, 255, 255), redmask2);
        // inRange(hsv_img, new Scalar(0, 100, 100), new Scalar(10, 255, 255), redmask1);
        // inRange(hsv_img, new Scalar(160, 100, 100), new Scalar(179, 255, 255), redmask2);


        Mat retVal = new Mat();
        bitwise_or(redmask1, redmask2, retVal);
        //retVal = redmask1;

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
            if (rect.height < 20 || rect.width >= rect.height || (rect.height < (rect.width * 2))) {
                continue;
            }
            int closeResult = checkifRectIsClose(foundRectangles,rect);
            if (closeResult >= 0){
                rectanglepoints.get(closeResult).addPoints(approxcurve);
            }else {
                rectanglepoints.add(new RectanglePoints(++id, approxcurve));

                rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0, 255), 1);

                foundRectangles.add(Imgproc.boundingRect(points));
            }
        }
        //Util.saveImage(img, "BoundingBox");
     /*   debugger.log(foundRectangles.size() + " Rectangles found; " + rectanglepoints.size() + " Rectanglepoints
     found",
                LogLevel.DEBUG);*/
    }
    private int checkifRectIsClose(List<Rect> rects, Rect rect){
        int result = -1;
        for(Rect r : rects){
            double diffpos = r.x - rect.x;
            double diffwidth = r.width - rect.width;
            if ((diffpos < 50 && diffpos > -50)&&(diffwidth < 10 && diffwidth > -10)){
                result = rects.indexOf(r);
                break;
            }

        }
        return  result;
    }
}
