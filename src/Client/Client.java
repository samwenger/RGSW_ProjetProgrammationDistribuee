package Client;

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
    private int localPortForServer;
    private int localPortForP2pIn;
    private int localPortForP2pOut;

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

    private PrintWriter poutServer;
    private PrintWriter poutClient;

    private Boolean connected;


    public Client (String serverName, int serverPort, String clientName, int localPortForServer, int localPortForP2pIn, int localPortForP2pOut, String pathToFiles) throws IOException {
        this.localAddress = InetAddress.getByName(clientName);
        this.localPortForServer = localPortForServer;
        this.localPortForP2pIn = localPortForP2pIn;
        this.localPortForP2pOut = localPortForP2pOut;
        this.pathToFiles = pathToFiles;

        this.serverAddress = InetAddress.getByName(serverName);
        this.serverPort = serverPort;
    }


    public void connectToServer() throws IOException {
        this.socketToServer = new Socket(serverAddress, serverPort, localAddress, localPortForServer);
        System.out.println();
        System.out.println("Connexion to server " + serverAddress + ":" + serverPort + " established.");
        connected = true;

        poutServer = new PrintWriter(this.socketToServer.getOutputStream());
    }

    public void connectToClient() throws IOException {
        this.socketToP2pClient = new Socket(clientP2pAddress, clientP2pPort, localAddress, localPortForP2pOut);
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

    public void sendLocalPortForP2pIn() {
        poutServer.write(localPortForP2pIn);
        poutServer.flush();
    }


    public void showOptions() throws IOException, InterruptedException, UnsupportedAudioFileException, LineUnavailableException {
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
        int messageToSend = 1;
        poutServer.write(messageToSend);
        System.out.println("Requesting list of available files.");
        poutServer.flush();

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
                System.out.println("  " + files.get(j));
            }

            System.out.println();
        }

    }


    public void getFileFromClient() throws IOException, InterruptedException, UnsupportedAudioFileException, LineUnavailableException {
        int idUser = selectUser();
        String fileName = selectFile();

        // connect to Client
        for(int i=0; i<availableFilesListJson.length(); i++){

            JSONObject client = availableFilesListJson.getJSONObject(i);

            if(client.getInt("clientNumber") == idUser){
                clientP2pAddress = InetAddress.getByName(client.getString("clientAddress"));
                clientP2pPort = client.getInt("clientPort");
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
        System.out.print("Veuillez entrer l'id de l'utilisateur : ");

        Scanner input = new Scanner(System.in);
        int id = input.nextInt();

        return id;
    }



    public String selectFile() {
        System.out.print("Veuillez entrer le nom du fichier : ");
        Scanner input = new Scanner(System.in);
        String titre = input.nextLine();
        return titre;
    }


    public void sendFileTitle(String fileName) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socketToP2pClient.getOutputStream());
        dataOutputStream.writeUTF(fileName);
        dataOutputStream.flush();
    }

    public void readFile() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        InputStream is = new BufferedInputStream(socketToP2pClient.getInputStream());

        AudioPlayer player = new AudioPlayer(is);
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
        int messageToSend = 2;
        poutServer.write(messageToSend);
        System.out.println("Disconnecting...");
        poutServer.flush();

        socketToServer.close();

        connected = false;
    }

    public Boolean getConnected() {
        return connected;
    }

}
