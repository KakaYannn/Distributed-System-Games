package manager;

import javax.swing.*;
import java.io.File;

/**
 * SaveWindow provides a dialog allowing the manager to save the current whiteboard
 * as a JSON file. Unlike SaveAsWindow, it only handles JSON (no image export).
 */
public class SaveWindow {

	/** The JFrame representing this save dialog. */
	JFrame frameSave;

	/** Input field for entering the filename to save as. */
	private JTextField textField;

	/** Reference to the ManagerUI instance to perform the save operation. */
	private final ManagerUI whiteboard;

	/**
	 * Constructs the SaveWindow dialog and binds it to the provided whiteboard UI.
	 * @param whiteboard the ManagerUI responsible for saving the file
	 */
	public SaveWindow(ManagerUI whiteboard) {
		this.whiteboard = whiteboard;
		initialize();
	}

	/**
	 * Initializes the GUI layout and save behavior.
	 */
	private void initialize() {
		frameSave = new JFrame();
		frameSave.setTitle("Save As");
		frameSave.setBounds(100, 100, 356, 208);
		frameSave.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameSave.getContentPane().setLayout(null);

		// Label
		JLabel saveLabel = new JLabel("Save as: ");
		saveLabel.setBounds(49, 35, 61, 16);
		frameSave.getContentPane().add(saveLabel);

		// Input field for filename
		textField = new JTextField();
		textField.setBounds(110, 30, 130, 26);
		frameSave.getContentPane().add(textField);
		textField.setColumns(10);

		// "Continue" button: triggers file saving
		JButton btnContinue = new JButton("Continue");
		btnContinue.addActionListener(_ ->  {
			String fileName = textField.getText().trim();

			// Validate filename input
			if (fileName.isEmpty()) {
				JOptionPane.showMessageDialog(frameSave, "Filename cannot be empty.");
				return;
			}
			if (fileName.matches(".*[\\\\/:*?\"<>|].*")) {
				JOptionPane.showMessageDialog(frameSave, "Filename contains invalid characters.");
				return;
			}

			// Build full file path
			String filePath = "./save/" + fileName + ".json";
			File file = new File(filePath);

			// Confirm overwrite if file already exists
			if (file.exists()) {
				int result = JOptionPane.showConfirmDialog(frameSave,
						"File already exists. Overwrite?",
						"Confirm Overwrite", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
			}

			// Perform the save operation
			whiteboard.saveFile(filePath);
			JOptionPane.showMessageDialog(frameSave, "Saved successfully to " + filePath);
			frameSave.dispose();
		});
		btnContinue.setBounds(123, 79, 117, 29);
		frameSave.getContentPane().add(btnContinue);

		// "Back" button: closes the dialog
		JButton btnBack = new JButton("Back");
		btnBack.setBounds(123, 120, 117, 29);
		btnBack.addActionListener(_ -> frameSave.dispose());
		frameSave.getContentPane().add(btnBack);
	}
}
