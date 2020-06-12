package Server;

import Client.MyInetAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * Application permettant de lancer le serveur
 */
public class ServerApp {

    private static Logger logger = LogManager.getLogger(ServerApp.class);


    /**
     * Création et lancement du serveur
     *
     * @param args
     */
    public static void main(String[] args) {


        try {
            String interfaceName = chooseNetworkInterface();
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

    public static String chooseNetworkInterface() throws IOException {

        Enumeration<NetworkInterface> allni;
        int cpt = 0;

        allni = NetworkInterface.getNetworkInterfaces();

        while (allni.hasMoreElements()) {
            NetworkInterface nix = allni.nextElement();

            if(nix.isUp() && !nix.isLoopback()){
                System.out.println(nix.getName());
                cpt++;


            }

        }

        if (cpt > 0) {
            System.out.println();
            System.out.println("Veuillez entrer le nom de l'interface que vous souhaitez utiliser : ");
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine();
        }
        else {
            System.out.println("Aucune interface disponible.");
        }

        return "-1";

    }
}