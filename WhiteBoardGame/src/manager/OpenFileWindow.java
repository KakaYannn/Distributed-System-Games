package manager;

import java.io.File;
import javax.swing.*;

/**
 * OpenFileWindow provides a small dialog for the manager to input
 * a file path and open a saved whiteboard session (JSON format).
 * This window is triggered from the ManagerUI "Open" menu option.
 */
public class OpenFileWindow {

	/** The JFrame representing this dialog window. */
	JFrame frameOpen;

	/** Text field for user to input the JSON file path. */
	private JTextField jsonPath;

	/** Reference to the parent whiteboard manager UI for invoking file open logic. */
	private final ManagerUI whiteboard;

	/**
	 * Constructs the Open File window and binds it to the Manager UI.
	 * @param whiteboard the manager UI instance
	 */
	public OpenFileWindow(ManagerUI whiteboard) {
		this.whiteboard = whiteboard;
		initialize();
	}

	/**
	 * Initializes the GUI layout and logic for the file open window.
	 */
	private void initialize() {
		frameOpen = new JFrame();
		frameOpen.setTitle("Open from");
		frameOpen.setBounds(100, 100, 450, 300);
		frameOpen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameOpen.getContentPane().setLayout(null);

		// Label
		JLabel lblNewLabel = new JLabel("Open from: ");
		lblNewLabel.setBounds(42, 41, 84, 16);
		frameOpen.getContentPane().add(lblNewLabel);

		// Input field for file path
		jsonPath = new JTextField();
		jsonPath.setBounds(138, 36, 223, 26);
		frameOpen.getContentPane().add(jsonPath);
		jsonPath.setColumns(10);

		// "Continue" button: opens the specified file
		JButton btnContinue = new JButton("Continue");
		btnContinue.addActionListener(_ ->  {
			String input = jsonPath.getText().trim();

			// Validate non-empty input
			if (input.isEmpty()) {
				JOptionPane.showMessageDialog(frameOpen, "Please enter a filename.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Determine full file path
			String path = input.startsWith("./") || input.startsWith("/") ? input : "./save/" + input;

			// Check if file exists
			File file = new File(path);
			if (!file.exists()) {
				JOptionPane.showMessageDialog(frameOpen, "File not found: " + path, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Attempt to open and load the whiteboard file
			try {
				whiteboard.openFile(path);
				JOptionPane.showMessageDialog(frameOpen, "Opened successfully: " + path, "Success", JOptionPane.INFORMATION_MESSAGE);
				frameOpen.dispose();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(frameOpen, "Failed to open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnContinue.setBounds(149, 97, 117, 29);
		frameOpen.getContentPane().add(btnContinue);

		// "Back" button: closes the window without doing anything
		JButton btnBack = new JButton("Back");
		btnBack.setBounds(149, 157, 117, 29);
		btnBack.addActionListener(_ -> frameOpen.dispose());
		frameOpen.getContentPane().add(btnBack);
	}
}
