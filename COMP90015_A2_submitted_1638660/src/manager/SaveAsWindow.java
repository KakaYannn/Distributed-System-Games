package manager;

import java.io.File;
import javax.swing.*;

/**
 * SaveAsWindow provides a dialog for the manager to specify a filename
 * and select an image format (.jpg or .png) to save the current whiteboard.
 * It saves both the drawing instructions as JSON and a rendered image file.
 */
public class SaveAsWindow {

	/** The JFrame representing this Save As dialog. */
	JFrame frameSaveAs;

	/** Input field for entering the desired filename. */
	private JTextField textField;

	/** Reference to the ManagerUI for accessing save logic. */
	private final ManagerUI whiteboard;

	/**
	 * Constructs a SaveAsWindow bound to the given whiteboard UI.
	 * @param whiteboard the ManagerUI instance responsible for saving
	 */
	public SaveAsWindow(ManagerUI whiteboard) {
		this.whiteboard = whiteboard;
		initialize();
	}

	/**
	 * Initializes the Save As dialog GUI and file saving logic.
	 */
	private void initialize() {
		frameSaveAs = new JFrame();
		frameSaveAs.setTitle("Save As");
		frameSaveAs.setBounds(100, 100, 356, 208);
		frameSaveAs.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameSaveAs.getContentPane().setLayout(null);

		// Label
		JLabel saveAsLabel = new JLabel("Save as: ");
		saveAsLabel.setBounds(49, 35, 61, 16);
		frameSaveAs.getContentPane().add(saveAsLabel);

		// Format dropdown (.jpg or .png)
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<>(new String[] {".jpg", ".png"}));
		comboBox.setBounds(239, 31, 74, 27);
		frameSaveAs.getContentPane().add(comboBox);

		// Filename input field
		textField = new JTextField();
		textField.setBounds(110, 30, 130, 26);
		frameSaveAs.getContentPane().add(textField);
		textField.setColumns(10);

		// "Continue" button: performs the actual save
		JButton btnContinue = new JButton("Continue");
		btnContinue.addActionListener(_ ->  {
			String fileName = textField.getText().trim();
			Object selectedItem = comboBox.getSelectedItem();
			if (selectedItem == null) {
				JOptionPane.showMessageDialog(frameSaveAs, "Please select a file format.");
				return;
			}
			String format = selectedItem.toString();

			// Validate filename input
			if (fileName.isEmpty()) {
				JOptionPane.showMessageDialog(frameSaveAs, "Filename cannot be empty.");
				return;
			}
			if (fileName.matches(".*[\\\\/:*?\"<>|].*")) {
				JOptionPane.showMessageDialog(frameSaveAs, "Filename contains invalid characters.");
				return;
			}

			// Ensure target save directory exists
			File parentDir = new File("./save/");
			if (!parentDir.exists()) {
				if (!parentDir.mkdirs()) {
					System.out.println("Failed to create directory: " + parentDir.getAbsolutePath());
					JOptionPane.showMessageDialog(
							frameSaveAs,
							"Failed to create directory: " + parentDir.getAbsolutePath(),
							"Directory Creation Error",
							JOptionPane.ERROR_MESSAGE
					);
					return;
				}
			}

			// Build target file paths
			String basePath = "./save/" + fileName;
			String jsonPath = basePath + ".json";
			String imgPath = basePath + format;

			// Check for overwrite confirmation
			File jsonFile = new File(jsonPath);
			File imgFile = new File(imgPath);
			if (jsonFile.exists() || imgFile.exists()) {
				int result = JOptionPane.showConfirmDialog(frameSaveAs,
						"File already exists. Overwrite?",
						"Confirm Overwrite", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
			}

			// Save JSON and image files
			whiteboard.saveFile(jsonPath);
			whiteboard.saveImg(basePath, format);

			// Confirmation message
			JOptionPane.showMessageDialog(frameSaveAs,
					"Saved successfully:\n" + jsonPath + "\n" + imgPath);
			frameSaveAs.dispose();
		});
		btnContinue.setBounds(123, 79, 117, 29);
		frameSaveAs.getContentPane().add(btnContinue);

		// "Back" button: closes the dialog
		JButton btnBack = new JButton("Back");
		btnBack.setBounds(123, 120, 117, 29);
		btnBack.addActionListener(_ -> frameSaveAs.dispose());
		frameSaveAs.getContentPane().add(btnBack);
	}
}
