package Client;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class AudioPlayer {
    // to store current position
    Long currentFrame;
    Clip clip;

    // current status of clip
    String status;

    AudioInputStream audioInputStream;
    static String filePath;

    // constructor to initialize streams and clip
    public AudioPlayer(InputStream is)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {
        // create AudioInputStream object
        audioInputStream = AudioSystem.getAudioInputStream(is);
        //AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());

        // create clip reference
        clip = AudioSystem.getClip();

        // open audioInputStream to the clip
        clip.open(audioInputStream);

        // clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void play()
    {
        if (clip.getMicrosecondPosition() == clip.getMicrosecondLength())
        {
            clip.setMicrosecondPosition(0);
        }
        //start the clip
        clip.start();

        status = "play";
    }


    // Method to pause the audio
    public void pause()
    {
        if (status.equals("paused"))
        {
            System.out.println("audio is already paused");
            return;
        }
        this.currentFrame = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }

    public void stop()
    {
        if(clip.getMicrosecondPosition() != 0){
            pause();
        }
        clip.setMicrosecondPosition(0);
    }


}