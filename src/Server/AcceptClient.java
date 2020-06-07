package Server;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AcceptClient implements Runnable {

    private Socket clientSocketOnServer;
    private int clientNumber;
    private SharedData sharedData;
    private Logger logger;


    public AcceptClient (Logger logger, Socket clientSocketOnServer, int clientNo, SharedData sharedData)
    {
        this.logger = logger;
        this.clientSocketOnServer = clientSocketOnServer;
        this.clientNumber = clientNo;
        this.sharedData = sharedData;
    }

    //overwrite the thread run()
    public void run() {

        try {

            // Recevoir la liste des fichiers du client
            ObjectInputStream objectInput = new ObjectInputStream(clientSocketOnServer.getInputStream());
            ArrayList<String> filesList = (ArrayList<String>) objectInput.readObject();

            // Recevoir le port sur lequel les clients peuvent se connecter à l'utilisateur
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocketOnServer.getInputStream()));
            int clientPortForP2p = inputStream.read();

            // Création de l'objet pour la mise à jour de la liste des utilisateurs et fichiers disponibles
            ConnectedClient client = new ConnectedClient(clientNumber, clientSocketOnServer.getInetAddress(), clientPortForP2p, filesList);
            sharedData.addClient(client);

            // Confirmation
            logger.info("Client n°" + clientNumber + "  Accessible at : " + clientSocketOnServer.getInetAddress() + ":" + clientPortForP2p);
            System.out.println();


            // Client connecté au serveur et attente des actions
            boolean connected = true;

            while (connected) {

                // attendre un message du client pour savoir la réponse à donner
                BufferedReader buffin = new BufferedReader(new InputStreamReader(clientSocketOnServer.getInputStream()));
                int numOperation = buffin.read();
                logger.info("Client n°" + clientNumber + " asked action n°" + numOperation);
                buffin.close();


                // si action 1: retourner la liste de tous les fichiers dispos
                if (numOperation == 1) {
                    String json = new Gson().toJson(sharedData.getClientsList());

                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocketOnServer.getOutputStream());
                    dataOutputStream.writeUTF(json);
                    dataOutputStream.flush();

                    logger.info("Files list sent to client n°" + clientNumber);
                    dataOutputStream.close();
                }


                // si action 2: enlever les fichiers de l'utilisateur de la liste et fermer le socket
                else if (numOperation == 2) {
                    System.out.println("updating");
                    sharedData.deleteClient(clientNumber);
                    clientSocketOnServer.close();

                    connected = false;
                    logger.info("Client n°" + clientNumber + " is disconnected");
                }

                System.out.println();
            }


      /*  } catch (SocketException e){
            updateFilesList();
            logger.warning("SocketException thrown, lost the connexion with client. Files list has been updated.");
            e.printStackTrace(); */
        } catch (IOException e) {
            sharedData.deleteClient(clientNumber);
            logger.severe("IOException thrown during list exchanges");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.severe("ClassNotFoundException when accessing filesList");
            e.printStackTrace();
        }
    }
}
