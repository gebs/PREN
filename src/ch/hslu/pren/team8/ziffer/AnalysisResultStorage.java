package ch.hslu.pren.team8.ziffer;

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
        //TODO:Implementation

        Map<Integer,Long> counts = results.stream().collect((Collectors.groupingBy(e -> e,Collectors.counting())));

        int romanNumber = counts.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

        System.out.println("Roman Number found: " + romanNumber + " with " + results.size());
        Display display = new Display();
        display.turnAllLedsOff();
        display.showDigit(romanNumber);

        double per1 = getPercentage(1);
        double per2 = getPercentage(2);
        double per3 = getPercentage(3);
        double per4 = getPercentage(4);
        double per5 = getPercentage(5);
        double per6 = getPercentage(6);


        System.out.println("1: " + per1);
        System.out.println("2: " + per2);
        System.out.println("3: " + per3);
        System.out.println("4: " + per4);
        System.out.println("5: " + per5);
        System.out.println("6: " + per6);

    }

    private static double getPercentage(int number) {
        return (double)results.stream().filter(r -> r == number).count() / (double)results.size();
    }

}
