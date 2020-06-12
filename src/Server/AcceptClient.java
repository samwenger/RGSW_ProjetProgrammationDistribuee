package Server;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Classe gérant le client connecté et ses interactions avec le serveur
 */
public class AcceptClient implements Runnable {

    private Server server;

    private Socket clientSocketOnServer;
    private int clientNumber;

    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private ObjectInputStream objectInputStream;

    private ConnectedClient client;


    /**
     * Constructeur de la classe AcceptClient
     * @param server
     * @param clientNumber
     * @param clientSocketOnServer
     * @param dataInputStream
     * @param dataOutputStream
     * @param objectInputStream
     */
    public AcceptClient (Server server, int clientNumber, Socket clientSocketOnServer, DataInputStream dataInputStream, DataOutputStream dataOutputStream, ObjectInputStream objectInputStream)
    {
        this.server = server;
        this.clientSocketOnServer = clientSocketOnServer;
        this.clientNumber = clientNumber;

        this.dataIn = dataInputStream;
        this.dataOut = dataOutputStream;
        this.objectInputStream = objectInputStream;
    }

    /**
     * Gère les interactions entre un client et le serveur
     */
    @Override
    public void run() {

        // Recevoir la liste des fichiers du client
        ArrayList<String> filesList = null;
        try {
            filesList = (ArrayList<String>) objectInputStream.readObject();
        } catch (IOException e) {
            this.server.logger.error("IOException when receiving client's list of files.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            this.server.logger.error("Class not found when receiving client's list of files.");
            e.printStackTrace();
        }

        // Recevoir le port sur lequel les clients peuvent se connecter à l'utilisateur
        int clientPortForP2p = 0;
        try {
            clientPortForP2p = dataIn.readInt();
        } catch (IOException e) {
            this.server.logger.error("IOException when receiving client's port.");
            e.printStackTrace();
        }

        // Création du client et ajout à la liste des clients connectés
        client = new ConnectedClient(clientNumber, clientSocketOnServer.getInetAddress(), clientPortForP2p, filesList);
        this.server.addClient(client);


        // Confirmation
        this.server.logger.info("Client n°" + clientNumber + "connected. --> Accessible at : " + clientSocketOnServer.getInetAddress() + ":" + clientPortForP2p);


        // interactions entre le client et le serveur
        boolean interrupt = false;
        int numOperation;

        while(true){

            if(interrupt) {
                if(this.server != null) {
                    this.server.removeClientFromList(client);
                    this.server.logger.warn("Lost the connexion with client. Files list has been updated.");
                    break;
                }
            }

            try {

                if(this.server != null){

                    numOperation = dataIn.readInt();

                    // si action 1: retourner la liste de tous les fichiers dispos
                    switch (numOperation){
                        case 1:

                            ArrayList<ConnectedClient> listToSend = new ArrayList<>();

                            for(int i=0; i<this.server.getClientList().size(); i++) {

                                if(clientNumber != this.server.getClientList().get(i).getClientNumber()){
                                    listToSend.add(this.server.getClientList().get(i));
                                }
                            }

                            String json = new Gson().toJson(listToSend);

                            dataOut.writeUTF(json);
                            dataOut.flush();

                            this.server.logger.info("Files list sent to client n°" + clientNumber);
                            break;

                        // si action 2: enlever les fichiers de l'utilisateur de la liste et fermer le socket
                        case 2:
                            dataIn.close();
                            interrupt = true;

                            this.server.logger.info("Client n°" + clientNumber + " is disconnected.");

                            break;
                    }
                }

            } catch (IOException e) {
                this.server.logger.error("IOException when interacting with client.");
                try {
                    dataIn.close();
                } catch (IOException e1) {
                    this.server.logger.error("IOException when closing dataInputStream in interactions with client");
                    e1.printStackTrace();
                }
                interrupt = true;
            }
        }
    }
}
