package ch.hslu.pren.team8;

import ch.hslu.pren.team8.start.StartRecognition;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Process p = new ProcessBuilder("omxplayer","-o","local","/home/pi/PREN_Music.mp3")
                    .start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        StartRecognition startRecognition = new StartRecognition();
        startRecognition.start();
    }
}
