package client;

import java.awt.EventQueue;

import javax.swing.JOptionPane;


/**
 * This class serves as the entry point for launching the client-side application.
 * It validates command-line arguments and initializes the client UI on the Event Dispatch Thread.
 */
public class ClientLauncher {
    public static void main(String[] args) {
        try {
        	// Validate the command-line arguments:
            // Expect exactly two arguments: IP address and a valid port number (1025–65535)
            if (args.length == 2 && Integer.valueOf(args[1]) > 1024 && Integer.valueOf(args[1]) < 65536) {
            	// Store IP address and port into static fields in TCPInteractiveClient
            	TCPInteractiveClient.address = args[0];
                TCPInteractiveClient.port = Integer.valueOf(args[1]);
            } else {
                System.out.println("Invalid Input.");
                System.exit(1);// Exit if arguments are invalid
            }
            
            String inputName = JOptionPane.showInputDialog(null, "Enter your username:", "Login", JOptionPane.PLAIN_MESSAGE);
            if (inputName == null || inputName.trim().isEmpty()) {
                System.out.println("Username cannot be empty.");
                System.exit(1);
            }
            TCPInteractiveClient.username = inputName.trim();

            // Use the Swing event dispatch thread to safely launch the UI
            EventQueue.invokeLater(() -> {
                try {
                	// Initialize and display the client GUI
                    ClientUI window = new ClientUI();
                    window.frmDic.setVisible(true);
                } catch (Exception e) {
                	// Catch any exceptions during GUI creation
                    System.out.println("Client interface initiation fails.");
                }
            });
        } catch (NumberFormatException e) {
        	// Handle improper number format for port
            System.out.println("Invalid Input.");
            System.exit(1);
        }
    }
 

}

