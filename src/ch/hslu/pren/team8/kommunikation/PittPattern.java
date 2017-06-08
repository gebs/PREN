package ch.hslu.pren.team8.kommunikation;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by peach on 05.06.17.
 */
public class PittPattern implements Runnable {

    private Display display;

    public PittPattern(Display display) {
        this.display = display;
    }

    @Override
    public void run() {
        display.flash(3);

        GpioPinDigitalOutput[] leds = display.getLedArray();

        List<int[]> states = new ArrayList<>(Arrays.asList(
                new int[]{0},
                new int[]{0, 1},
                new int[]{0, 1, 2},
                new int[]{1, 2, 3},
                new int[]{2, 3, 4},
                new int[]{3, 4},
                new int[]{4},
                new int[]{},
                new int[]{},
                new int[]{3, 4},
                new int[]{2, 3, 4},
                new int[]{1, 2, 3},
                new int[]{0, 1, 2},
                new int[]{0, 1},
                new int[]{0},
                new int[]{},
                new int[]{}
        ));

        while (!Thread.currentThread().isInterrupted()) {
            for (int[] state : states) {
                display.turnAllLedsOff();
                for (int led : state) {
                    leds[led].low();
                }

                try {
                    Thread.sleep(75);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
