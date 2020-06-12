package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * Application permettant de lancer un nouveau client
 */
public class ClientApp {

    /**
     * Création d'un nouveau client et lancement
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Paramètres du client pour la connexion au serveur
        String serverName = setServerName();
        int serverPort  = 45007;

        InetAddress localAddress = chooseNetworkInterface();

        String pathToFiles = selectFilesFolder();


        // Création du thread pour accepter les connexions de clients (p2p)
        ClientConnexion clientConnexion = new ClientConnexion(localAddress, pathToFiles);
        Thread thread = new Thread(clientConnexion);
        thread.start();


        // Créer un client
        Client client = new Client(serverName, serverPort, localAddress, clientConnexion.getLocalPort(), pathToFiles);


        // Connecter le client au serveur
        client.connectToServer();


        // Envoyer la liste des fichiers disponibles
        client.sendFilesList();


        // Envoyer le port sur lequel d'autres clients peuvent se connecter (p2p)
        client.sendLocalPortForP2pIn();


        // Tantque le client est connecté au serveur, afficher le menu des actions possibles
        while(client.getConnected()) {
            client.showOptions();
        }

        thread.interrupt();

        System.exit(0);

    }

    /**
     * Saisie par l'utilisateur de l'interface à utiliser
     * @return
     * @throws IOException
     */
    public static InetAddress chooseNetworkInterface() throws IOException {

        Enumeration<NetworkInterface> allni;
        Enumeration<InetAddress> alladresses;
        ArrayList<MyInetAddress> validAdresses = new ArrayList<>();


        InetAddress chosedInetAdress = null;

        int cpt = 0;

        allni = NetworkInterface.getNetworkInterfaces();

        while (allni.hasMoreElements()) {
            NetworkInterface nix = allni.nextElement();

            if (nix.isUp()) {

                alladresses = nix.getInetAddresses();

                while (alladresses.hasMoreElements()) {
                    InetAddress inetAddress = alladresses.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        validAdresses.add(new MyInetAddress(nix.getName(), inetAddress));
                        System.out.println((cpt+1) + " (" + nix.getName() + ") --> " + inetAddress);
                        cpt++;
                    }
                }
            }
        }


        if (cpt > 0) {

            Boolean validSelection = false;

            do{

                System.out.println();
                System.out.println("Veuillez entrer l'id de l'interface à utiliser : ");
                Scanner scanner = new Scanner(System.in);
                int id = scanner.nextInt();

                if (id <= cpt) {
                    validSelection = true;
                }

            } while(!validSelection);


        }
        else{
            System.out.println("Aucune interface à utiliser");
        }


        return chosedInetAdress;

    }

    /**
     * Saisie par l'utilisateur du chemin vers le dossier des fichiers
     * @return
     */
    public static String selectFilesFolder() {
        System.out.print("Veuillez entrer le chemin vers le dossier à partager : ");
        Scanner scanner = new Scanner(System.in);

        return scanner.nextLine();
    }

    /**
     * Saisie par l'utilisateur de l'adresse du serveur à contacter
     * @return
     */
    public static String setServerName() {
        System.out.print("Veuillez entrer l'adresse du serveur : ");
        Scanner scanner = new Scanner(System.in);

        return scanner.nextLine();
    }
}