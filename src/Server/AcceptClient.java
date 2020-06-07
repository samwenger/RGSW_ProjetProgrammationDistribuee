package Server;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AcceptClient implements Runnable {

    private Server server;

    private Socket clientSocketOnServer;
    private int clientNumber;
    private Logger logger;

    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private ObjectInputStream objectInputStream;

    private ConnectedClient client;


    public AcceptClient (Server server, int clientNumber, Logger logger, Socket clientSocketOnServer, DataInputStream dataInputStream, DataOutputStream dataOutputStream, ObjectInputStream objectInputStream)
    {
        this.server = server;
        this.logger = logger;
        this.clientSocketOnServer = clientSocketOnServer;
        this.clientNumber = clientNumber;

        this.dataIn = dataInputStream;
        this.dataOut = dataOutputStream;
        this.objectInputStream = objectInputStream;
    }

    @Override
    public void run() {

        // Recevoir la liste des fichiers du client
        ArrayList<String> filesList = null;
        try {
            filesList = (ArrayList<String>) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Recevoir le port sur lequel les clients peuvent se connecter à l'utilisateur
        int clientPortForP2p = 0;
        try {
            clientPortForP2p = dataIn.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Création du client et ajout à la liste
        client = new ConnectedClient(clientNumber, clientSocketOnServer.getInetAddress(), clientPortForP2p, filesList);
        this.server.addClient(client);


        boolean interrupt = false;
        int numOperation;

        while(true){

            if(interrupt) {
                if(this.server != null)
                    this.server.removeClientFromList(client);
                break;
            }

            try {

                if(this.server != null){

                    numOperation = dataIn.readInt();

                    // si action 1: retourner la liste de tous les fichiers dispos
                    switch (numOperation){
                        case 1:
                            String json = new Gson().toJson(this.server.getClientList());

                            dataOut.writeUTF(json);
                            dataOut.flush();

                            logger.info("Files list sent to client n°" + clientNumber);
                            break;

                        // si action 2: enlever les fichiers de l'utilisateur de la liste et fermer le socket
                        case 2:
                            dataIn.close();
                            interrupt = true;
                            break;
                    }
                }

            } catch (IOException e) {
                try {
                    dataIn.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                interrupt = true;
            }

        }


    /*
    //overwrite the thread run()
    public void run() {

        try {

            // Recevoir la liste des fichiers du client
            ObjectInputStream objectInput = new ObjectInputStream(clientSocketOnServer.getInputStream());
            ArrayList<String> filesList = (ArrayList<String>) objectInput.readObject();

            System.out.println("2: " + clientSocketOnServer.isClosed());


            // Recevoir le port sur lequel les clients peuvent se connecter à l'utilisateur
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocketOnServer.getInputStream()));
            int clientPortForP2p = inputStream.read();

            System.out.println("3: " + clientSocketOnServer.isClosed());


            // Création de l'objet pour la mise à jour de la liste des utilisateurs et fichiers disponibles
            ConnectedClient client = new ConnectedClient(clientNumber, clientSocketOnServer.getInetAddress(), clientPortForP2p, filesList);
            sharedData.addClient(client);

            System.out.println("4: " + clientSocketOnServer.isClosed());


            // Confirmation
            logger.info("Client n°" + clientNumber + "  Accessible at : " + clientSocketOnServer.getInetAddress() + ":" + clientPortForP2p);
            System.out.println();


            boolean connected = true;

            while (connected) {

                BufferedReader buffin = new BufferedReader(new InputStreamReader(clientSocketOnServer.getInputStream()));
                int numOperation = buffin.read();

                switch (numOperation){
                    case 1:
                        String json = new Gson().toJson(sharedData.getClientsList());

                        DataOutputStream dataOutputStream = new DataOutputStream(clientSocketOnServer.getOutputStream());
                        dataOutputStream.writeUTF(json);
                        dataOutputStream.flush();

                        logger.info("Files list sent to client n°" + clientNumber);
                        break;
                    case 2:
                        connected = false;
                }
            }



/*

            // Client connecté au serveur et attente des actions
            boolean connected = true;

            while (connected) {

                // attendre un message du client pour savoir la réponse à donner
                BufferedReader buffin = new BufferedReader(new InputStreamReader(clientSocketOnServer.getInputStream()));
                int numOperation = buffin.read();
                logger.info("Client n°" + clientNumber + " asked action n°" + numOperation);

                System.out.println("5: " + clientSocketOnServer.isClosed());


                // si action 1: retourner la liste de tous les fichiers dispos
                if (numOperation == 1) {

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
*/

      /*  } catch (SocketException e){
            updateFilesList();
            logger.warning("SocketException thrown, lost the connexion with client. Files list has been updated.");
            e.printStackTrace(); */

      /*
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            sharedData.deleteClient(clientNumber);
            try {
                clientSocketOnServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
}
