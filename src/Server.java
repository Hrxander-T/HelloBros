import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Server class - Handles hosting a chat room.
 *
 * Flow:
 * 1. Start on a given port and listen for incoming connections
 * 2. When a client connects, add them to the client list
 * 3. Handle messages from clients and broadcast to all other clients
 * 4. Save all messages to a log file
 */
public class Server {

    // Log file where all chat messages are saved
    static final String LOG_FILE   = "chatlog.txt";

    // Directory where received files are saved
    static final String FILES_DIR  = "received_files/";

    // Thread-safe list of all connected clients
    // We use synchronizedList because multiple threads access this list
    static final List<DataOutputStream> clients = Collections.synchronizedList(new ArrayList<>());

    // Port number where this server listens
    private final int port;

    // Callback interface for displaying messages in the UI
    private final MessageListener listener;

    public Server(int port, MessageListener listener) {
        this.port     = port;
        this.listener = listener;
    }

    // Starts the server in a background thread
    public void start() {
        // Create the directory for received files if it doesn't exist
        new File(FILES_DIR).mkdirs();

        // Run the server in a separate thread (so the GUI doesn't freeze)
        Thread serverThread = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                // Notify UI that server has started
                listener.onMessage("-- Server started on port " + port + " --");

                // Start a keep-alive thread that pings clients every 30 seconds
                // This helps detect disconnected clients
                @SuppressWarnings("BusyWait")
                Thread keepAlive = new Thread(() -> {
                    while (true) {
                        try {
                            Thread.sleep(30000); // Wait 30 seconds
                            broadcast("PING", "", null); // Send ping to all clients
                        } catch (InterruptedException e) {
                            break; // Thread interrupted, exit the loop
                        }
                    }
                });
                keepAlive.setDaemon(true); // Won't prevent app from closing
                keepAlive.start();

                // Main loop - keep accepting new client connections
                while (true) {
                    // BLOCKING CALL: waits here until a client connects
                    Socket socket = ss.accept();

                    // A new client connected!
                    listener.onMessage("-- New client connected --");

                    // Create output stream for this client
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    clients.add(dos);

                    // Handle this client in a separate thread
                    Thread t = new Thread(() -> handleClient(socket, dos));
                    t.setDaemon(true); // Won't prevent app from closing
                    t.start();
                }
            } catch (IOException e) {
                listener.onMessage("Server error: " + e.getMessage());
            }
        });

        serverThread.setDaemon(true); // Won't prevent app from closing
        serverThread.start();
    }

    // Handles communication with a single client
    // Runs in its own thread so multiple clients can connect simultaneously
    void handleClient(Socket socket, DataOutputStream dos) {
        try (socket) { // try-with-resources: socket is auto-closed when method exits
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            // Keep reading messages until client disconnects or says "bye"
            while (true) {
                // Read message type (first thing client sends)
                String type = dis.readUTF();

                if (type.equals("MSG")) {
                    // Regular text message
                    String msg = dis.readUTF();

                    // Check if client wants to disconnect
                    if (msg.equals("bye")) break;

                    // Display the message in our UI
                    listener.onMessage(msg);

                    // Save to log file with timestamp
                    saveToFile(msg);

                    // Broadcast to all OTHER clients (not the sender)
                    broadcast("MSG", msg, dos);

                } else if (type.equals("FILE")) {
                    // File transfer message
                    String sender   = dis.readUTF();      // Who sent the file
                    String fileName = dis.readUTF();       // Original file name
                    long fileSize   = dis.readLong();      // File size in bytes

                    // Read the file data
                    byte[] fileData = new byte[(int) fileSize];
                    dis.readFully(fileData); // Read exactly fileSize bytes

                    // Save the file to our received_files directory
                    try (FileOutputStream fos = new FileOutputStream(FILES_DIR + fileName)) {
                        fos.write(fileData);
                    }

                    // Notify about the file transfer
                    String notice = "[" + sender + "] sent a file: " + fileName;
                    listener.onMessage(notice);
                    saveToFile(notice);
                    broadcast("MSG", notice, dos);
                }
            }
        } catch (IOException e) {
            // Client disconnected (connection lost)
            listener.onMessage("-- A client disconnected --");
        } finally {
            // Always remove the client from our list
            clients.remove(dos);
        }
    }

    // Sends a message to all connected clients except the sender
    // type - message type ("MSG" or "PING")
    // msg - the message content
    // sender - the client who sent this message (won't receive it)
    static void broadcast(String type, String msg, DataOutputStream sender) {
        // Synchronized block protects against race conditions
        // (multiple threads trying to modify the clients list at once)
        synchronized (clients) {
            Iterator<DataOutputStream> it = clients.iterator();
            while (it.hasNext()) {
                DataOutputStream client = it.next();

                // Don't send back to the sender
                if (client != sender) {
                    try {
                        // Write message type and content
                        client.writeUTF(type);
                        client.writeUTF(msg);
                    } catch (IOException e) {
                        // Client is disconnected, remove them from the list
                        it.remove();
                    }
                }
            }
        }
    }

    // Saves a message to the log file with a timestamp
    // msg - the message to save
    static void saveToFile(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) { // true = append mode
            // Get current timestamp
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Write: [timestamp] message
            fw.write("[" + timestamp + "] " + msg + "\n");
        } catch (IOException e) {
            System.out.println("Log error: " + e.getMessage());
        }
    }
}