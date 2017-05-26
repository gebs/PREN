package ch.hslu.pren.team8.kommunikation;

/**
 * Created by Peter Gisler on 26.05.17.
 */
public interface CommunicatorInterface {

    /**
     * Publish the start signal.
     */
    public void publishStartSignal();

    /**
     * Publish the recognition of a digit.
     *
     * @param detectedDigit The detected digit (valid values are 1-5)
     * @throws Exception if the provided digit is no valid digit
     */
    public void publishDigitRecognition(int detectedDigit) throws Exception;

}
