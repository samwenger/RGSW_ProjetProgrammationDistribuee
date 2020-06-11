package Client;

import com.google.gson.JsonArray;
import javazoom.jl.decoder.JavaLayerException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    // Local Client
    private InetAddress localAddress;
    private int localPortForP2pIn = 0;


    // Access Server
    private InetAddress serverAddress;
    private int serverPort;
    private Socket socketToServer;

    // Access client (p2p)
    private InetAddress clientP2pAddress;
    private int clientP2pPort ;
    private Socket socketToP2pClient;

    private String pathToFiles;
    private ArrayList<String> filesList = new ArrayList<>();

    private JSONArray availableFilesListJson;

  //  private PrintWriter poutServer;
    private PrintWriter poutClient;
    private DataOutputStream dataOut;

    private Boolean connected;


    public Client (String serverName, int serverPort, InetAddress localAddress, int localPortForP2pIn, String pathToFiles) throws IOException {
        this.localAddress = localAddress;
        this.localPortForP2pIn = localPortForP2pIn;
        this.pathToFiles = pathToFiles;

        this.serverAddress = InetAddress.getByName(serverName);
        this.serverPort = serverPort;
    }


    public void connectToServer() throws IOException {
        socketToServer = new Socket(serverAddress, serverPort, localAddress, 0);
        System.out.println();
        System.out.println("Connexion to server " + serverAddress + ":" + serverPort + " established.");
        connected = true;

       // poutServer = new PrintWriter(this.socketToServer.getOutputStream());
        dataOut = new DataOutputStream(socketToServer.getOutputStream());
    }

    public void connectToClient() throws IOException {

            this.socketToP2pClient = new Socket(clientP2pAddress, clientP2pPort, localAddress, 0);
            System.out.println();
            System.out.println("Connexion to client " + serverAddress + ":" + serverPort + " established.");
            connected = true;

            poutClient = new PrintWriter(this.socketToP2pClient.getOutputStream());
    }


    public void scanFiles(){
        File folder = new File(pathToFiles);

        for (File file : folder.listFiles()) {
            if(!file.isDirectory()) {
                filesList.add(file.getName());
            }
        }
    }

    public void sendFilesList() throws IOException {
        scanFiles();
        ObjectOutputStream objectOutput = new ObjectOutputStream(this.socketToServer.getOutputStream());
        objectOutput.writeObject(filesList);

        System.out.println("List of local files submitted.");

    }

    public void sendLocalPortForP2pIn() throws IOException {
        dataOut.writeInt(localPortForP2pIn);
        dataOut.flush();
    }


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


    public void getAvailablesFiles() throws IOException {
        int numAction = 1;
        dataOut.writeInt(numAction);
        System.out.println("Requesting list of available files.");
        dataOut.flush();

        DataInputStream dataInputStream = new DataInputStream(this.socketToServer.getInputStream());
        String json = dataInputStream.readUTF();

        availableFilesListJson = new JSONArray(json);

        showAvailableFiles();
    }


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


    public void sendFileTitle(String fileName) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socketToP2pClient.getOutputStream());
        dataOutputStream.writeUTF(fileName);
        dataOutputStream.flush();
    }

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


    public void disconnectFromServer() throws IOException {
        int numAction = 2;
        dataOut.write(numAction);
        System.out.println("Disconnecting...");
        dataOut.flush();

        socketToServer.close();

        connected = false;
    }

    public Boolean getConnected() {
        return connected;
    }

}
