package ch.hslu.pren.team8.kommunikation;

import ch.hslu.pren.team8.ziffer.Ziffererkennung;
import com.pi4j.io.gpio.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Peter Gisler on 11.05.17.
 * <p>
 * This class may be used for sending commands over GPIO pins. Therefore 3 output pins will be used for generating 8 different signals.
 * Currently only 6 of these signals have a significant meaning.
 * <p>
 * Important Notice: The internal bit numbering is from left (1) to right (3) which means from most significant (1) to least significant (3) bit.
 * <p>
 * Since the CommunicatorPi class implements the singleton pattern, you should use the static method getInstance to retrieve the single instance of the communicator. After obtaining the instance, the two public methods for publishing state information can be used.
 */
public class CommunicatorPi implements CommunicatorInterface {

    private static CommunicatorPi instance;
    private Display display;

    private static final Pin BIT1_PIN = RaspiPin.GPIO_22;
    private static final Pin BIT2_PIN = RaspiPin.GPIO_23;
    private static final Pin BIT3_PIN = RaspiPin.GPIO_26;

    private static GpioPinDigitalOutput BIT_1;
    private static GpioPinDigitalOutput BIT_2;
    private static GpioPinDigitalOutput BIT_3;

    public static Map<GpioPinDigitalOutput, PinState> SIGNAL_START;
    public static Map<GpioPinDigitalOutput, PinState> SIGNAL_DIGIT_1;
    public static Map<GpioPinDigitalOutput, PinState> SIGNAL_DIGIT_2;
    public static Map<GpioPinDigitalOutput, PinState> SIGNAL_DIGIT_3;
    public static Map<GpioPinDigitalOutput, PinState> SIGNAL_DIGIT_4;
    public static Map<GpioPinDigitalOutput, PinState> SIGNAL_DIGIT_5;

    private static Map<GpioPinDigitalOutput, PinState> createMap(PinState bit1state, PinState bit2state, PinState bit3state) {
        Map<GpioPinDigitalOutput, PinState> map = new HashMap<>();
        map.put(BIT_1, bit1state);
        map.put(BIT_2, bit2state);
        map.put(BIT_3, bit3state);
        return map;
    }

    /**
     * Private constructor since no direct object initialisation is allowed from the outside.
     * The gpio outputs ar initialised with a LOW signal.
     */
    private CommunicatorPi() {
        GpioController gpio = GpioFactory.getInstance();
        BIT_1 = gpio.provisionDigitalOutputPin(BIT1_PIN, "BIT 1", PinState.LOW);
        BIT_2 = gpio.provisionDigitalOutputPin(BIT2_PIN, "BIT 2", PinState.LOW);
        BIT_3 = gpio.provisionDigitalOutputPin(BIT3_PIN, "BIT 3", PinState.LOW);

        SIGNAL_START = createMap(PinState.HIGH, PinState.HIGH, PinState.LOW);
        SIGNAL_DIGIT_1 = createMap(PinState.LOW, PinState.LOW, PinState.HIGH);
        SIGNAL_DIGIT_2 = createMap(PinState.LOW, PinState.HIGH, PinState.LOW);
        SIGNAL_DIGIT_3 = createMap(PinState.LOW, PinState.HIGH, PinState.HIGH);
        SIGNAL_DIGIT_4 = createMap(PinState.HIGH, PinState.LOW, PinState.LOW);
        SIGNAL_DIGIT_5 = createMap(PinState.HIGH, PinState.LOW, PinState.HIGH);

        display = Display.getInstance();
    }

    /**
     * Returns the only existing communicator instance.
     * Upon retrieving the instance, the output signal is reset to triple-zero (zero-state with no significance).
     *
     * @return The only existing communicator instance
     */
    public static CommunicatorPi getInstance() {
        if (instance == null) {
            instance = new CommunicatorPi();
        }
        instance.resetSignal();
        return instance;
    }

    /**
     * Resets the output signal to triple-zero.
     * According to the communication protocol the state 000 has no determined meaning.
     */
    private void resetSignal() {
        BIT_1.setState(PinState.LOW);
        BIT_2.setState(PinState.LOW);
        BIT_3.setState(PinState.LOW);
    }

    /**
     * Publishes the start signal.
     */
    public void publishStartSignal() {
        publishSignal(SIGNAL_START);
        display.showStartPattern();
        new Ziffererkennung().Start();
    }

    /**
     * Publishes the "detected digit" signal for a specific digit.
     *
     * @param detectedDigit The detected digit value
     * @throws Exception If invalid digit value is provided
     */
    public void publishDigitRecognition(int detectedDigit) throws Exception {
        Map<GpioPinDigitalOutput, PinState> signal;
        switch (detectedDigit) {
            case 1:
                signal = SIGNAL_DIGIT_1;
                break;
            case 2:
                signal = SIGNAL_DIGIT_2;
                break;
            case 3:
                signal = SIGNAL_DIGIT_3;
                break;
            case 4:
                signal = SIGNAL_DIGIT_4;
                break;
            case 5:
                signal = SIGNAL_DIGIT_5;
                break;
            default:
                throw new IllegalArgumentException(detectedDigit + " is no valid value for detected digit! Only values 1-5 are valid.");
        }

        publishSignal(signal);
        display.showDigit(detectedDigit);
    }

    /**
     * Publishes a signal by iterating over the provided signal map.
     * The signal map should contain a state (HIGH|LOW) for every gpio output bit.
     *
     * @param signal The signal value to publish
     */
    public void publishSignal(Map<GpioPinDigitalOutput, PinState> signal) {

        for (Object o : signal.entrySet()) {
            Map.Entry pair = (Map.Entry) o;

            GpioPinDigitalOutput bit = (GpioPinDigitalOutput) pair.getKey();
            PinState state = (PinState) pair.getValue();
            bit.setState(state);
        }
    }
}
