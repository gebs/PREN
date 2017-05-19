package ch.hslu.pren.team8.ziffernanzeige;

import com.pi4j.io.gpio.*;

import java.util.ArrayList;

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
        ArrayList<GpioPinDigitalOutput> activeLeds = getActiveLeds(digit);
        turnAllLedsOff();
        for (GpioPinDigitalOutput led : activeLeds) {
            led.low();
        }
    }

    public void showStartPattern() {
        // TODO: Anzeige des start-patterns implementieren
        showPattern = true;
        while (showPattern) {
            // Hinweis an ADRIAN: evt. werden für diesen Zweck noch weitere (private) Methoden verwendet
        }
    }

    private void stopStartPattern() {
        showPattern = false;
        // TODO: Start-pattern deaktivieren
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
