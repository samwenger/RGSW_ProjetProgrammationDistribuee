package Client;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.commons.io.IOUtils;
import java.io.*;


/**
 * Classe gérant le player audio
 */
public class AudioPlayer {

    private MediaPlayer mediaPlayer;
    private File tempFile;

    /**
     * Constructeur pour initialiser le stream
     * @param is
     * @throws IOException
     */

    public AudioPlayer(InputStream is) throws IOException
    {
        JFXPanel fxPanel = new JFXPanel();
        stream2file(is);

        Media media = new Media(tempFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        is.close();
    }


    /**
     * Play la lecture
     */
    public void play()
    {
        mediaPlayer.play();
    }


    /**
     * Pause la lecture
     */
    // Method to pause the audio
    public void pause()
    {
        mediaPlayer.pause();
    }


    /**
     * Stop la lecture
     */
    public void stop()
    {
        mediaPlayer.stop();
        mediaPlayer.dispose();

        tempFile.delete();
    }


    /**
     * Création d'un fichier temporaire pour la lecture à distance (stream)
     * @param in
     * @throws IOException
     */
    public void stream2file (InputStream in) throws IOException {
        tempFile = File.createTempFile("tempStream", ".tmp");
        tempFile.deleteOnExit();

        try (OutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }

        in.close();
    }


}