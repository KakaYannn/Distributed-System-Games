package manager;

import remote.WhiteboardRemote;
import remote.WhiteboardServer;
import util.Message;

import javax.swing.*;
import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the WhiteboardServer remote interface.
 * Manages client registration, communication, broadcasting messages,
 * and administrative tasks like kicking users or updating member lists.
 */
public class WhiteboardServerImpl extends UnicastRemoteObject implements WhiteboardServer {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Stores all connected clients with their usernames mapped to remote stubs. */
    private final Map<String, WhiteboardRemote> clients = new ConcurrentHashMap<>();

    /** Incremental ID generator for assigning unique usernames to guests. */
    private final AtomicInteger clientID = new AtomicInteger(1);

    /** Thread pool used for asynchronous message broadcasting to clients. */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Constructs a new WhiteboardServerImpl and prepares client management.
     */
    public WhiteboardServerImpl() throws RemoteException {
        super();
        System.out.println("WhiteboardServerImpl constructed. clients = " + clients);
    }

    /**
     * Initializes a new guest after the manager accepts their request.
     * Sends the guest a rename message and initial batch of whiteboard content.
     */
    @Override
    public synchronized void handleBegin(String senderName) throws RemoteException {
        WhiteboardRemote guest = clients.get(senderName);
        if (guest == null) {
            System.out.println("No such client: " + senderName);
            return;
        }

        // Inform guest of their accepted name
        Message rename = new Message("rename");
        rename.data.put("newName", senderName);
        guest.receiveMessage(rename.toJson());

        // Send full canvas drawing history
        ArrayList<String> records = ManagerUI.listener.getRecord();
        String joined = String.join("\n", records);
        Message batch = new Message("batch");
        batch.data.put("records", joined);
        guest.receiveMessage(batch.toJson());

        // Update all users with the new member list
        updateUserList();
        System.out.println("Initialization + userList sent for: " + senderName);
    }

    /**
     * Handles join requests from new guests. Prompts the manager for approval.
     */
    @Override
    public synchronized String requestJoin(String requestedName, WhiteboardRemote guest) throws RemoteException {
        int id = clientID.getAndIncrement();
        String actualName = requestedName + "(" + id + ")";

        int option = JOptionPane.showConfirmDialog(
                null,
                actualName + " wants to join your whiteboard.",
                "Join Request",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            clients.put(actualName, guest);
            System.out.println("Accepted guest: " + actualName);
            return actualName;
        } else {
            System.out.println("Rejected guest: " + actualName);
            clientID.decrementAndGet();
            return null;
        }
    }

    /**
     * Broadcasts a JSON-formatted message to all connected clients.
     * Also updates the local manager UI accordingly.
     */
    @Override
    public synchronized void broadcast(String json) throws RemoteException {
        try {
            Message msg = Message.fromJson(json);

            // Process local updates on manager side
            switch (msg.type) {
                case "draw" -> {
                    String rec = (String) msg.data.get("record");
                    ManagerUI.listener.update(rec);
                    ManagerUI.canvas.repaint();
                }
                case "chat" -> {
                    String sender = msg.sender;
                    String content = (String) msg.data.get("message");
                    ManagerUI.chatBox.append("\n" + sender + ": " + content);
                    ManagerUI.chatBox.setCaretPosition(ManagerUI.chatBox.getDocument().getLength());
                }
                case "userList" -> {
                    try {
                        ArrayList<?> raw = (ArrayList<?>) msg.data.get("members");
                        String[] usernames = raw.stream().map(Object::toString).toArray(String[]::new);
                        LoginWindow.createWhiteBoard.list.setListData(usernames);
                    } catch (Exception e) {
                        System.out.println("Manager userList update failed: " + e.getMessage());
                    }
                }
                case "clientOut" -> {
                    String guestName = (String) msg.data.get("name");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            LoginWindow.createWhiteBoard.frmManagerBoard,
                            "User " + guestName + " has left the whiteboard.",
                            "User Left",
                            JOptionPane.INFORMATION_MESSAGE
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Local broadcast pre-processing failed: " + e.getMessage());
        }

        // Distribute message to all guests (in parallel)
        for (Map.Entry<String, WhiteboardRemote> entry : clients.entrySet()) {
            String name = entry.getKey();
            WhiteboardRemote stub = entry.getValue();

            executor.submit(() -> {
                try {
                    stub.receiveMessage(json);
                } catch (RemoteException e) {
                    System.out.println("Failed to send message to: " + name + " Reason: " + e.getMessage());
                    clients.remove(name);

                    Message disconnectMsg = new Message("chat", "system");
                    disconnectMsg.data.put("message", name + " has disconnected unexpectedly.");
                    try {
                        broadcast(disconnectMsg.toJson());
                    } catch (RemoteException ex) {
                        System.out.println("Failed to broadcast disconnect message: " + ex.getMessage());
                    }

                    try {
                        updateUserList();
                    } catch (RemoteException ex2) {
                        System.out.println("Failed to update user list after removing: " + name);
                    }
                }
            });
        }
    }

    /**
     * Kicks a guest from the whiteboard, removes them from the client list,
     * and notifies all participants.
     */
    @Override
    public synchronized void kickGuest(String name) throws RemoteException {
        WhiteboardRemote guestStub = clients.get(name);
        if (guestStub != null) {
            new Thread(() -> {
                try {
                    Message kickMsg = new Message("kick");
                    guestStub.receiveMessage(kickMsg.toJson());
                } catch (Exception ex) {
                    System.out.println("Guest '" + name + "' may not have received kick: " + ex.getMessage());
                }
            }).start();
        }

        clients.remove(name);
        updateUserList();

        JOptionPane.showMessageDialog(LoginWindow.createWhiteBoard.frmManagerBoard,
                "User " + name + " has been kicked out.");

        Message systemMsg = new Message("chat", "System");
        systemMsg.data.put("message", name + " has been kicked from the whiteboard.");
        broadcast(systemMsg.toJson());
    }

    /**
     * Removes a client from the whiteboard due to voluntary disconnection.
     */
    @Override
    public synchronized void removeClient(String name) throws RemoteException {
        clients.remove(name);
        updateUserList();

        Message msg = new Message("clientOut");
        msg.data.put("name", name);
        broadcast(msg.toJson());
    }

    /**
     * Broadcasts the current list of connected users to all clients.
     */
    public void updateUserList() throws RemoteException {
        ArrayList<String> members = new ArrayList<>(clients.keySet());
        members.add(LoginWindow.username);  // Include manager
        Message msg = new Message("userList");
        msg.data.put("members", members);
        broadcast(msg.toJson());
    }
}
