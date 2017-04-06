package ch.hslu.pren.team8.ziffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import java.io.File;
import java.util.ArrayList;

import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;

/**
 * Created by Adrian on 03.11.2016.
 */
@Deprecated
class SVM {
    private static final String PATH_POSITIVE = "/home/gebs/Projects/PREN 2/PREN/resources/Images/TrainingData";
    private static final String PATH_NEGATIVE = "/home/gebs/Projects/PREN 2/PREN/resources/Images/Nagative";
    private static final String XML = "/home/gebs/Projects/PREN 2/PREN/resources/Images/test{0}.xml";

    private static ArrayList<Mat> trainingImages;
    private static ArrayList<Mat> trainingLabels;
    private static ArrayList<Mat> trainingData;
    private static ArrayList<Mat> classes;
    private static ArrayList<CvSVM> clasificadors;
    private static int cntClasses;


    static {
        trainingImages = new ArrayList<>();
        trainingLabels = new ArrayList<>();
        trainingData = new ArrayList<>();
        classes = new ArrayList<>();
        clasificadors = new ArrayList<>();
    }

    static void Init(int _cntClasses) {
        cntClasses = _cntClasses;
        for (int i = 0; i< cntClasses;i++){
            trainingImages.add(new Mat());
            trainingLabels.add(new Mat());
            trainingData.add(new Mat());
            classes.add(new Mat());
        }

    }

    private static Mat getMat(String path) {
        Mat img = new Mat();
        Mat con = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        con.convertTo(img, CvType.CV_32FC1, 1.0 / 255.0);
        resize(img, img, new Size(400, 300));
        return img;
    }

    static void train() {
        for (int i = 1; i<= cntClasses;i++){
            trainingImages.get(i-1).copyTo(trainingData.get(i-1));
            trainingData.get(i-1).convertTo(trainingData.get(i-1), CvType.CV_32FC1);
            trainingLabels.get(i-1).copyTo(classes.get(i-1));
            CvSVMParams params = new CvSVMParams();
            params.set_kernel_type(CvSVM.LINEAR);
            clasificadors.add(new CvSVM(trainingData.get(i-1), classes.get(i-1), new Mat(), new Mat(), params));
            clasificadors.get(i-1).save(XML.replace("{0}","" +i));
        }
    }

    public static void trainPositive() {
        for (int i = 1; i<= cntClasses;i++){
            for (File file : new File(PATH_POSITIVE + i).listFiles()) {
                Mat img = getMat(file.getAbsolutePath());
                trainingImages.get(i-1).push_back(img.reshape(1, 1));
                trainingLabels.get(i-1).push_back(Mat.ones(new Size(1, 1), CvType.CV_32FC1));
            }
        }
    }

    public static void trainNegative() {
        for (int i = 1; i<= cntClasses;i++){
            for (File file : new File(PATH_NEGATIVE + i).listFiles()) {
                Mat img = getMat(file.getAbsolutePath());
                trainingImages.get(i-1).push_back(img.reshape(1, 1));
                trainingLabels.get(i-1).push_back(Mat.zeros(new Size(1, 1), CvType.CV_32FC1));
            }
        }
    }

    public static void test(Mat testimage) {
        Mat in = new Mat();
        cvtColor(testimage, in, Imgproc.COLOR_BGR2GRAY);
        Mat out = new Mat();
        in.convertTo(out, CvType.CV_32FC1);
        out = out.reshape(1, 1);

        for (int i = 1; i<= cntClasses;i++){
            clasificadors.get(i-1).load(new File(XML).getAbsolutePath());
            System.out.println(clasificadors.get(i-1));

            System.out.println(out);
            System.out.println(i + ": "+clasificadors.get(i-1).predict(out));
        }
    }
}
