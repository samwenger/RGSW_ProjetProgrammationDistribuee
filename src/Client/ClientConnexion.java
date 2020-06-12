package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe permettant la connexion de clients à un client (p2p)
 */
public class ClientConnexion implements Runnable {

    private ServerSocket mySkServer;
    private Socket socketClient;

    private InetAddress localAddress;

    private String pathToFiles;


    /**
     * Constructeur de la classe ClientConnexion
     * @param localAddress
     * @param pathToFiles
     * @throws IOException
     */
    public ClientConnexion(InetAddress localAddress, String pathToFiles) throws IOException {
        this.localAddress = localAddress;
        this.pathToFiles = pathToFiles;

        mySkServer = new ServerSocket(0, 10, this.localAddress);
    }

    /**
     * Ecoute des connexions de clients et lancement du thread gérant la communication entre les clients (mutli-thread)
     */
    @Override
    public void run() {


        while(true) {
            try {
                // attendre des connexions
                socketClient = mySkServer.accept();
                ClientToClient p2p = new ClientToClient(socketClient, pathToFiles);
                // lancement du thread
                Thread t = new Thread(p2p);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Retourne le port local utilisé dans la connexion p2p
     * @return
     */
    public int getLocalPort() {
        return mySkServer.getLocalPort();
    }
}
