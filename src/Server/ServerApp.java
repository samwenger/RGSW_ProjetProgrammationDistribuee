package Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class ServerApp {

    private static Logger logger = LogManager.getLogger(ServerApp.class);

    public static void main(String[] args) {


        try {
            String interfaceName = "wlan2";
            int serverPort = 45007;

            // Créer le serveur
            Server server = new Server(interfaceName, serverPort);

            // Récupérer l'ip du serveur
            server.getLocalAddress();

            // Créer le socket du serveur
            server.createSocket();

            // Confirmation
            server.displayInfos();

            // Attente de connexions
            server.acceptConnexions();


        } catch (IOException e) {
            logger.error("IOException thrown when accepting connexions from clients.");
            e.printStackTrace();
        }

    }
}