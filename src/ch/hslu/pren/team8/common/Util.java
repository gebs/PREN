package ch.hslu.pren.team8.common;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Utility Class for PREN Team 8
 */
public class Util {

    /**
     * Creates a Buffered Image from a Mat image (for Display)
     *
     * @param m Mat Image
     * @return Buffered Image
     */
    public static BufferedImage toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            Mat m2 = new Mat();
            Imgproc.cvtColor(m, m2, Imgproc.COLOR_BGR2RGB);
            type = BufferedImage.TYPE_3BYTE_BGR;
            m = m2;
        }
        byte[] b = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        image.getRaster().setDataElements(0, 0, m.cols(), m.rows(), b);
        return image;

    }

    /**
     * Draws an Array of Rectangles onto an Mat Image
     *
     * @param img        Image to draw on
     * @param rectangles Rectangles to draw
     */
    public static void drawRectangles(Mat img, ArrayList<Rect> rectangles) {

        for (Rect rec : rectangles) {
            Core.rectangle(img, new Point(rec.x, rec.y), new Point(rec.x + rec.width, rec.y + rec.height), new Scalar(255, 0, 0, 255), 1);
        }

    }

    public static void drawPoints(Mat img, ArrayList<Point> points) {
        for (Point p : points) {
            Core.circle(img, p, 2, new Scalar(255, 255, 0, 255));
        }
    }
}
