package Server;

import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerApp {

    private static Logger logger;

    public static void main(String[] args) {


        try {
            logger = Logger.getLogger("TestLog");

            FileHandler fh = new FileHandler("./my.log",true);
            logger.addHandler(fh);

            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);


            String interfaceName = "wlan2";
            int serverPort = 45007;

            // Créer le serveur
            Server server = new Server(logger, interfaceName, serverPort);

            // Récupérer l'ip du serveur
            server.getLocalAddress();

            // Créer le socket du serveur
            server.createSocket();

            // Confirmation
            server.displayInfos();

            // Attente de connexions
            server.acceptConnexions();

        } catch (IOException e) {
            logger.severe("IOException thrown when creating FileHandler");
            e.printStackTrace();
        }

    }

}