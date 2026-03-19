package util;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

/**
 * A unified message structure used for all socket and RMI communication
 * between clients and the whiteboard server.
 * Each message includes a message type, optional sender identifier,
 * and a flexible data map payload. The structure is designed to be
 * serialized and deserialized using Gson.
 */
public class Message {

    /** The type of the message (e.g., "chat", "draw", "userList", etc.). */
    public String type;

    /** The name of the sender (optional; may be null or system-generated). */
    public String sender;

    /** A flexible key-value store for additional message-specific data. */
    public Map<String, Object> data;

    /**
     * Constructs a message with a specified type.
     * @param type the message type
     */
    public Message(String type) {
        this.type = type;
        this.data = new HashMap<>();
    }

    /**
     * Constructs a message with a specified type and sender.
     * @param type   the message type
     * @param sender the sender's name
     */
    public Message(String type, String sender) {
        this.type = type;
        this.sender = sender;
        this.data = new HashMap<>();
    }

    /**
     * Serializes this message to a JSON-formatted string.
     * @return JSON representation of the message
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Deserializes a JSON-formatted string into a Message object.
     * @param json the JSON string to parse
     * @return the corresponding Message object
     */
    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }

}
