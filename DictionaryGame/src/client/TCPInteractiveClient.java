package client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import com.google.gson.JsonObject;

/**
 * The TCPInteractiveClient class is responsible for handling all network communication
 * on the client side in the distributed dictionary application.
 * - Establishes a TCP connection to the dictionary server using the specified host and port.
 * - Runs on a dedicated thread to send JSON-formatted requests from a request queue to the server.
 * - Waits for server responses and provides feedback to the UI.
 * - Handles socket closure and gracefully updates the UI on disconnection.
 *
 * Key features:
 * - Uses a thread-safe LinkedBlockingQueue<String> to receive command strings from the UI.
 * - Converts dictionary operations into JSON strings via the static `createJsonCommand` method.
 * - Communicates with the server using buffered streams over a socket.
 * - Notifies the ClientUI in case of connection issues or server responses.
 */


public class TCPInteractiveClient implements Runnable {
	
	public static String username; // store username globally
	static String address;
    static int port;
    Socket clientSocket = null;
    private boolean connected = true;// Used to track connection status
    private BufferedReader in;
    private BufferedWriter out;

    // Queue used to handle requests from the UI in a thread-safe way
    LinkedBlockingQueue<String> requestQueue = new LinkedBlockingQueue<String>();


    /**
     * Entry point for the client thread.
     * It tries to establish a connection to the server, then waits for requests from the UI.
     */
    public void run() {
        try {
        	// Try connecting to the server using the given address and port
            clientSocket = new Socket(address, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            System.out.println("Connection established");
        } catch (IOException e) {
            System.out.println("No server found or launched, please try later!");
            connected = false;
            System.exit(1);// Exit if connection fails before GUI shows up
        }

        try {
            String inputStr;
            while ((inputStr = requestQueue.take()) != null) {
                try {
                	// Send a request to the server
                    out.write(inputStr + "\n");
                    out.flush();
                } catch (IOException e) {
                	// If server disconnects while sending, alert user and exit loop
                    System.out.println("The server is disconnected, please reopen the window later!");
                    ClientUI.setResultText("The server is disconnected, please reopen the window later!");
                    closeConnection();
                    break;
                }
            }
        } catch (InterruptedException e) {
            ClientUI.setResultText("Client was interrupted unexpectedly.");
            closeConnection();
        }
    }


    /**
     * Reads a single line response from the server after sending a request.
     * Returns null if the connection is closed or the server did not respond.
     */
    public String getResponse() {
        try {
            String response = in.readLine();
            if (response == null || !connected) {
                ClientUI.setResultText("Server did not respond. Please try again later.");
                closeConnection();
                return null;
            }
            return response;
        } catch (IOException e) {
            ClientUI.setResultText("Failed to communicate with server.");
            closeConnection();
            return null;
        }
    }

    /**
     * Utility method to build a JSON-formatted command to be sent to the server.
     */
    public static String createJsonCommand(String operation, String word, String newMeaning, String oldMeaning) {
        JsonObject json = new JsonObject();
        json.addProperty("operation", operation);
        json.addProperty("word", word);
        json.addProperty("newMeaning", newMeaning);
        json.addProperty("oldMeaning", oldMeaning);
        json.addProperty("username", username); 
        return json.toString(); 
    }


    /**
     * Gracefully closes the client socket connection and disables all UI buttons.
     */
    public void closeConnection() {
        connected = false;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        } finally {
            ClientUI.disableAllButtons();
        }
    }

    /**
     * Returns the current connection status of the client.
     */
    public boolean isConnected() {
        return connected;
    }

}
