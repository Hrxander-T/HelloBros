import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ChatServer {
    static final List<DataOutputStream> clients = Collections.synchronizedList(new ArrayList<>());
    static final String LOG_FILE = "chatlog.txt";

    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(4999)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket socket = ss.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                clients.add(dos);

                Thread t = new Thread(() -> handleClient(socket, dos));
                t.start();
            }
        }
    }

    static void handleClient(Socket socket, DataOutputStream dos) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            while (true) {
                String msg = dis.readUTF();
                System.out.println("Received: " + msg);
                if (msg.equals("bye")) break;
                saveToFile(msg);      // save to file
                broadcast(msg, dos);
            }
        } catch (IOException e) {
            System.out.println("A client disconnected.");
        } finally {
            clients.remove(dos);
        }
    }

    static void saveToFile(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) { // true = append
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            fw.write("[" + timestamp + "] " + msg + "\n");
        } catch (IOException e) {
            System.out.println("Could not save to file: " + e.getMessage());
        }
    }

    static void broadcast(String msg, DataOutputStream sender) {
        synchronized (clients) {
            for (DataOutputStream client : clients) {
                if (client != sender) {
                    try {
                        client.writeUTF(msg);
                    } catch (IOException e) {
                        clients.remove(client);
                    }
                }
            }
        }
    }
}