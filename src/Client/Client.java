package Client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Classe pour gérer le client
 */
public class Client {

    // Client local
    private InetAddress localAddress;
    private int localPortForP2pIn = 0;

    // Paramètres pour la connexion au serveur
    private InetAddress serverAddress;
    private int serverPort;
    private Socket socketToServer;

    // Paramètres pour la connexion à un client
    private InetAddress clientP2pAddress;
    private int clientP2pPort ;
    private Socket socketToP2pClient;

    // Accès aux fichiers
    private String pathToFiles;
    private ArrayList<String> filesList = new ArrayList<>();

    // Liste des fichiers disponibles
    private JSONArray availableFilesListJson;

    // Communication
    private PrintWriter poutClient;
    private DataOutputStream dataOut;
    private Boolean connected;


    /**
     * Constructeur d'un nouveau client
     * @param serverName
     * @param serverPort
     * @param localAddress
     * @param localPortForP2pIn
     * @param pathToFiles
     * @throws IOException
     */
    public Client (String serverName, int serverPort, InetAddress localAddress, int localPortForP2pIn, String pathToFiles) throws IOException {
        this.localAddress = localAddress;
        this.localPortForP2pIn = localPortForP2pIn;
        this.pathToFiles = pathToFiles;

        this.serverAddress = InetAddress.getByName(serverName);
        this.serverPort = serverPort;
    }


    /**
     * Permet la connexion au serveur
     * @throws IOException
     */
    public void connectToServer() throws IOException {
        socketToServer = new Socket(serverAddress, serverPort, localAddress, 0);
        System.out.println();
        System.out.println("Connexion to server " + serverAddress + ":" + serverPort + " established.");
        connected = true;

        dataOut = new DataOutputStream(socketToServer.getOutputStream());
    }

    /**
     * Permet la connexion à un client
     * @throws IOException
     */
    public void connectToClient() throws IOException {

            this.socketToP2pClient = new Socket(clientP2pAddress, clientP2pPort, localAddress, 0);
            System.out.println();
            System.out.println("Connexion to client " + serverAddress + ":" + serverPort + " established.");
            connected = true;

            poutClient = new PrintWriter(this.socketToP2pClient.getOutputStream());
    }


    /**
     * Ajoute la liste des fichiers qui sont disponibles dans un dossier à la liste des fichiers
     */
    public void scanFiles(){
        File folder = new File(pathToFiles);

        for (File file : folder.listFiles()) {
            if(!file.isDirectory()) {
                filesList.add(file.getName());
            }
        }
    }

    /**
     * Transmission de la liste des fichiers disponibles en local au serveur
     * @throws IOException
     */
    public void sendFilesList() throws IOException {
        scanFiles();
        ObjectOutputStream objectOutput = new ObjectOutputStream(this.socketToServer.getOutputStream());
        objectOutput.writeObject(filesList);

        System.out.println("List of local files submitted.");

    }

    /**
     * Transmission au serveur du port permettant la connexion d'autres client à ce client
     * @throws IOException
     */
    public void sendLocalPortForP2pIn() throws IOException {
        dataOut.writeInt(localPortForP2pIn);
        dataOut.flush();
    }


    /**
     * Proposer les actions disponibles à l'utilisateur et déclenche l'action demandée
     * @throws Exception
     */
    public void showOptions() throws Exception {
        System.out.println();
        System.out.println("Que souhaitez-vous faire ? ");
        System.out.println("1 - Obtenir la liste des fichiers disponibles");
        System.out.println("2 - Obtenir un fichier");
        System.out.println("3 - Quitter");
        System.out.print("Veuillez entrer le numéro de l'action : ");

        Scanner input = new Scanner(System.in);
        int action = input.nextInt();

        if(action == 1)
            getAvailablesFiles();
        else if(action == 2) {
            getFileFromClient();
        }else if(action == 3)
            disconnectFromServer();
    }


    /**
     * Récupérer du serveur la liste des fichiers disponibles chez d'autres utilisateurs
     * @throws IOException
     */
    public void getAvailablesFiles() throws IOException {

        // Envoi de la requête
        int numAction = 1;
        dataOut.writeInt(numAction);
        System.out.println("Requesting list of available files.");
        dataOut.flush();

        // Ecoute de la réponse
        DataInputStream dataInputStream = new DataInputStream(this.socketToServer.getInputStream());
        String json = dataInputStream.readUTF();

        // Enregistrement de la liste
        availableFilesListJson = new JSONArray(json);

        // Affichage de la liste
        showAvailableFiles();
    }


    /**
     * Affiche pour chaque client également connecté au serveur la liste des fichiers disponibles
     */
    public void showAvailableFiles() {

        for(int i=0; i<availableFilesListJson.length(); i++) {

            JSONObject client = availableFilesListJson.getJSONObject(i);

            System.out.print("Client id : " + client.get("clientNumber"));
            System.out.println(" (" + client.get("clientAddress") + ":" + client.get("clientPort") + ")");
            System.out.println("-----------------------------");

            JSONArray files = client.getJSONArray("clientFilesList");

            for(int j=0; j<files.length(); j++){
                System.out.println("Id " + (j+1) + ":  " + files.get(j));
            }

            System.out.println();
        }

    }


    /**
     * Connexion au client (p2p)
     * @throws Exception
     */
    public void getFileFromClient() throws Exception {

        if(availableFilesListJson == null || availableFilesListJson.length() == 0){
            System.out.println("Veuillez récupérer la liste des fichiers disponibles avant de continuer.");
            return;
        }

        int idUser = selectUser();
        int idFile = selectFile(idUser);

        // connect to Client
        for(int i=0; i<availableFilesListJson.length(); i++){

            JSONObject client = availableFilesListJson.getJSONObject(i);

            if(client.getInt("clientNumber") == idUser){
                clientP2pAddress = InetAddress.getByName(client.getString("clientAddress"));
                clientP2pPort = client.getInt("clientPort");
                String fileName = client.getJSONArray("clientFilesList").getString(idFile-1);

                connectToClient();
                sendFileTitle(fileName);
                readFile();
            }
        }
    }


    /**
     * Retourne l'id du client auquel le client souhaite se connecter
     * @return
     */
    public int selectUser() {
        System.out.println();
        System.out.println("Connexion à un utilisateur");
        System.out.println("---------------------------------");

        int id;
        Boolean valid = false;

        do {
            System.out.print("Veuillez entrer l'id de l'utilisateur : ");
            Scanner input = new Scanner(System.in);
            id = input.nextInt();

            for(int i=0; i < availableFilesListJson.length(); i++) {
                JSONObject client = availableFilesListJson.getJSONObject(i);

                if(client.getInt("clientNumber") == id){
                    valid = true;
                }
                else{
                    System.out.println("Utilisateur inexistant.");
                }
            }

        } while(!valid);


        return id;
    }


    /**
     * Retourne l'id du fichier que le client souhaite écouter
     * @param idUser
     * @return
     */
    public int selectFile(int idUser) {

        Boolean valid = false;

        int idFile;

        do {
            System.out.print("Veuillez entrer l'id du fichier : ");
            Scanner input = new Scanner(System.in);
            idFile = input.nextInt();

            for(int i=0; i < availableFilesListJson.length(); i++) {
                JSONObject client = availableFilesListJson.getJSONObject(i);

                if(client.getInt("clientNumber") == idUser){
                    if(client.getJSONArray("clientFilesList").length() >= idFile && idFile > 0){
                        valid = true;
                    }
                    else{
                        System.out.println("Fichier inexistant.");
                    }
                }
            }

        } while (!valid);

        return idFile;
    }


    /**
     * Transmission au client du titre que l'on souhaite écouter (p2p))
     * @param fileName
     * @throws IOException
     */
    public void sendFileTitle(String fileName) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socketToP2pClient.getOutputStream());
        dataOutputStream.writeUTF(fileName);
        dataOutputStream.flush();
    }

    /**
     * Stream d'un fichier distant
     * @throws Exception
     */
    public void readFile() throws Exception {

        InputStream is = new BufferedInputStream(socketToP2pClient.getInputStream());
        AudioPlayer player = new AudioPlayer(is);
        is.close();

        System.out.println();
        System.out.println("---------------------------------------------------");
        System.out.println("Welcome to AudioPlayer");
        System.out.println("Liste des actions : PLAY    PAUSE    EXIT");
        System.out.println("---------------------------------------------------");


        Boolean mediaPlayerConnected = true;

        do{
            Scanner scanner = new Scanner(System.in);
            String action = scanner.nextLine().toUpperCase();

            switch (action){
                case "PLAY":
                    player.play();
                    break;
                case "PAUSE":
                    player.pause();
                    break;
                case "EXIT":
                    player.stop();
                    mediaPlayerConnected = false;
                    break;
                default:
                    System.out.println("Action incconue.");
            }

        }while(mediaPlayerConnected);

        poutClient.close();
        socketToP2pClient.close();

    }


    /**
     * Déconnexion du serveur sur demande de l'utilisateur
     * @throws IOException
     */
    public void disconnectFromServer() throws IOException {
        int numAction = 2;
        dataOut.write(numAction);
        System.out.println("Disconnecting...");
        dataOut.flush();

        socketToServer.close();

        connected = false;
    }


    /**
     * Retourne le statut de connexion
     * @return
     */
    public Boolean getConnected() {
        return connected;
    }

}
