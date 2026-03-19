package manager;

import java.awt.EventQueue;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import javax.swing.*;

/**
 * LoginWindow is the entry point for launching the whiteboard as the manager.
 * It collects the manager's username, initializes the RMI registry and service,
 * and starts the Manager UI upon successful setup.
 */
public class LoginWindow {

    /** RMI server address (typically "localhost"). */
    static String address;

    /** Port for RMI registry binding. */
    static int port;

    /** Username of the manager (displayed in canvas). */
    static String username;

    /** Reference to the manager UI once initialized. */
    public static ManagerUI createWhiteBoard;

    /** Login window frame. */
    private JFrame frmLoginWindow;

    /** Text field for entering the manager username. */
    private JTextField textField;

    /**
     * Application entry point for the manager.
     * Accepts optional command-line arguments: <address> <port> <username>
     */
    public static void main(String[] args) {

        if (args.length >= 3) {
            try {
                address = args[0];
                port = Integer.parseInt(args[1]);
                username = args[2];
            } catch (Exception e) {
                System.out.println("Invalid startup parameters. Expected: <address> <port> <username>");
                System.exit(1);
            }
        } else {
            address = "localhost";
            port = 1099;
            username = "admin";
            System.out.println("Application is launched by default.");
        }

        EventQueue.invokeLater(() -> {
            try {
                LoginWindow window = new LoginWindow();
                window.frmLoginWindow.setVisible(true);
            } catch (Exception e) {
                System.out.println("Failed to initialize login window.");
                JOptionPane.showMessageDialog(null,
                        "Login window failed to launch.\n" + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /** Constructs the login window and initializes its components. */
    public LoginWindow() {
        initialize();
    }

    /**
     * Initializes and configures the login window layout and event handling.
     * Sets up RMI registry and binds the whiteboard server object.
     */
    private void initialize() {
        frmLoginWindow = new JFrame();
        frmLoginWindow.setTitle("Login Window");
        frmLoginWindow.setBounds(100, 100, 450, 300);
        frmLoginWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmLoginWindow.getContentPane().setLayout(null);

        JLabel lblInputPrompt = new JLabel("Input your username displayed in the canvas room:");
        lblInputPrompt.setBounds(41, 25, 358, 16);
        frmLoginWindow.getContentPane().add(lblInputPrompt);

        textField = new JTextField();
        textField.setBounds(41, 58, 358, 26);
        frmLoginWindow.getContentPane().add(textField);
        textField.setText(username);

        // "Continue" button triggers RMI setup and whiteboard launch
        JButton btnContinue = new JButton("Continue");
        btnContinue.setBounds(166, 127, 117, 29);
        frmLoginWindow.getContentPane().add(btnContinue);

        btnContinue.addActionListener(_ -> {
            String input = textField.getText().trim();
            if (!input.isEmpty()) {
                username = input + "(manager)";
            }

            try {
                // Start RMI registry and bind whiteboard server
                LocateRegistry.createRegistry(port);
                WhiteboardServerImpl server = new WhiteboardServerImpl();
                Naming.rebind("rmi://"+address+":" + port + "/WhiteboardService", server);
                System.out.println("RMI service bound on port " + port);

                frmLoginWindow.dispose();
                createWhiteBoard = new ManagerUI(username, server);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frmLoginWindow, "Failed to start RMI server.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });

        // "Exit" button allows manager to cancel the session
        JButton btnExit = new JButton("Exit");
        btnExit.setBounds(166, 189, 117, 29);
        btnExit.addActionListener(_ -> {
            System.out.println("Manager cancelled whiteboard creation. Exiting...");
            System.exit(0);
        });
        frmLoginWindow.getContentPane().add(btnExit);
    }
}
