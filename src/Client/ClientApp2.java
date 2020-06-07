package Client;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class ClientApp2 {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, UnsupportedAudioFileException, LineUnavailableException {

        String serverName = "192.168.1.74";
        int serverPort  = 45007;

        String localName = "192.168.1.74";

        String pathToFiles = "C://toSend/";




        // Création du thread pour accepter les connexions de clients (p2p)
        ClientConnexion clientConnexion = new ClientConnexion(localName, pathToFiles);
        Thread thread = new Thread(clientConnexion);
        thread.start();

        // Créer un client
        Client client = new Client(serverName, serverPort, localName, clientConnexion.getLocalPort(), pathToFiles);

        // Connecter le client au serveur
        client.connectToServer();

        // Envoyer la liste des fichiers disponibles
        client.sendFilesList();

        // Envoyer le port sur lequel d'autres clients peuvent se connecter (p2p)
        client.sendLocalPortForP2pIn();

        // Tant que le client est connecté au serveur, afficher le menu des actions possibles
        while(client.getConnected()) {
            client.showOptions();
        }

        System.out.println("test");
        thread.interrupt();

        System.exit(0);
    }
}
