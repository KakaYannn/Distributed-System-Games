package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The Connection class represents a single client connection to the dictionary server.
 * It handles incoming requests from the client, processes JSON-based commands,
 * updates the shared dictionary, and sends appropriate responses back to the client.
 *
 * This class implements Runnable, and each instance is submitted to the thread pool.
 */
public class Connection implements Runnable {

    Socket clientSocket;
    BufferedReader in;
    BufferedWriter out;
    private String username = "Unknown";
    private boolean logged = false;

    /**
     * Constructor to initialize the socket and communication streams.
     *
     * @param clientSocket The socket connected to the client
     */
    public Connection(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            //Get the input/output streams for reading/writing data from/to the socket
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            clientSocket.setSoTimeout(300000); // 5 minutes
        } catch (IOException e) {
            ServerUI.logMessage("Client communication error: " + e.getMessage());
        }

    }


    /**
     * Main logic to process requests from the client.
     * Each request is expected to be a JSON string containing a dictionary operation.
     * The loop runs until the client disconnects or an error occurs.
     */
    @Override
    public void run() {
        TCPInteractiveServer.numOfClient.incrementAndGet();
        ServerUI.updateConnectedUsersDisplay();
        //ServerUI.logMessage("User " + username + " logged in at " + new Date().toString());

        try {
            String request;
            while ((request = in.readLine()) != null) {
            	// Parse and process the JSON request from client
                parseJsonRequest(request);
                // Persist dictionary changes and update the UI
                TCPInteractiveServer.updateDictionary();
                TCPInteractiveServer.dictionaryDisplay();
                ServerUI.updateDictionarySize();
            }
        } catch (SocketTimeoutException e) {
            ServerUI.logMessage("Client timed out due to inactivity.");
            System.out.println("Client timed out due to inactivity.");
        } catch (IOException e) {
            System.out.println("Client disconnected!");
            ServerUI.logMessage("Client disconnected unexpectedly!");
        } finally {
        	// Clean up resources and update connected user count
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                ServerUI.logMessage("Error closing socket: " + e.getMessage());
            }
            TCPInteractiveServer.numOfClient.decrementAndGet();
            ServerUI.updateConnectedUsersDisplay();
            ServerUI.logMessage("Client disconnected. Total: " + TCPInteractiveServer.numOfClient.get());
        }
    }

    /**
     * Parses a JSON command string and dispatches it to the appropriate handler.
     */
    public void parseJsonRequest(String jsonRequest) {

        ServerUI.logMessage("Received request: " + jsonRequest);
        
        JsonObject json = JsonParser.parseString(jsonRequest).getAsJsonObject();
        String operation = json.get("operation").getAsString();
        String word = json.get("word").getAsString();
        String newMeaning = json.has("newMeaning") ? json.get("newMeaning").getAsString() : "";
        String oldMeaning = json.has("oldMeaning") ? json.get("oldMeaning").getAsString() : "";
        if (json.has("username")) {
            this.username = json.get("username").getAsString();
            // First time login logging
            if (!logged) {
                ServerUI.logMessage("User " + username + " logged in at " + new Date().toString());
                logged = true;
            }
        }

        
        try {
            switch (operation) {
                case "ADD":
                    handleAdd(username, word, newMeaning);
                    break;
                case "DELETE":
                    handleDelete(username, word);
                    break;
                case "QUERY":
                    handleQuery(username, word);
                    break;
                case "ADD_MEANING":
                    handleAddMeaning(username, word, newMeaning);
                    break;
                case "UPDATE":
                    handleUpdate(username, word, oldMeaning, newMeaning);
                    break;
                default:
                    System.out.println("Invalid operation.");
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error handling client request: " + e.getMessage());
            ServerUI.logMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Handles adding a new word to the dictionary.
     */
    private void handleAdd(String username, String word, String newMeaning) throws IOException {
        if (TCPInteractiveServer.dictionary.containsKey(word)) {
            writeResponse("unsuccess");
            ServerUI.logMessage(username + " add operation failed: Word already exists.");
        } else {
            TCPInteractiveServer.dictionary.put(word, newMeaning);
            writeResponse("success");
            ServerUI.logMessage(username + " added word: " + word);
        }
    }

    /**
     * Handles deletion of a word from the dictionary.
     */
    private void handleDelete(String username, String word) throws IOException {
        if (TCPInteractiveServer.dictionary.containsKey(word)) {
            TCPInteractiveServer.dictionary.remove(word);
            writeResponse("success");
            ServerUI.logMessage(username + " deleted word: " + word);
        } else {
            writeResponse("unsuccess");
            ServerUI.logMessage(username + " remove operation failed: Word not found.");
        }
    }

    /**
     * Handles querying a word’s meaning(s).
     */
    private void handleQuery(String username, String word) throws IOException {
        if (TCPInteractiveServer.dictionary.containsKey(word)) {
            String response = TCPInteractiveServer.dictionary.get(word);
            writeResponse(response);
            ServerUI.logMessage(username + " queried word: " + word);
        } else {
            writeResponse("unsuccess");
            ServerUI.logMessage(username + " query operation failed: Word not found.");
        }
    }

    /**
     * Handles appending a new meaning to an existing word.
     */
    private void handleAddMeaning(String username, String word, String newMeaning) throws IOException {
        if (!TCPInteractiveServer.dictionary.containsKey(word)) {
            writeResponse("unsuccess");
            ServerUI.logMessage(username + " word added a new meaning failed: not found.");
        } else if (containsMeaning(word, newMeaning)) {
            writeResponse("sameMeaning");
            ServerUI.logMessage(username + " word added a new meaning failed: aleady existed.");
        } else {
            updatesMeaning(word, newMeaning);
            writeResponse("success");
            ServerUI.logMessage(username + " added a new meaning to: " + word + " " + newMeaning);
        }
    }

    /**
     * Handles updating an old meaning with a new one.
     */
    private void handleUpdate(String username, String word, String oldMeaning, String newMeaning) throws IOException {
        if (!TCPInteractiveServer.dictionary.containsKey(word) || !containsMeaning(word, oldMeaning)) {
            writeResponse("unsuccess");
            ServerUI.logMessage(username + " word meaning updated failed: not found.");
        } else if (containsMeaning(word, newMeaning)) {
            writeResponse("sameMeaning");
            ServerUI.logMessage(username + " word meaning updated failed: aleady existed.");
        } else {
            updatesMeaning(word, newMeaning, oldMeaning);
            writeResponse("success");
            ServerUI.logMessage(username + " updated word meaning: " + word + " " + newMeaning);
        }
    }

    /**
     * Checks if a given meaning already exists for the word.
     */
    private boolean containsMeaning(String word, String newMeaning) {
        String[] meaningList = TCPInteractiveServer.dictionary.get(word).split(";");
        for (String meaning : meaningList) {
            if (meaning.equals(newMeaning)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Appends a new meaning to the list of meanings for a word.
     */
    private void updatesMeaning(String word, String newMeaning) {
        String meaningList = TCPInteractiveServer.dictionary.get(word);
        meaningList = meaningList + ";" + newMeaning;
        TCPInteractiveServer.dictionary.put(word, meaningList);
    }

    /**
     * Replaces an existing meaning with a new one.
     */
    private void updatesMeaning(String word, String newMeaning, String oldMeaning) {
        String[] meaningList = TCPInteractiveServer.dictionary.get(word).split(";");
        StringBuilder updatedMeaningList = new StringBuilder();
        for (String meaning : meaningList) {
            if (!meaning.equals(oldMeaning)) {
                updatedMeaningList.append(";").append(meaning);
            } else {
                updatedMeaningList.append(";").append(newMeaning);
            }
        }
        // remove the first ";"
        String result = updatedMeaningList.toString().startsWith(";")
                ? updatedMeaningList.toString().substring(1)
                : updatedMeaningList.toString();
        TCPInteractiveServer.dictionary.put(word, result);

    }

    /**
     * Sends a single-line response to the client.
     */
    private void writeResponse(String response) throws IOException {
        out.write(response + "\n");
        out.flush();
    }

}
