package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.*;


/**
 * TCPInteractiveServer is responsible for launching a multithreaded dictionary server.
 * It initializes a custom thread pool, sets up a listening server socket,
 * handles dictionary read/write using JSON, and updates the GUI accordingly.
 */
public class TCPInteractiveServer {

    public static AtomicInteger numOfClient = new AtomicInteger(0);// Atomic counter for currently connected clients
    private ThreadPool threadPool = new ThreadPool(100);
    ServerSocket listeningSocket = null;
    public static ConcurrentHashMap<String, String> dictionary = new ConcurrentHashMap<String, String>();
    // The main dictionary data structure (thread-safe)
    private static int port;
    private static String dicFilePath;

    /**
     * Constructor for setting up server configuration.
     * 
     * @param dicFilePath the path to the JSON-formatted dictionary file
     * @param port        the port number the server listens on
     */
    public TCPInteractiveServer(String dicFilePath, int port) {
    	TCPInteractiveServer.dicFilePath = dicFilePath;
    	TCPInteractiveServer.port = port;
    	serverPrepare(); 
    }
    
    /**
     * Default constructor
     */
    public TCPInteractiveServer(){
    	
    }
    
    /**
     * Prepares the server environment: launches the GUI, loads the dictionary,
     * updates UI, and starts accepting client connections.
     */
    public void serverPrepare() {
      // Launch the server-side Swing GUI in a new thread
      ServerUI serverUI = new ServerUI();
      Thread uiThread = new Thread(serverUI);
      uiThread.start();

      try {
          Thread.sleep(800);// Give time for GUI to initialize before continuing
      } catch (InterruptedException e) {
          System.out.println("UI thread initialization was interrupted.");
      }

      readDictionary();                 // Load dictionary from file
      ServerUI.updateDictionarySize();  // Update size display
      dictionaryDisplay();              // Show dictionary contents in UI
      runServer();                      // Start listening for clients
    }

    /**
     * Opens a server socket and continuously listens for new clients.
     * Each accepted client is handled via the custom thread pool.
     */
    public void runServer() {
        try {
            listeningSocket = new ServerSocket(port);
            Socket clientSocket = null;
            while (true) {
                System.out.println("Server listening on port" + port + "for a connection");
                //Accept an incoming client connection request
                clientSocket = listeningSocket.accept(); //This method will block until a connection request is received
                // Only process if socket is valid
                if (clientSocket != null && !clientSocket.isClosed()) {
                	threadPool.submit(new Connection(clientSocket));
                }
            }
        } catch (SocketException e) {
            System.out.println("Server stopped listening.");
            ServerUI.logMessage("Server stopped listening.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            ServerUI.logMessage("Error: " + e.getMessage());
        } finally {
        	// Ensure the socket is closed on shutdown or crash
            if (listeningSocket != null) {
                try {
                    listeningSocket.close();
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    ServerUI.logMessage("Error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Reads the dictionary JSON file and loads it into the in-memory map.
     * Each word can have multiple meanings separated by semicolons.
     */
    private void readDictionary() {
        try {
            File dicFile = new File(dicFilePath);
            if (dicFile.exists()) {
                InputStreamReader is = new InputStreamReader(new FileInputStream(dicFile), "UTF-8");
                BufferedReader br = new BufferedReader(is);
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                br.close();
                is.close();

                // Parse JSON and populate dictionary
                JsonObject jsonObject = JsonParser.parseString(jsonBuilder.toString()).getAsJsonObject();
                for (String key : jsonObject.keySet()) {
                    JsonArray meaningsArray = jsonObject.getAsJsonArray(key);
                    StringBuilder meanings = new StringBuilder();
                    for (int i = 0; i < meaningsArray.size(); i++) {
                        if (meaningsArray.get(i).isJsonPrimitive() && meaningsArray.get(i).getAsJsonPrimitive().isString()) {
                            meanings.append(meaningsArray.get(i).getAsString()).append(";");
                        }
                    }
                    if (meanings.length() > 0)
                    	// Remove the last semicolon before storing
                        dictionary.put(key, meanings.substring(0, meanings.length() - 1)); // remove trailing ;
                }
                ServerUI.updateDictionarySize();
            } else {
                System.out.println("Dictionary not found!");
            }

        } catch (UnsupportedEncodingException e) {
        	ServerUI.logMessage("Encoding error: " + e.getMessage());
        } catch (FileNotFoundException e) {
        	ServerUI.logMessage("Dictionary file not found: " + dicFilePath);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            ServerUI.logMessage("Error: " + e.getMessage());
        }

    }

    /**
     * Writes the current dictionary to the file in formatted JSON.
     * Called when the dictionary is modified by a client.
     */
    public static synchronized void updateDictionary() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dicFilePath))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject json = new JsonObject();
            for (Entry<String, String> entry : dictionary.entrySet()) {
                JsonArray meaningsArray = new JsonArray();
                for (String meaning : entry.getValue().split(";")) {
                    meaningsArray.add(meaning);
                }
                json.add(entry.getKey(), meaningsArray);
            }
            String prettyJsonString = gson.toJson(json);
            bw.write(prettyJsonString);
            bw.flush();
        } catch (IOException e) {
            System.out.println("Write error: " + e.getMessage());
            ServerUI.logMessage("Write error: " + e.getMessage());
        }
    }

    /**
     * Updates the dictionary content preview on the server-side UI.
     * This method converts the map into a nicely formatted string.
     */
    public static void dictionaryDisplay() {
        StringBuilder displayBuilder = new StringBuilder();
        for (Entry<String, String> entry : dictionary.entrySet()) {
            displayBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        ServerUI.textAreaDicDisplay.setText(displayBuilder.toString());
    }

}
