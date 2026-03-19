package guest;

import remote.WhiteboardRemote;
import util.Message;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * GuestRemoteImpl implements the remote interface for receiving messages from the manager.
 * This class is bound to the RMI registry and provides the remote method for handling incoming JSON-formatted messages.
 */
public class GuestRemoteImpl extends UnicastRemoteObject implements WhiteboardRemote {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs the guest's RMI stub and exports it as a UnicastRemoteObject.
     * @throws RemoteException if RMI setup fails
     */
    public GuestRemoteImpl() throws RemoteException {
        super();
    }

    /**
     * Receives a JSON-formatted message from the manager over RMI.
     * Parses the message and delegates its handling to the local MessageHandler.
     * @param json the message in JSON string format
     * @throws RemoteException if a remote communication error occurs
    c */
    @Override
    public void receiveMessage(String json) throws RemoteException {
        try {
            Message msg = Message.fromJson(json);
            MessageHandler.handle(msg);
        } catch (Exception e) {
            System.out.println("Error in receiveMessage from manager: " + e.getMessage());
        }
    }
}
