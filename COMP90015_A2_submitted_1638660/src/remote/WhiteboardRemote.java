package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for RMI clients (guests).
 * This interface allows the whiteboard server to send messages (in JSON format)
 * to any connected client using RMI.
 */
public interface WhiteboardRemote extends Remote {

    /**
     * Sends a message to the remote guest client.
     * The message is expected to be a JSON-formatted string,
     * which the client will parse and handle accordingly.
     *
     * @param json the JSON message to be received and handled by the client
     * @throws RemoteException if a communication error occurs
     */
    void receiveMessage(String json) throws RemoteException;
}
