package ch.hslu.pren.team8.kommunikation;

import com.pi4j.io.gpio.*;

import java.util.*;

/**
 * Created by Peter Gisler on 30.03.17.
 * This class may be used for displaying detected digits.
 */
public class Display {

    private static Display instance;
    private Thread thread;
    private PittPattern pattern;

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
        flash(3);
        ArrayList<GpioPinDigitalOutput> activeLeds = getActiveLeds(digit);
        for (GpioPinDigitalOutput led : activeLeds) {
            led.low();
        }
    }

    public GpioPinDigitalOutput[] getLedArray() {
        return new GpioPinDigitalOutput[]{led1, led2, led3, led4, led5};
    }

    public void showStartPattern() {
        pattern = new PittPattern(this);
        thread = new Thread(pattern);
        thread.start();
    }

    public void stopStartPattern() {
        if (thread != null) {
            thread.interrupt();
            flash(3);
        }
    }

    public void flash(int flashCount) {
        for (int count = 0; count < flashCount; count++) {
            try {
                turnAllLedsOn();
                Thread.sleep(100);
                turnAllLedsOff();
                Thread.sleep(100);
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
        ArrayList<GpioPinDigitalOutput> activeLeds;

        switch (digit) {
            case 1:
                activeLeds = new ArrayList<>(Collections.singletonList(led3));
                break;
            case 2:
                activeLeds = new ArrayList<>(Arrays.asList(led2, led4));
                break;
            case 3:
                activeLeds = new ArrayList<>(Arrays.asList(led1, led3, led5));
                break;
            case 4:
                activeLeds = new ArrayList<>(Arrays.asList(led1, led2, led4, led5));
                break;
            case 5:
                activeLeds = new ArrayList<>(Arrays.asList(led1, led2, led3, led4, led5));
                break;
            default:
                throw new IllegalArgumentException("Only digits from 1 to 5 are valid digits!");
        }

        return activeLeds;
    }
}
