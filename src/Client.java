import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client class - Handles connecting to a chat server.
 *
 * Flow:
 * 1. Connect to server at given address and port
 * 2. Send messages to the server
 * 3. Receive messages from the server and display in UI
 * 4. Can also send files to the server
 */
public class Client {

    // Server address (e.g., "localhost" or "bore.pub")
    private final String host;

    // Server port number
    private final int port;

    // User's display name (shown with their messages)
    private final String name;

    // Callback interface for displaying messages in the UI
    private final MessageListener listener;

    // Output stream for sending messages to server
    // (initialized in start() method after connection is established)
    private DataOutputStream dos;

    public Client(String host, int port, String name, MessageListener listener) {
        this.host     = host;
        this.port     = port;
        this.name     = name;
        this.listener = listener;
    }

    // Connects to the server in a background thread
    public void start() {
        Thread clientThread = new Thread(() -> {
            try {
                // Connect to the server
                try (Socket socket = new Socket(host, port)) {

                    // Brief pause to allow connection to stabilize
                    // This is especially important when using tunnels like bore.pub
                    Thread.sleep(500);

                    // Set up output stream for sending messages
                    dos = new DataOutputStream(socket.getOutputStream());

                    // Set up input stream for receiving messages
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    // Notify UI that we're connected
                    listener.onMessage("-- Connected to " + host + ":" + port + " --");

                    // Main loop - keep reading messages from server
                    while (true) {
                        // Read message type (server sends "MSG" or "PING")
                        String type = dis.readUTF();

                        if (type.equals("MSG")) {
                            // Regular message - display it
                            listener.onMessage(dis.readUTF());
                        } else if (type.equals("PING")) {
                            // Keep-alive ping from server - just consume it silently
                            dis.readUTF();
                        }
                    }
                }

            } catch (IOException e) {
                // Connection lost - notify UI
                listener.onDisconnected();
            } catch (InterruptedException e) {
                // Thread was interrupted - restore interrupt status and exit
                Thread.currentThread().interrupt();
            }
        });

        clientThread.setDaemon(true); // Won't prevent app from closing
        clientThread.start();
    }

    // Sends a text message to the server
    // msg - the message to send (without the sender's name)
    public void send(String msg) {
        // Don't send if not connected yet
        if (dos == null) return;

        try {
            // Send message type
            dos.writeUTF("MSG");
            // Send the actual message with sender's name prepended
            dos.writeUTF("[" + name + "]: " + msg);
        } catch (IOException e) {
            listener.onMessage("Send error: " + e.getMessage());
        }
    }

    // Sends a file to the server
    // file - the File object to send
    public void sendFile(File file) {
        // Don't send if not connected yet
        if (dos == null) return;

        try {
            // Read the entire file into memory
            byte[] fileData;
            try (FileInputStream fis = new FileInputStream(file)) {
                fileData = fis.readAllBytes();
            }

            // Send file transfer message
            dos.writeUTF("FILE");              // Message type
            dos.writeUTF(name);                // Sender name
            dos.writeUTF(file.getName());      // Original file name
            dos.writeLong(fileData.length);    // File size
            dos.write(fileData);               // File content
            dos.flush();                       // Force send immediately

            // Notify user that file was sent
            listener.onMessage("File sent: " + file.getName());

        } catch (IOException e) {
            listener.onMessage("File send error: " + e.getMessage());
        }
    }
}