package ch.hslu.pren.team8.ziffer;

import ch.hslu.pren.team8.debugger.Debugger;
import ch.hslu.pren.team8.debugger.LogLevel;
import ch.hslu.pren.team8.ziffernanzeige.Display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by gebs on 5/7/17.
 */
public class AnalysisResultStorage {
    private static final List<Integer> results = new ArrayList<Integer>();
    private static int ENOUGHT_RESULTS = 20;
    private static Debugger debugger = Debugger.getInstance(false);

    public static boolean isProcessStarted() {
        return processStarted;
    }

    private static boolean processStarted = false;

    public static void put(int result) {
        synchronized (results) {
            if (!hasEnoughtResults()) {
                results.add(result);
            }
        }
    }

    public static boolean hasEnoughtResults() {
        return results.size() >= ENOUGHT_RESULTS;
    }

    public static void processResults() {
        processStarted = true;
        System.out.println("DONE");

        debugger.log("Processing Started", LogLevel.ERROR);
        Map<Integer,Long> counts = results.stream().collect((Collectors.groupingBy(e -> e,Collectors.counting())));

        int romanNumber = counts.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();


        debugger.log("Roman Number found: " + romanNumber, LogLevel.ERROR);

        Display display = new Display();
        display.turnAllLedsOff();
        display.showDigit(romanNumber);

    }

    private static double getPercentage(int number) {
        return (double)results.stream().filter(r -> r == number).count() / (double)results.size();
    }

}
