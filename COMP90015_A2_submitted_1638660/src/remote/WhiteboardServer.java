package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface defining server-side operations exposed to RMI clients (guests).
 * Clients use this interface to interact with the shared whiteboard system,
 * including joining, broadcasting messages, and managing connection status.
 */
public interface WhiteboardServer extends Remote {

    /**
     * Requests to join the whiteboard session with a desired username.
     * The server prompts the manager to approve or reject the request.
     *
     * @param name   the requested username from the guest
     * @param guest  the guest's remote stub for callback
     * @return the assigned username if accepted, or null if rejected
     * @throws RemoteException if communication fails
     */
    String requestJoin(String name, WhiteboardRemote guest) throws RemoteException;

    /**
     * Broadcasts a JSON-formatted message to all connected clients.
     * The message may represent drawings, chat, system events, etc.
     *
     * @param json the message in JSON format
     * @throws RemoteException if communication fails
     */
    void broadcast(String json) throws RemoteException;

    /**
     * Notifies the server that a client has disconnected and should be removed.
     *
     * @param name the username of the departing client
     * @throws RemoteException if communication fails
     */
    void removeClient(String name) throws RemoteException;

    /**
     * Triggers a full user list update, broadcasting the current list
     * of connected usernames to all participants.
     *
     * @throws RemoteException if communication fails
     */
    void updateUserList() throws RemoteException;

    /**
     * Handles post-join initialization for a newly accepted guest.
     * Sends them the initial drawing state and user list.
     *
     * @param sender the accepted guest's username
     * @throws RemoteException if communication fails
     */
    void handleBegin(String sender) throws RemoteException;

    /**
     * Removes a specific guest from the whiteboard (e.g., via kick action).
     *
     * @param name the username of the guest to be removed
     * @throws RemoteException if communication fails
     */
    void kickGuest(String name) throws RemoteException;
}
