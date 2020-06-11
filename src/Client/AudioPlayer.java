package Client;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.*;
import java.io.*;

public class AudioPlayer {

    private final JFXPanel fxPanel = new JFXPanel();
    private MediaPlayer mediaPlayer;

    private File tempFile;

    // constructor to initialize streams and clip
    public AudioPlayer(InputStream is)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException
    {
        stream2file(is);

        Media media = new Media(tempFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        is.close();

    }

    public void play()
    {
        mediaPlayer.play();
    }


    // Method to pause the audio
    public void pause()
    {
        mediaPlayer.pause();
    }


    public void stop()
    {
        mediaPlayer.stop();

        System.out.println(tempFile.getPath());
        this.tempFile.delete();
    }


    public void stream2file (InputStream in) throws IOException {
        tempFile = File.createTempFile("tempStream", ".tmp");
        tempFile.deleteOnExit();

        OutputStream out = new FileOutputStream(tempFile);
        IOUtils.copy(in, out);
        out.close();
        in.close();

    }


}