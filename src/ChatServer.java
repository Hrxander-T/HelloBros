import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    // shared list of all connected clients
    static final List<DataOutputStream> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(4999)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) { // keep accepting new clients forever
                Socket socket = ss.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                clients.add(dos); // add to shared list

                // give each client their own thread
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
                broadcast(msg, dos); // send to everyone except sender
            }
        } catch (IOException e) {
            System.out.println("A client disconnected.");
        } finally {
            clients.remove(dos); // remove from list when disconnected
        }
    }

    static void broadcast(String msg, DataOutputStream sender) {
        synchronized (clients) {
            for (DataOutputStream client : clients) {
                if (client != sender) { // don't send back to sender
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