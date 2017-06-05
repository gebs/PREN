package ch.hslu.pren.team8.kommunikation;

import ch.hslu.pren.team8.ziffer.Ziffererkennung;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import java.util.Map;

/**
 * Created by Peter Gisler on 26.05.17.
 */
public class CommunicatorNonPi implements CommunicatorInterface {

    private static CommunicatorNonPi instance;

    private CommunicatorNonPi() {
        // empty constructor...
    }

    public static CommunicatorNonPi getInstance() {
        if (instance == null) {
            instance = new CommunicatorNonPi();
        }
        return instance;
    }

    @Override
    public void publishStartSignal() {
        System.out.println("\n\t --> START");
    }

    @Override
    public void publishDigitRecognition(int detectedDigit) throws Exception {
        System.out.println("DETECTED: " + detectedDigit);
    }

    /**
     * Publish a signal.
     *
     * @param signal to publish
     */
    public void publishSignal(Map<GpioPinDigitalOutput, PinState> signal) {
        System.out.println("PUBLISH ANY SIGNAL");
    }

}
