package ch.hslu.pren.team8.kommunikation;

import com.pi4j.io.gpio.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Peter Gisler on 30.03.17.
 * This class may be used for displaying detected digits.
 */
public class Display {

    private static Display instance;

    final private static Pin LED1_PIN = RaspiPin.GPIO_24;
    final private static Pin LED2_PIN = RaspiPin.GPIO_25;
    final private static Pin LED3_PIN = RaspiPin.GPIO_27;
    final private static Pin LED4_PIN = RaspiPin.GPIO_28;
    final private static Pin LED5_PIN = RaspiPin.GPIO_29;

    private GpioPinDigitalOutput led1;
    private GpioPinDigitalOutput led2;
    private GpioPinDigitalOutput led3;
    private GpioPinDigitalOutput led4;
    private GpioPinDigitalOutput led5;

    private boolean showPattern = false;

    private static final int SLEEP_MILLISECONDS = 1000;

    /**
     * Gets an instance of GpioFactory and initializes all 5 leds with an initial LOW state
     */
    private Display() {
        GpioController gpio = GpioFactory.getInstance();
        led1 = gpio.provisionDigitalOutputPin(LED1_PIN, "LED 1", PinState.LOW);
        led2 = gpio.provisionDigitalOutputPin(LED2_PIN, "LED 2", PinState.LOW);
        led3 = gpio.provisionDigitalOutputPin(LED3_PIN, "LED 3", PinState.LOW);
        led4 = gpio.provisionDigitalOutputPin(LED4_PIN, "LED 4", PinState.LOW);
        led5 = gpio.provisionDigitalOutputPin(LED5_PIN, "LED 5", PinState.LOW);
    }

    /**
     * This constructor is private since the Display class implements the singleton interface.
     *
     * @return The only existing instance of Display
     */
    public static Display getInstance() {
        if (instance == null) {
            instance = new Display();
        }
        return instance;
    }

    /**
     * Displays the specified digit.
     *
     * @param digit the digit to display
     */
    public void showDigit(int digit) {
        flash();
        ArrayList<GpioPinDigitalOutput> activeLeds = getActiveLeds(digit);
        for (GpioPinDigitalOutput led : activeLeds) {
            led.low();
        }
    }

    public void showStartPattern() {
        flash();
        showPattern = true;

        GpioPinDigitalOutput[] leds = new GpioPinDigitalOutput[]{led1, led2, led3, led4, led5};

        List<int[]> states = new ArrayList<>(Arrays.asList(
                new int[]{1},
                new int[]{1, 2},
                new int[]{1, 2, 3},
                new int[]{2, 3, 4},
                new int[]{3, 4, 5},
                new int[]{4, 5},
                new int[]{5},
                new int[]{4, 5},
                new int[]{3, 4, 5},
                new int[]{2, 3, 4},
                new int[]{1, 2, 3},
                new int[]{1, 2},
                new int[]{1}
        ));

        while (showPattern) {
            turnAllLedsOff();
            for (int[] state : states) {
                for (int led : state) {
                    leds[led].low();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopStartPattern() {
        showPattern = false;
        flash();
    }

    public void flash() {
        for (int count = 0; count < 4; count++) {
            try {
                turnAllLedsOn();
                Thread.sleep(200);
                turnAllLedsOff();
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Turns off all leds.
     */
    public void turnAllLedsOff() {
        led1.high();
        led2.high();
        led3.high();
        led4.high();
        led5.high();
    }

    public void turnAllLedsOn() {
        led1.low();
        led2.low();
        led3.low();
        led4.low();
        led5.low();
    }

    /**
     * Returns an array of all active leds for a specific digit.
     *
     * @param digit The digit which should be displayed
     * @return The array with all leds to turn on
     */
    private ArrayList<GpioPinDigitalOutput> getActiveLeds(int digit) {
        ArrayList<GpioPinDigitalOutput> activeLeds = new ArrayList<>();

        switch (digit) {
            case 1:
                activeLeds.add(led3);
                break;
            case 2:
                activeLeds.add(led2);
                activeLeds.add(led4);
                break;
            case 3:
                activeLeds.add(led1);
                activeLeds.add(led3);
                activeLeds.add(led5);
                break;
            case 4:
                activeLeds.add(led1);
                activeLeds.add(led2);
                activeLeds.add(led4);
                activeLeds.add(led5);
                break;
            case 5:
                activeLeds.add(led1);
                activeLeds.add(led2);
                activeLeds.add(led3);
                activeLeds.add(led4);
                activeLeds.add(led5);
                break;
            default:
                throw new IllegalArgumentException("Only digits from 1 to 5 are valid digits!");
        }

        return activeLeds;
    }
}
