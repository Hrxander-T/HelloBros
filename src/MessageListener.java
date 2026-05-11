/**
 * Bridge between network and UI.
 * This interface allows the Server/Client to communicate with the ChatScreen
 * without them being tightly coupled - they only communicate through these callbacks.
 *
 * How it works:
 * 1. ChatScreen implements this interface
 * 2. Server/Client receives a message from network
 * 3. Server/Client calls listener.onMessage(msg) to display it in the UI
 * 4. ChatScreen shows the message to the user
 */
public interface MessageListener {

    // Called when a new message arrives from the network
    // msg - the message text to display
    void onMessage(String msg);

    // Called when the connection to the server is lost
    // Used to show "Disconnected" message in the UI
    void onDisconnected();
}