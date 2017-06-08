package ch.hslu.pren.team8.start;

import ch.hslu.pren.team8.common.Util;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.net.URL;

/**
 * Created by Peter Gisler on 04.05.2017.
 *
 *  DO NOT DOCUMENT THIS CLASS!!!
 *
 */
public class _Test {

    private static Detector detector;

    public static void main(String[] args) {
        detector = Detector.getInstance(false);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new _Test().run();
    }

    public void run() {
        String basePathStart = "/Images/startTest1/test_";
        String basePathEnd = ".png";

        int[] imageIndices = new int[]{1, 17};

        URL[] urls = new URL[imageIndices.length];

        int index = 0;
        for (int i : imageIndices) {
            urls[index++] = this.getClass().getResource(basePathStart + i + basePathEnd);
        }

        Mat workingCopy;
        int counter = 1;
        for (URL url : urls) {
            File file = new File(url.getFile());

            Mat inputImage = Highgui.imread(file.getAbsolutePath());
            Mat increasedBrightness = Util.increaseBrightness(inputImage, 2, 100);
            Util.showImage("Working copy #" + counter++, increasedBrightness);
            //workingCopy = detector.detect(inputImage);
        }
    }
}
