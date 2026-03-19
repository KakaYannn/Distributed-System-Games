package manager;

import common.AbstractCanvasListener;
import remote.WhiteboardServer;
import util.Message;

import javax.swing.*;

/**
 * ManagerCanvasListener handles drawing events on the manager side of the whiteboard.
 * It updates the local record and broadcasts drawing instructions to all connected guests via the server.
 */
public class ManagerCanvasListener extends AbstractCanvasListener {

    /** Remote reference to the whiteboard server for broadcasting drawing events. */
    private final WhiteboardServer server;

    /**
     * Constructs a manager-side canvas listener.
     * @param frame the main UI frame
     * @param canvas the canvas component where drawing occurs
     * @param username the manager's username
     * @param server the remote server interface used to broadcast draw messages
     */
    public ManagerCanvasListener(JFrame frame, JPanel canvas, String username, WhiteboardServer server) {
        super(frame, canvas, username);
        this.server = server;
    }

    /**
     * Sends a drawing command by updating the local record and broadcasting to all guests.
     * Repaints the canvas locally and shows an error dialog on failure.
     * @param record the drawing instruction string
     */
    @Override
    protected void sendDrawMessage(String record) {
        update(record);
        try {
            Message msg = new Message("draw", username);
            msg.data.put("record", record);
            server.broadcast(msg.toJson());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to broadcast drawing.\n" + e.getMessage());
        }
        canvas.repaint();
    }
}
