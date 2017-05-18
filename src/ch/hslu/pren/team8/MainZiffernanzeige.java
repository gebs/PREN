package ch.hslu.pren.team8;

import ch.hslu.pren.team8.ziffernanzeige.Display;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Peter Gisler on 30.03.17
 */
public class MainZiffernanzeige {

    public static void main(String[] args) throws InterruptedException {

        Display display = Display.getInstance();
        display.turnAllLedsOff();

        for (int i = 0; i < 10; i++) {
            int randomDigit = ThreadLocalRandom.current().nextInt(1, 6);
            System.out.println("Ziffer: " + randomDigit);
            display.showDigit(randomDigit);
            Thread.sleep(2000);
        }

        display.turnAllLedsOff();
    }
}
