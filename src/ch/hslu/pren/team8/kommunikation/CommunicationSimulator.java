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

        if (args.length != 3) {
            System.out.println("INVALID ARGUMENT LENGTH!");
            System.out.println("Arguments: startDelay | digitDelay | digit");
            System.exit(-1);
        }

        int startDelaySeconds = Integer.valueOf(args[0]);
        int digitDelaySeconds = Integer.valueOf(args[1]);
        int digit = Integer.valueOf(args[2]);

        System.out.println("Start in " + startDelaySeconds + "s | digit in " + digitDelaySeconds + "s | digit: " + digit);

        if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
            communicator = CommunicatorNonPi.getInstance();
            System.out.println("run on mac");
        } else {
            communicator = CommunicatorPi.getInstance();
            display = Display.getInstance();
            System.out.println("run on pi");
        }

        simulator.simulate(startDelaySeconds, digitDelaySeconds, digit);
    }

    public void simulate(int startDelaySeconds, int digitDelaySeconds, int digit) {
        sleep(startDelaySeconds * 1000);
        System.out.println("START");
        communicator.publishSignal(CommunicatorPi.SIGNAL_START);

        if (display != null) {
            display.showStartPattern();
        }

        sleep(digitDelaySeconds * 1000);

        if (display != null) {
            display.stopStartPattern();
        }

        simulateStatic(digit);
    }

    public void simulateStatic(int digit) {
        System.out.println("DIGIT: " + digit);

        if (display != null) {
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
