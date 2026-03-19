package client;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * The ClientUI class is responsible for rendering and managing the client-side GUI of the 
 * distributed dictionary system.
 *
 * It allows the user to perform dictionary operations. Each operation generates a corresponding 
 * JSON command and sends it to the server using the TCPInteractiveClient instance, which handles 
 * socket communication in a separate thread.
 *
 * The UI also displays server responses, provides meaningful user feedback,
 * and disables all buttons if the server is unreachable.
 *
 * This class interacts directly with:
 * - TCPInteractiveClient (handles backend communication)
 * - ClientLauncher (the class with the main method that launches this UI)
 */


public class ClientUI {
	
	// Main window frame
    JFrame frmDic;
    
    // UI fields and buttons
    private static JTextArea textAreaResult;
    private static JButton btnADD;
    private static JButton btnREMOVE;
    private static JButton btnAddMeaning;
    private static JButton btnQUERY;
    private static JButton btnUPDATE;
    private JTextField textFieldNewMeaning;
    private JTextField textFieldOldMeaning;
    private JTextField textFieldWord;
    
    TCPInteractiveClient client = new TCPInteractiveClient();

    /**
     * Constructor that starts the client communication thread and initializes the GUI.
     */
    public ClientUI() {
        Thread t1 = new Thread(client);
        t1.start();
        initialize();
    }

    /**
     * Initializes the GUI window and its components.
     */
    private void initialize() {
    	// Frame settings
        frmDic = new JFrame();
        frmDic.getContentPane().setBackground(new Color(255, 255, 204));
        frmDic.getContentPane().setForeground(new Color(255, 255, 204));
        frmDic.setBackground(new Color(255, 255, 204));
        frmDic.setForeground(new Color(255, 255, 204));
        frmDic.setTitle("CLIENT UI");
        frmDic.setBounds(100, 100, 514, 408);
        frmDic.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmDic.getContentPane().setLayout(null);

        // Labels and input fields
        JLabel lblNewLabel = new JLabel("Dictionary for you");
        lblNewLabel.setForeground(new Color(255, 0, 204));
        lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
        lblNewLabel.setBounds(174, 17, 148, 16);
        frmDic.getContentPane().add(lblNewLabel);

        JLabel lblWordInput = new JLabel("Input Word:");
        lblWordInput.setForeground(new Color(255, 153, 204));
        lblWordInput.setBounds(56, 62, 96, 16);
        frmDic.getContentPane().add(lblWordInput);

        textFieldWord = new JTextField();
        textFieldWord.setForeground(new Color(255, 0, 204));
        textFieldWord.setBounds(210, 57, 237, 26);
        frmDic.getContentPane().add(textFieldWord);
        textFieldWord.setColumns(10);

        JLabel lblNewLabel_2 = new JLabel("Result:");
        lblNewLabel_2.setForeground(new Color(255, 153, 204));
        lblNewLabel_2.setBounds(56, 233, 61, 16);
        frmDic.getContentPane().add(lblNewLabel_2);

        // Result display area (non-editable)
        textAreaResult = new JTextArea();
        textAreaResult.setForeground(new Color(255, 0, 204));
        textAreaResult.setText("results will be displayed here...");
        textAreaResult.setLineWrap(true);
        textAreaResult.setEditable(false);
        textAreaResult.setBounds(210, 227, 237, 75);
        frmDic.getContentPane().add(textAreaResult);

        JScrollPane scrollPaneResult = new JScrollPane(textAreaResult);
        scrollPaneResult.setBounds(210, 227, 237, 75);
        frmDic.getContentPane().add(scrollPaneResult);

        // Buttons for each dictionary operation
        btnADD = new JButton("ADD");
        btnADD.setBackground(new Color(204, 255, 255));
        btnADD.setForeground(new Color(255, 0, 204));
        btnADD.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	// Validate input and build JSON command
                String word = textFieldWord.getText();
                String newMeaning = textFieldNewMeaning.getText();
                if (word.isEmpty()) {
                    textAreaResult.setText("Please input the word!");
                } else if (newMeaning.isEmpty()) {
                    textAreaResult.setText("Please input the meaning!");
                } else {
                    try {
                    	// Send request to client logic thread
                        String command = TCPInteractiveClient.createJsonCommand("ADD", word.toLowerCase(), newMeaning, "");
                        while (client.requestQueue.size() == 0) {
                            client.requestQueue.put(command);
                            System.out.println(command);
                            break;
                        }

                        // Wait for response and update UI accordingly
                        String response = client.getResponse();
                        if (response == null) {
                            textAreaResult.setText("Something Wrong. Please try later!");
                        } else if (response.equals("unsuccess")) {
                            textAreaResult.setText("The word is already existed!");
                        } else if (response.equals("success")) {
                            textAreaResult.setText("Add the word successfully!");
                            textFieldWord.setText("");
                        }
                        textFieldNewMeaning.setText("");
                        textFieldOldMeaning.setText("");
                    } catch (Exception excption) {
                        ClientUI.setResultText("Failed to send ADD request. Please try again.");
                    }
                }

            }
        });
        btnADD.setBounds(35, 321, 82, 26);
        btnADD.setToolTipText("Add a new word to the dictionary.");
        frmDic.getContentPane().add(btnADD);

        // REMOVE button
        btnREMOVE = new JButton("REMOVE");
        btnREMOVE.setForeground(new Color(255, 0, 204));
        btnREMOVE.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String word = textFieldWord.getText();
                if (word.isEmpty()) {
                    textAreaResult.setText("Please input the word!");
                } else {
                    try {
                        String command = TCPInteractiveClient.createJsonCommand("DELETE", word.toLowerCase(), "", "");
                        while (client.requestQueue.size() == 0) {
                            client.requestQueue.put(command);
                            break;
                        }

                        String response = client.getResponse();
                        if (response == null) {
                            textAreaResult.setText("Something Wrong. Please try later!");
                        } else if (response.equals("unsuccess")) {
                            textAreaResult.setText("The word NOT exists!");
                        } else if (response.equals("success")) {
                            textAreaResult.setText("Remove the word successfully!");
                            textFieldWord.setText("");
                        }
                        textFieldNewMeaning.setText("");
                        textFieldOldMeaning.setText("");
                    } catch (Exception excption) {
                        ClientUI.setResultText("Failed to send REMOVE request. Please try again.");
                    }
                }

            }
        });
        btnREMOVE.setBounds(116, 321, 82, 27);
        btnREMOVE.setToolTipText("Remove an existing word from the dictionary.");
        frmDic.getContentPane().add(btnREMOVE);

        JLabel lblMeaningInput = new JLabel("Input New Meaning:");
        lblMeaningInput.setForeground(new Color(255, 153, 204));
        lblMeaningInput.setBounds(56, 100, 142, 16);
        frmDic.getContentPane().add(lblMeaningInput);

        textFieldNewMeaning = new JTextField();
        textFieldNewMeaning.setForeground(new Color(255, 0, 204));
        textFieldNewMeaning.setBounds(210, 95, 237, 54);
        frmDic.getContentPane().add(textFieldNewMeaning);
        textFieldNewMeaning.setColumns(10);

        JLabel lblOldMeaning = new JLabel("<html>Input Old Meaning: <br>(to be updated)</html>)");
        lblOldMeaning.setForeground(new Color(255, 153, 204));
        lblOldMeaning.setHorizontalAlignment(SwingConstants.LEFT);
        lblOldMeaning.setVerticalAlignment(SwingConstants.TOP);
        lblOldMeaning.setBounds(56, 165, 129, 50);
        frmDic.getContentPane().add(lblOldMeaning);

        textFieldOldMeaning = new JTextField();
        textFieldOldMeaning.setForeground(new Color(255, 0, 204));
        textFieldOldMeaning.setBounds(210, 161, 237, 54);
        frmDic.getContentPane().add(textFieldOldMeaning);
        textFieldOldMeaning.setColumns(10);

        // QUERY button
        btnQUERY = new JButton("QUERY");
        btnQUERY.setForeground(new Color(255, 0, 204));
        btnQUERY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String word = textFieldWord.getText();
                if (word.isEmpty()) {
                    textAreaResult.setText("Please input the word!");
                } else {
                    try {
                        String command = TCPInteractiveClient.createJsonCommand("QUERY", word.toLowerCase(), "", "");
                        while (client.requestQueue.size() == 0) {
                            client.requestQueue.put(command);
                            break;
                        }

                        String response = client.getResponse();
                        if (response == null) {
                            textAreaResult.setText("Something Wrong. Please try later!");
                        } else if (response.equals("unsuccess")) {
                            textAreaResult.setText("The word is not found!");
                        } else {
                            textAreaResult.setText(response.replaceAll(";", ";\n"));
                            textFieldWord.setText("");
                        }
                        textFieldNewMeaning.setText("");
                        textFieldOldMeaning.setText("");
                    } catch (Exception excption) {
                        ClientUI.setResultText("Failed to send QUERY request. Please try again.");
                    }
                }
            }
        });
        btnQUERY.setBounds(197, 321, 82, 26);
        btnQUERY.setToolTipText("Query the meaning of an existing word.");
        frmDic.getContentPane().add(btnQUERY);

        // ADD_MEANING button
        btnAddMeaning = new JButton("ADD MEANING");
        btnAddMeaning.setForeground(new Color(255, 0, 204));
        btnAddMeaning.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String word = textFieldWord.getText();
                String newMeaning = textFieldNewMeaning.getText();
                if (word.isEmpty()) {
                    textAreaResult.setText("Please input the word!");
                } else if (newMeaning.isEmpty()) {
                    textAreaResult.setText("Please input the new meaning!");
                } else {
                    try {
                        String command = TCPInteractiveClient.createJsonCommand("ADD_MEANING", word.toLowerCase(), newMeaning, "");
                        while (client.requestQueue.size() == 0) {
                            client.requestQueue.put(command);
                            break;
                        }

                        String response = client.getResponse();
                        if (response == null) {
                            textAreaResult.setText("Something Wrong. Please try later!");
                        } else if (response.equals("unsuccess")) {
                            textAreaResult.setText("The word is NOT found!");
                        } else if (response.equals("success")) {
                            textAreaResult.setText("Add the meaning successfully!");
                            textFieldWord.setText("");
                        } else if (response.equals("sameMeaning")) {
                            textAreaResult.setText("The meaning is already existed!");
                        }
                        textFieldNewMeaning.setText("");
                        textFieldOldMeaning.setText("");
                    } catch (Exception excption) {
                        ClientUI.setResultText("Failed to send ADD_MEANING request. Please try again.");
                    }
                }
            }
        });
        btnAddMeaning.setBounds(277, 320, 117, 29);
        btnAddMeaning.setToolTipText("Add a new meaning to an existing word.");
        frmDic.getContentPane().add(btnAddMeaning);

        // UPDATE button
        btnUPDATE = new JButton("UPDATE");
        btnUPDATE.setForeground(new Color(255, 0, 204));
        btnUPDATE.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String word = textFieldWord.getText();
                String oldMeaning = textFieldOldMeaning.getText();
                String newMeaning = textFieldNewMeaning.getText();
                if (word.isEmpty()) {
                    textAreaResult.setText("Please input the word!");
                } else if (oldMeaning.isEmpty()) {
                    textAreaResult.setText("Please input the old meaning!");
                } else if (newMeaning.isEmpty()) {
                    textAreaResult.setText("Please input the new meaning!");
                } else {
                    try {
                        String command = TCPInteractiveClient.createJsonCommand("UPDATE", word.toLowerCase(), newMeaning, oldMeaning);
                        while (client.requestQueue.size() == 0) {
                            client.requestQueue.put(command);
                            break;
                        }
                        String response = client.getResponse();
                        if (response == null) {
                            textAreaResult.setText("Something Wrong. Please try later!");
                        } else if (response.equals("unsuccess")) {
                            textAreaResult.setText("The word or meaning is NOT found!");
                        } else if (response.equals("success")) {
                            textAreaResult.setText("Update the meaning successfully!");
                            textFieldWord.setText("");
                        } else if (response.equals("sameMeaning")) {
                            textAreaResult.setText("The meaning is already existed!");
                        }
                        textFieldNewMeaning.setText("");
                        textFieldOldMeaning.setText("");
                    } catch (Exception excption) {
                        ClientUI.setResultText("Failed to send UPDATE request. Please try again.");
                    }
                }
            }
        });
        btnUPDATE.setBounds(392, 320, 82, 29);
        btnUPDATE.setToolTipText("Update the meaning of an existing word.");
        frmDic.getContentPane().add(btnUPDATE);
    }
    

    /**
     * Utility method to set result display message.
     */
    
    public static void setResultText(String message) {
        textAreaResult.setText(message);
    }
    
    /**
     * Disable all action buttons — useful when disconnected.
     */

    public static void disableAllButtons() {
        btnADD.setEnabled(false);
        btnREMOVE.setEnabled(false);
        btnQUERY.setEnabled(false);
        btnAddMeaning.setEnabled(false);
        btnUPDATE.setEnabled(false);
    }

}
