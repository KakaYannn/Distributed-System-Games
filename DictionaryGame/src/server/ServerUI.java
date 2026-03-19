package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;

/**
 * ServerUI is the graphical interface for the dictionary server.
 * It provides visual feedback on current server state, including:
 * - connected clients count
 * - current dictionary content
 * - dictionary size
 * - server logs
 * 
 * It also supports static methods to update the UI from other server components.
 */
public class ServerUI implements Runnable {

    public static JFrame frmForServer;
    public static JTextArea textAreaLog;
    public static JTextArea textAreaDicDisplay;
    public static JTextField textFieldDictionarySize;
    private static JTextField textFieldNumUsers;


    /**
     * Starts the GUI on a separate thread.
     */
    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            ServerUI.frmForServer.setVisible(true);
        } catch (Exception e) {
        	System.out.println("Failed to launch Server UI: " + e.getMessage());
        }
    }

    /**
     * Constructor that initializes the UI components.
     */
    public ServerUI() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmForServer = new JFrame();
        frmForServer.getContentPane().setBackground(new Color(204, 204, 255));
        frmForServer.getContentPane().setForeground(new Color(255, 204, 255));
        frmForServer.setBackground(new Color(255, 255, 153));
        frmForServer.setForeground(new Color(255, 153, 204));
        frmForServer.setTitle("SERVER UI");
        frmForServer.setBounds(100, 100, 800, 600);
        frmForServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmForServer.getContentPane().setLayout(null);

        JLabel lblNewLabel = new JLabel("Server Dictionary");
        lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
        lblNewLabel.setForeground(new Color(153, 51, 255));
        lblNewLabel.setBounds(300, 10, 200, 27);
        frmForServer.getContentPane().add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Connected users: ");
        lblNewLabel_1.setForeground(new Color(102, 51, 255));
        lblNewLabel_1.setBounds(46, 50, 126, 16);
        frmForServer.getContentPane().add(lblNewLabel_1);

        textFieldNumUsers = new JTextField();
        textFieldNumUsers.setForeground(new Color(204, 51, 255));
        textFieldNumUsers.setText("0");
        textFieldNumUsers.setBounds(200, 45, 200, 26);
        frmForServer.getContentPane().add(textFieldNumUsers);
        textFieldNumUsers.setColumns(10);

        JScrollPane scrollPaneDicDisplay = new JScrollPane();
        scrollPaneDicDisplay.setBounds(46, 120, 700, 180);
        frmForServer.getContentPane().add(scrollPaneDicDisplay);


        textAreaDicDisplay = new JTextArea();
        textAreaDicDisplay.setForeground(new Color(153, 51, 255));
        textAreaDicDisplay.setLineWrap(true);
        textAreaDicDisplay.setEditable(false);
        scrollPaneDicDisplay.setViewportView(textAreaDicDisplay);


        JLabel lblDicDisplay = new JLabel("DIctionary preview: ");
        lblDicDisplay.setForeground(new Color(102, 51, 255));
        lblDicDisplay.setBounds(46, 101, 194, 16);
        frmForServer.getContentPane().add(lblDicDisplay);
        JScrollPane scrollPaneLog = new JScrollPane();
        scrollPaneLog.setBounds(46, 320, 700, 200); 
        frmForServer.getContentPane().add(scrollPaneLog); 

        textAreaLog = new JTextArea();
        textAreaLog.setForeground(new Color(204, 51, 255));
        scrollPaneLog.setViewportView(textAreaLog);
        textAreaLog.setEditable(false); 

        JLabel lblDictionarySize = new JLabel("Dictionary size:");
        lblDictionarySize.setForeground(new Color(102, 51, 255));
        lblDictionarySize.setBounds(46, 80, 126, 16);
        frmForServer.getContentPane().add(lblDictionarySize);

        textFieldDictionarySize = new JTextField();
        textFieldDictionarySize.setForeground(new Color(204, 51, 255));
        textFieldDictionarySize.setEditable(false);
        textFieldDictionarySize.setBounds(200, 75, 200, 26);
        frmForServer.getContentPane().add(textFieldDictionarySize);
        textFieldDictionarySize.setColumns(10);

    }


    /**
     * Updates the dictionary size display field.
     * Called whenever the dictionary is modified.
     */
    public static void updateDictionarySize() {
        textFieldDictionarySize.setText(String.valueOf(TCPInteractiveServer.dictionary.size()));
    }

    /**
     * Appends a message to the server log text area and writes it to the server log file.
     *
     * @param message The log message to be recorded
     */
    public static void logMessage(String message) {
        textAreaLog.append(message + "\n");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("server.log", true))) {
            writer.write(message + "\n");
        } catch (IOException e) {
            System.out.println("Failed to write log: " + e.getMessage());
        }
    }

    /**
     * Updates the field showing the number of connected users.
     * Should be called whenever a client connects or disconnects.
     */
    public static void updateConnectedUsersDisplay() {
        textFieldNumUsers.setText(String.valueOf(TCPInteractiveServer.numOfClient.get()));
    }

}
