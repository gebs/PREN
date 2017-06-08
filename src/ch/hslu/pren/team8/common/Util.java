package ch.hslu.pren.team8.common;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
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

    /**
     * Saves the image to the Harddisk
     *
     * @param image Image to save
     * @param path  Path to save the file to (Without filename)
     */
    public static void saveImage(Mat image, String path) {
        File file = new File(path + "temp" + Instant.now().getNano() + ".png");

        try {
            ImageIO.write(toBufferedImage(image), "png", file);  // ignore returned boolean
        } catch (IOException e) {
            System.out.println("Write error for " + file.getPath() +
                    ": " + e.getMessage());
        }
    }

    public static Mat increaseBrightness(Mat image, double alpha, double beta) {
        Mat optimizedImage = new Mat();
        image.convertTo(optimizedImage, -1, alpha, beta);
        return optimizedImage;
    }

    /**
     * Converts an image in BGR format to HSV format and returns the converted hsv Mat
     *
     * @param raw BGR input image
     * @return converted HSV image
     */
    public static Mat bgrToHsv(Mat raw) {
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(raw, hsvImage, Imgproc.COLOR_BGR2HSV);
        return hsvImage;
    }

    /**
     * Converts an image in HSV format to RGB format and returns the converted rgb Mat.
     *
     * @param raw HSV input image
     * @return converted RGB image
     */
    public static Mat hsvToRgb(Mat raw) {
        Mat rgbImage = new Mat();
        Imgproc.cvtColor(raw, rgbImage, Imgproc.COLOR_HSV2RGB);
        return rgbImage;
    }

    /**
     * Converts an image in HSV format to BGR format and returns the converted bgr Mat.
     *
     * @param raw HSV input image
     * @return converted BGR image
     */
    public static Mat hsvToBgr(Mat raw) {
        Mat bgrImage = new Mat();
        Imgproc.cvtColor(raw, bgrImage, Imgproc.COLOR_HSV2BGR);
        return bgrImage;
    }

    public static void showImage(String title, Mat image) {
        showImage(title, toBufferedImage(image));
    }

    public static void showImage(String title, Image image) {
        ImageIcon icon = new ImageIcon(image);
        JLabel lbl = new JLabel(icon);
        JPanel panel = new JPanel();
        panel.add(lbl);

        JFrame frame = new JFrame(title);
        frame.setSize(400, 400);
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
