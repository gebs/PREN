package ch.hslu.pren.team8.ziffernanzeige;

import com.pi4j.io.gpio.*;

import java.util.ArrayList;

/**
 * Created by Peter Gisler on 30.03.17
 */
public class Display {

    final private static Pin LED1_PIN = RaspiPin.GPIO_01;
    final private static Pin LED2_PIN = RaspiPin.GPIO_02;
    final private static Pin LED3_PIN = RaspiPin.GPIO_03;
    final private static Pin LED4_PIN = RaspiPin.GPIO_04;
    final private static Pin LED5_PIN = RaspiPin.GPIO_05;

    private GpioPinDigitalOutput led1;
    private GpioPinDigitalOutput led2;
    private GpioPinDigitalOutput led3;
    private GpioPinDigitalOutput led4;
    private GpioPinDigitalOutput led5;

    private static int SLEEP_MILLISECONDS = 1000;

    private GpioController gpio;

    public Display() {
        gpio = GpioFactory.getInstance();
        initializeLeds(PinState.LOW);
    }

    /**
     * Initializes all leds from 1 to 5 with a given state (LOW|HIGH)
     *
     * @param pinState The initial state of all pins
     */
    private void initializeLeds(PinState pinState) {
        led1 = gpio.provisionDigitalOutputPin(LED1_PIN, "LED 1", pinState);
        led2 = gpio.provisionDigitalOutputPin(LED2_PIN, "LED 2", pinState);
        led3 = gpio.provisionDigitalOutputPin(LED3_PIN, "LED 3", pinState);
        led4 = gpio.provisionDigitalOutputPin(LED4_PIN, "LED 4", pinState);
        led5 = gpio.provisionDigitalOutputPin(LED5_PIN, "LED 5", pinState);
    }

    public void demo() throws InterruptedException {

        System.out.println("<--Pi4J--> GPIO Control Example ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin = led1;

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);

        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(SLEEP_MILLISECONDS);

        // turn off gpio pin #01
        pin.low();
        System.out.println("--> GPIO state should be: OFF");

        Thread.sleep(SLEEP_MILLISECONDS);

        // toggle the current state of gpio pin #01 (should turn on)
        pin.toggle();
        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(SLEEP_MILLISECONDS);

        // toggle the current state of gpio pin #01  (should turn off)
        pin.toggle();
        System.out.println("--> GPIO state should be: OFF");

        Thread.sleep(SLEEP_MILLISECONDS);

        // turn on gpio pin #01 for 1 second and then off
        System.out.println("--> GPIO state should be: ON for only 1 second");
        pin.pulse(SLEEP_MILLISECONDS, true); // set second argument to 'true' use a blocking call

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();

        System.out.println("Exiting ControlGpioExample");
    }


    public void showDigit(int digit) {
        ArrayList<GpioPinDigitalOutput> activeLeds = getActiveLeds(digit);
        turnAllLedsOff();
        for (GpioPinDigitalOutput led : activeLeds) {
            led.high();
        }
    }

    private void turnAllLedsOff() {
        led1.low();
        led2.low();
        led3.low();
        led4.low();
        led5.low();
    }

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
