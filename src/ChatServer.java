import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ChatServer {

    static final int PORT = 4999;
    static final String LOG_FILE = "chatlog.txt";
    static final String FILES_DIR = "received_files/";
    static final List<DataOutputStream> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        new File(FILES_DIR).mkdirs();

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = ss.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                clients.add(dos);

                Thread t = new Thread(() -> handleClient(socket, dos));
                t.setDaemon(true); // thread dies when main exits
                t.start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static void handleClient(Socket socket, DataOutputStream dos) {
        try (socket) { // auto-closes socket when done
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            while (true) {
                String type = dis.readUTF();

                if (type.equals("MSG")) {
                    String msg = dis.readUTF();
                    if (msg.equals("bye")) break;
                    System.out.println(msg);
                    saveToFile(msg);
                    broadcast("MSG", msg, dos);

                } else if (type.equals("FILE")) {
                    String sender   = dis.readUTF();
                    String fileName = dis.readUTF();
                    long fileSize   = dis.readLong();

                    byte[] fileData = new byte[(int) fileSize];
                    dis.readFully(fileData);

                    String savePath = FILES_DIR + fileName;
                    try (FileOutputStream fos = new FileOutputStream(savePath)) {
                        fos.write(fileData);
                    }

                    String notice = "[" + sender + "] sent a file: " + fileName;
                    System.out.println(notice);
                    saveToFile(notice);
                    broadcast("MSG", notice, dos);
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            clients.remove(dos);
        }
    }

    static void broadcast(String type, String msg, DataOutputStream sender) {
        synchronized (clients) {
            Iterator<DataOutputStream> it = clients.iterator();
            while (it.hasNext()) {
                DataOutputStream client = it.next();
                if (client != sender) {
                    try {
                        client.writeUTF(type);
                        client.writeUTF(msg);
                    } catch (IOException e) {
                        it.remove(); // safe removal during iteration
                    }
                }
            }
        }
    }

    static void saveToFile(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            fw.write("[" + timestamp + "] " + msg + "\n");
        } catch (IOException e) {
            System.out.println("Log error: " + e.getMessage());
        }
    }
}