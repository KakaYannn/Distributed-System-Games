package guest;

import common.AbstractCanvasListener;
import util.Message;

import javax.swing.*;

/**
 * GuestCanvasListener extends the abstract drawing logic for guest users.
 * It handles local updates to the drawing record and forwards drawing messages
 * to the manager's server via remote broadcast.
 */
public class GuestCanvasListener extends AbstractCanvasListener {

    /**
     * Constructs a listener for the guest's drawing canvas.
     * @param frame the parent window used for dialogs and UI context
     * @param canvas the JPanel on which the guest draws
     * @param username the guest user's display name
     */
    public GuestCanvasListener(JFrame frame, JPanel canvas, String username) {
        super(frame, canvas, username);
    }

    /**
     * Sends a drawing command from the guest to the manager via RMI.
     * Also appends the drawing record locally and repaints the canvas.
     * If the broadcast fails, an error dialog is shown.
     * @param record the drawing instruction string to send
     */
    @Override
    protected void sendDrawMessage(String record) {
        update(record);
        try {
            Message msg = new Message("draw", username);
            msg.data.put("record", record);
            GuestUI.server.broadcast(msg.toJson());
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "The manager may has disconnected. Exiting.");
                System.exit(0);
            });
        }
        canvas.repaint();
    }
}
