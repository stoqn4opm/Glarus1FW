package firmware.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author stoqn
 */
public class NetworkManager {

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private ObjectInputStream streamIn;
    private ObjectOutputStream streamOut;

    public NetworkManager(int listenPort) {
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(listenPort);
        } catch (IOException e) {
            System.err.printf("[ERROR]Could not listen on port: $d.\n", listenPort);
            System.exit(-1);
        }
    }

    //**************************************************************************
    // Connection Handling
    //**************************************************************************
    public void waitForControlClientToConnect() {

        clientSocket = null;
        try {
            System.out.println("[INFO]Waiting for Control Client...");
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("[ERROR]Client connection atempt failed.");
            System.exit(1);
        }

        try {
            streamIn = new ObjectInputStream(clientSocket.getInputStream());
            streamOut = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException ex) {
            System.err.println("[ERROR]Trying to open streams of dead socket.");
            killCurrentConnection();
        }
    }

    public void killCurrentConnection() {
        try {
            streamIn.close();
            streamOut.close();

            clientSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            System.err.println("[ERROR]Unappropriate time to kill connection. Connection not closed.");
        }
    }

    //**************************************************************************
    // Sending / Receiveing Messages Handling
    //**************************************************************************
    public void sendMessageToControlClient(String message, boolean printLocally) {

        String preparedMessage = MessagesHandler.prepareMessageForSending(message);
        try {
            streamOut.writeObject(preparedMessage);
            streamOut.flush();
            if (printLocally) {
                System.out.printf("[INFO]Message to Control Client send:%s\n", message);
            }
        } catch (IOException ex) {
            System.err.println("[ERROR]Can not send message to Control Client. Connection is lost.");
        }
    }
    
    private void handleReceivedMessages() {
        // some endless loop to read input constanty needs to be implemented.
        MessagesHandler.handleReceivedMessage(null);
    }
}
