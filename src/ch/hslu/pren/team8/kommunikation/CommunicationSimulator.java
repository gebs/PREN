package ch.hslu.pren.team8.kommunikation;

/**
 * Created by peach on 01.06.17.
 */
public class CommunicationSimulator {

    private final static int SIGNAL_DURATION_MILLISECONDS = 5000;

    private static CommunicatorInterface communicator;

    private static Display display;

    public static void main(String[] args) {
        CommunicationSimulator simulator = new CommunicationSimulator();

        if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
            communicator = CommunicatorNonPi.getInstance();
            System.out.println("run on mac");
        } else {
            communicator = CommunicatorPi.getInstance();
            display = Display.getInstance();
            System.out.println("run on pi");
        }

        if (args.length > 0) {
            String digitString = args[0];
            int digit = Integer.valueOf(digitString);
            simulator.simulateStatic(digit);
        } else {
            simulator.simulate();
        }
    }

    public void simulate() {
        System.out.println("START");
        communicator.publishSignal(CommunicatorPi.SIGNAL_START);
        sleep(SIGNAL_DURATION_MILLISECONDS);

        for (int digit = 1; digit <= 5; digit++) {
            try {
                simulateStatic(digit);
                sleep(SIGNAL_DURATION_MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void simulateStatic(int digit) {
        System.out.println("DIGIT: " + digit);

        if (display != null) {
            System.out.println("HAS DISPLAY!");
            display.showDigit(digit);
        }

        try {
            communicator.publishDigitRecognition(digit);
        } catch (Exception e) {
            e.printStackTrace();
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
