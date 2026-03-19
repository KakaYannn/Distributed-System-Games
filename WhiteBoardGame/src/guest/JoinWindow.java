package guest;

import remote.WhiteboardServer;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;

/**
 * JoinWindow provides the guest-side login interface for connecting to a remote whiteboard session.
 * Guests can specify their username and initiate a join request to the manager.
 */
public class JoinWindow {

    /** Default or CLI-provided RMI server address. */
    static String address;

    /** Default or CLI-provided RMI port. */
    static int port;

    /** Guest-provided username used in the session. */
    static String username;

    /** Swing frame for the login window. */
    private JFrame frmLoginWindow;

    /** Text field for entering the desired username. */
    private JTextField textField;

    /**
     * Main entry point for the guest client application.
     * Accepts optional command-line arguments: <address> <port> <username>
     */
    public static void main(String[] args) {
        if (args.length >= 3) {
            try {
                address = args[0];
                port = Integer.parseInt(args[1]);
                username = args[2];
            } catch (Exception e) {
                System.out.println("Expected: <address> <port> <username>");
                System.out.println("Reason: " + e.getMessage());
                System.exit(1);
            }
        } else {
            address = "127.0.0.1";
            port = 1099;
            username = "guest";
            System.out.println("Application is launched by default.");
        }

        EventQueue.invokeLater(() -> {
            try {
                JoinWindow window = new JoinWindow();
                window.frmLoginWindow.setVisible(true);
            } catch (Exception e) {
                System.out.println("Failed to launch login window: " + e.getMessage());
                JOptionPane.showMessageDialog(
                        null,
                        "Whiteboard login window failed to start.\n" + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }

    /** Constructs a JoinWindow and initializes UI components. */
    public JoinWindow() {
        initialize();
    }

    /**
     * Initializes the login UI, handles RMI join logic, and launches the GuestUI on success.
     */
    private void initialize() {
        frmLoginWindow = new JFrame("Login Window");
        frmLoginWindow.setBounds(100, 100, 450, 300);
        frmLoginWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmLoginWindow.getContentPane().setLayout(null);

        JLabel lblInputPrompt = new JLabel("Input your username displayed in the canvas room:");
        lblInputPrompt.setBounds(41, 25, 358, 16);
        frmLoginWindow.getContentPane().add(lblInputPrompt);

        textField = new JTextField(username);
        textField.setBounds(41, 58, 358, 26);
        frmLoginWindow.getContentPane().add(textField);
        textField.setText(username);

        // "Continue" button to initiate RMI join request
        JButton btnContinue = new JButton("Continue");
        btnContinue.setBounds(166, 127, 117, 29);
        frmLoginWindow.getContentPane().add(btnContinue);

        btnContinue.addActionListener(_ -> {
            try {
                username = textField.getText().trim();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(frmLoginWindow, "Username cannot be empty.");
                    return;
                }
                
                WhiteboardServer[] serverHolder = new WhiteboardServer[1];
                Exception[] errorHolder = new Exception[1];

                Thread lookupThread = new Thread(() -> {
                    try {
                        serverHolder[0] = (WhiteboardServer) Naming.lookup("rmi://" + address + ":" + port + "/WhiteboardService");
                    } catch (Exception e) {
                        errorHolder[0] = e;
                    }
                });

                lookupThread.start();
                lookupThread.join(3000);  // wait max 3 seconds

                if (serverHolder[0] == null) {
                    if (errorHolder[0] != null) {
                        JOptionPane.showMessageDialog(null,
                                "Failed to connect to server:\n" + errorHolder[0].getMessage(),
                                "Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Connection timed out. Please check the server address and try again.",
                                "Timeout",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    System.exit(1);
                }

                // Lookup the remote WhiteboardServer interface
                WhiteboardServer server = serverHolder[0];

                // Register the guest's own remote stub
                GuestRemoteImpl remoteStub = new GuestRemoteImpl();

                // Request to join; may return adjusted name or null if rejected
                String actualName = server.requestJoin(username, remoteStub);

                if (actualName == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Your request to join was rejected by the manager.",
                            "Join Rejected",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    System.exit(0);
                }

                frmLoginWindow.dispose();
                new GuestUI(server, remoteStub, actualName);

            } catch (Exception ex) {
                System.out.println("Failed to launch Guest whiteboard for user.");
                JOptionPane.showMessageDialog(
                        frmLoginWindow,
                        "Failed to launch whiteboard.\n" + ex.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // "Exit" button to cancel login
        JButton btnExit = new JButton("Exit");
        btnExit.setBounds(166, 189, 117, 29);
        btnExit.addActionListener(_ -> {
            System.out.println("Manager cancelled whiteboard creation. Exiting...");
            System.exit(0);
        });
        frmLoginWindow.getContentPane().add(btnExit);
    }
}
