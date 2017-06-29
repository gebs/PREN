package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.LogLevel;
import ch.hslu.pren.team8.kommunikation.CommunicatorPi;
import ch.hslu.pren.team8.kommunikation.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * KLasse zur Speicherung und Auswertung der erkannten Ziffern
 * Created by gebs on 5/7/17.
 */
public class AnalysisResultStorage {

    private static AnalysisResultStorage instance;

    private static final List<Integer> results = new ArrayList<Integer>();
    private static final int ENOUGHT_RESULTS = 10;
    private static final int ENOUGHT_TIME = 55;
    private static Debugger debugger = Debugger.getInstance(true);
    private static long firstNumberTime = 0;

    public static AnalysisResultStorage getInstance() {
        if (instance == null) {
            instance = new AnalysisResultStorage();
        }
        return instance;
    }

    boolean isProcessStarted() {
        return processStarted;
    }

    private boolean processStarted = false;

    void put(int result) {
        synchronized (results) {
            if (!hasEnoughtResults()) {
                results.add(result);
            }
        }
    }

    boolean hasEnoughtResults() {
        if (firstNumberTime == 0) {
            firstNumberTime = System.currentTimeMillis();
        }

        return results.size() >= ENOUGHT_RESULTS || ((((System
                .currentTimeMillis() - firstNumberTime) / 1000) >= ENOUGHT_TIME) && firstNumberTime != 0);
    }

    void processResults() {
        processStarted = true;
        System.out.println("DONE");

        debugger.log("Processing Started", LogLevel.ERROR);
        Map<Integer, Long> counts = results.stream().collect((Collectors.groupingBy(e -> e, Collectors.counting())));

        int romanNumber = counts.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();


        if (romanNumber == 0) {
            int randomNum = ThreadLocalRandom.current().nextInt(1, 6);
            romanNumber = randomNum;
            debugger.log("Roman Number generated: " + romanNumber, LogLevel.ERROR);
        }
        else {
            debugger.log("Roman Number found: " + romanNumber, LogLevel.ERROR);
        }

        try {
            System.out.println("REC: " + romanNumber);
            CommunicatorPi.getInstance().publishDigitRecognition(romanNumber);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
