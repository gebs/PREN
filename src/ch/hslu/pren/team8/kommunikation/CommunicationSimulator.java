package ch.hslu.pren.team8.kommunikation;

/**
 * Created by peach on 01.06.17.
 */
public class CommunicationSimulator {

    private final static int SIGNAL_DURATION_MILLISECONDS = 2000;

    private CommunicatorInterface communicator;

    public static void main(String[] args) {
        new CommunicationSimulator().simulate();
    }


    public void simulate() {
        if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
            communicator = CommunicatorNonPi.getInstance();
        } else {
            communicator = CommunicatorPi.getInstance();
        }

        communicator.publishStartSignal();

        sleep(SIGNAL_DURATION_MILLISECONDS);

        for (int digit = 1; digit <= 5; digit++) {
            try {
                communicator.publishDigitRecognition(digit);
                sleep(SIGNAL_DURATION_MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param duration to sleep
     */
    private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
