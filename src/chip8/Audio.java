package chip8;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Audio {
    public static boolean isRunning = false;
    private static Clip clip;
    
    public static void playSound(String file) {
            try {
            InputStream audioSrc = Audio.class.getResourceAsStream(file);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
                    isRunning=true;
            } catch (Exception e) {
                    System.err.println("Failed to play audio file: " + e.getMessage());
            }

    }
    public static void stopSound(){
        clip.stop();
        clip.close();
        isRunning=false;
    }

}