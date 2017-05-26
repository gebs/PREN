package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.LogLevel;
import ch.hslu.pren.team8.kommunikation.CommunicatorPi;
import ch.hslu.pren.team8.kommunikation.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Created by gebs on 5/7/17.
 */
public class AnalysisResultStorage {

    private static AnalysisResultStorage instance;

    private static final List<Integer> results = new ArrayList<Integer>();
    private static final int ENOUGHT_RESULTS = 20;
    private static final int ENOUGHT_TIME = 10;
    private static Debugger debugger = Debugger.getInstance(false);
    private static long firstNumberTime = 0;

    public static AnalysisResultStorage getInstance(){
        if (instance == null){
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
                if (firstNumberTime == 0) {
                    firstNumberTime = System.currentTimeMillis();
                }
            }
        }
    }

    boolean hasEnoughtResults() {

        return results.size() >= ENOUGHT_RESULTS ||
                ((((System.currentTimeMillis() - firstNumberTime) / 1000) >= ENOUGHT_TIME) && firstNumberTime != 0);
    }

    void processResults() {
        processStarted = true;
        System.out.println("DONE");

        debugger.log("Processing Started", LogLevel.ERROR);
        Map<Integer, Long> counts = results.stream().collect((Collectors.groupingBy(e -> e, Collectors.counting())));

        int romanNumber = counts.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();


        debugger.log("Roman Number found: " + romanNumber, LogLevel.ERROR);

        try{
            CommunicatorPi.getInstance().publishDigitRecognition(romanNumber);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try {
            Display.getInstance().showDigit(romanNumber);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
