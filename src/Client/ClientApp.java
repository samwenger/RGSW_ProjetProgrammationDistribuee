package Client;

import javazoom.jl.decoder.JavaLayerException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) throws Exception {

        String serverName = "192.168.1.74";
        int serverPort  = 45007;

        InetAddress localAddress = chooseNetworkInterface();

        String pathToFiles = "C://toSend/Client1/";



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

        // Tant que le client est connecté au serveur, afficher le menu des actions possibles
        while(client.getConnected()) {
            client.showOptions();
        }

        System.out.println("test");
        thread.interrupt();

        System.exit(0);

    }



    public static InetAddress chooseNetworkInterface() throws IOException {

        Enumeration<NetworkInterface> allni;
        Enumeration<InetAddress> alladresses;

        InetAddress chosedInetAdress = null;

        System.out.println("----->External Linked up interfaces and their addresses (no loopback)");

        allni = NetworkInterface.getNetworkInterfaces();

        while (allni.hasMoreElements()) {
            NetworkInterface nix = allni.nextElement();

            if (nix.isUp()) {

                alladresses = nix.getInetAddresses();

                while (alladresses.hasMoreElements()) {
                    InetAddress inetAddress = alladresses.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        System.out.println(nix.getName() + " (" + nix.getDisplayName() + ") " + " --> " + inetAddress);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("Veuillez entrer le nom de l'interface à utiliser : ");
        Scanner scanner = new Scanner(System.in);
        String interfaceName = scanner.nextLine();


        allni = NetworkInterface.getNetworkInterfaces();

        while (allni.hasMoreElements()) {

            NetworkInterface nix = allni.nextElement();

            System.out.println("Avant: " + nix.getName());


            if(nix.getName().equals(interfaceName)) {
                System.out.println(nix.getName());
                chosedInetAdress = nix.getInetAddresses().nextElement();
                break;
            }
        }

        return chosedInetAdress;
    }
}