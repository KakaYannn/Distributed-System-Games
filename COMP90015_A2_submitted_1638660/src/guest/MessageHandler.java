package guest;

import util.Message;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * MessageHandler processes all incoming messages received remotely by a guest client.
 * It interprets message types and applies appropriate updates to the guest GUI,
 * canvas, user list, and session state.
 */
public class MessageHandler {

    /**
     * Handles an incoming message from the manager or server.
     * Dispatches control based on the message type.
     * @param msg the parsed Message object
     */
    public static void handle(Message msg) {
        switch (msg.type) {

            case "draw": {
                // Apply a single drawing record to the guest canvas
                String record = (String) msg.data.get("record");
                GuestUI.listener.update(record);
                SwingUtilities.invokeLater(() -> GuestUI.canvas.repaint());
                break;
            }

            case "batch": {
                // Apply a full set of drawing records (e.g., when a guest joins)
                String records = (String) msg.data.get("records");
                String[] lines = records.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        GuestUI.listener.update(line);
                    }
                }
                SwingUtilities.invokeLater(() -> GuestUI.canvas.repaint());
                break;
            }

            case "chat": {
                // Display an incoming chat message in the chat panel
                String sender = msg.sender;
                String content = (String) msg.data.get("message");
                GuestUI.chatBox.setText(GuestUI.chatBox.getText() + "\n" + sender + ": " + content);
                GuestUI.chatBox.setCaretPosition(GuestUI.chatBox.getDocument().getLength());
                break;
            }

            case "userList": {
                // Update the guest-side display of all connected users
                try {
                    ArrayList<?> raw = (ArrayList<?>) msg.data.get("members");
                    String[] members = raw.stream().map(Object::toString).toArray(String[]::new);
                    System.out.println("Received userList: " + Arrays.toString(members));
                    GuestUI.gui.list.setListData(members);
                } catch (Exception e) {
                    System.out.println("Failed to update userList: " + e.getMessage());
                }
                break;
            }

            case "kick": {
                // Notify guest that they have been kicked, then exit the application
                JOptionPane.showMessageDialog(GuestUI.gui.frmGuestBoard, "You have been kicked out.");
                System.exit(0);
                break;
            }

            case "clientOut": {
                // Notify guest that another guest has left the session
                String name = (String) msg.data.get("name");
                new Thread(() -> JOptionPane.showMessageDialog(GuestUI.gui.frmGuestBoard, "guest " + name + " leaves!")).start();
                break;
            }

            case "new": {
                // Clear the guest canvas for a new whiteboard session
                GuestUI.canvas.removeAll();
                GuestUI.canvas.updateUI();
                GuestUI.listener.clearRecord();
                break;
            }

            case "rename": {
                // Update the guest's displayed username if it has been changed by the server
                String newName = (String) msg.data.get("newName");
                GuestUI.gui.username = newName;
                GuestUI.gui.frmGuestBoard.setTitle("Whiteboard (" + newName + ")");
                JoinWindow.username = newName;
                break;
            }

            case "shutdown": {
                // Handle host shutdown notification and exit guest session
                String info = (String) msg.data.get("message");
                JOptionPane.showMessageDialog(
                        GuestUI.gui.frmGuestBoard,
                        info != null ? info : "The host has closed the whiteboard.",
                        "Session Closed",
                        JOptionPane.WARNING_MESSAGE
                );
                System.exit(0);
                break;
            }

            default:
                // Catch any unsupported or unexpected message types
                System.out.println("Unknown message type: " + msg.type);
        }
    }
}
