package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.common.Util;
import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.LogLevel;
import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.highgui.Highgui.CV_CAP_PROP_FRAME_WIDTH;
import static org.opencv.highgui.Highgui.imread;

/**
 * Klasse in welcher die ganze Ziffererkennung abläuft
 */
public class Ziffererkennung {

    private boolean runCamera = true;
    private Debugger debugger;
    private AnalysisResultStorage storage = AnalysisResultStorage.getInstance();
    ThreadPoolExecutor executor;


    public Ziffererkennung() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void Start() {

        runCamera = System.getProperty("user.name").equals("pi");
        debugger = Debugger.getInstance(runCamera);
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        if (runCamera) {
            startWithCamera(null);
        }
        else {
            startWithFiles();
        }
    }

    private void startWithCamera(VideoCapture camera) {
        debugger.log("Program started with Camera", LogLevel.DEBUG);
        //Init Kamera
        if (camera == null) {
            camera = new VideoCapture(0);
            camera.set(CV_CAP_PROP_FRAME_HEIGHT, 480);
            camera.set(CV_CAP_PROP_FRAME_WIDTH, 640);

        }

        /* Wait till camera is online*/
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        int i = 1;
        while (runCamera) {
            if (!storage.hasEnoughtResults()) {
                Mat frame = new Mat();
                camera.read(frame);
                if (frame.size().height == 0) {
                    debugger.log("No Image from Camera", LogLevel.ERROR);
                }
                Util.saveImage(frame, "image_" + i);
                if (executor.getActiveCount() != executor.getMaximumPoolSize()) {
                    executor.execute(new DetectorWorker(frame, runCamera, ""));
                }
            }
            else if (!storage.isProcessStarted()) {
                storage.processResults();
            }
            else {
                runCamera = false;
            }

            i++;
        }
        shutdown();
    }


    private void startWithFile() {
        Mat oimg = imread(   "/home/gebs/Projects/PREN 2/PREN/resources/TestImages/image_22temp286000000.png");
        if (executor.getActiveCount() != executor.getMaximumPoolSize()) {
            executor.execute(new DetectorWorker(oimg, runCamera, ""));
        }
        shutdown();
    }

    private void startWithFiles() {
        File folder = new File("resources/TestImages");
        for (File file : folder.listFiles()) {
            Mat src = imread(file.getAbsolutePath());

            executor.execute(new DetectorWorker(src, runCamera, file.getAbsolutePath()));

        }

    }

    private void shutdown() {
        try {
            while (executor.getTaskCount() != executor.getCompletedTaskCount()) {
                System.err.println("count=" + executor.getTaskCount() + "," + executor.getCompletedTaskCount());
                Thread.sleep(1000);
            }
            executor.shutdown();

            executor.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
