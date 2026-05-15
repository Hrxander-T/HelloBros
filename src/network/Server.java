package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
 * Server - Hosts the chat room.
 *
 * Flow:
 * 1. Start on a given port, listen for connections
 * 2. Each client gets its own handler thread
 * 3. Messages are broadcast to all other clients
 * 4. All messages saved to chatlog.txt
 */
public class Server {

    static final String LOG_FILE = "chatlog.txt";
    static final String FILES_DIR = "received_files/";

    // Thread-safe list of all connected client output streams
    static final List<DataOutputStream> clients = Collections.synchronizedList(new ArrayList<>());

    private final int port;
    private final MessageListener listener;
    private ServerSocket ss; // stored so stop() can close it

    public Server(int port, MessageListener listener) {
        this.port = port;
        this.listener = listener;
    }

    // ── Start ──────────────────────────────
    public void start() {
        new File(FILES_DIR).mkdirs();

        Thread serverThread = new Thread(() -> {
            try {
                ss = new ServerSocket(port);
                listener.onMessage("-- Server started on port " + port + " --");

                startKeepAlive();

                while (true) {
                    Socket socket = ss.accept();
                    listener.onMessage("-- New client connected --");

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    clients.add(dos);

                    Thread t = new Thread(() -> handleClient(socket, dos));
                    t.setDaemon(true);
                    t.start();
                }
            } catch (IOException e) {
                // only log if it wasn't intentionally stopped
                if (ss != null && !ss.isClosed()) {
                    listener.onMessage("Server error: " + e.getMessage());
                }
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();
    }

    // ── Stop ───────────────────────────────
    public void stop() {
        try {
            clients.clear();
            if (ss != null && !ss.isClosed())
                ss.close();
        } catch (IOException e) {
            System.out.println("Server stop error: " + e.getMessage());
        }
    }

    // ── Keep-alive ─────────────────────────
    private void startKeepAlive() {
        @SuppressWarnings("BusyWait")
        Thread keepAlive = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                    broadcast(Protocol.PING, "", null);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        keepAlive.setDaemon(true);
        keepAlive.start();
    }

    // ── Client handler ─────────────────────
    void handleClient(Socket socket, DataOutputStream dos) {
        try (socket) {
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            OUTER: while (true) {
                String type = dis.readUTF();

                switch (type) {
                    case Protocol.MSG -> {
                        String msg = dis.readUTF();
                        if (msg.equals("bye"))
                            break OUTER;
                        listener.onMessage(msg);
                        saveToFile(msg);
                        broadcast(Protocol.MSG, msg, dos);
                    }
                    case Protocol.REACTION -> {
                        String msgId = dis.readUTF();
                        String emoji = dis.readUTF();
                        String sender2 = dis.readUTF();
                        String payload = msgId + ":" + emoji + ":" + sender2;
                        listener.onReaction(msgId, emoji, sender2);
                        broadcast(Protocol.REACTION, payload, dos);
                    }
                    case Protocol.FILE -> {
                        String sender = dis.readUTF();
                        String fileName = dis.readUTF();
                        long fileSize = dis.readLong();

                        broadcastFileHeader(sender, fileName, fileSize);

                        byte[] buffer = new byte[8192];
                        long remaining = fileSize;
                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

                        while (remaining > 0) {
                            int toRead = (int) Math.min(buffer.length, remaining);
                            int bytesRead = dis.read(buffer, 0, toRead);
                            if (bytesRead == -1)
                                break;
                            remaining -= bytesRead;
                            baos.write(buffer, 0, bytesRead);
                            broadcastFileChunk(buffer, bytesRead);
                        }

                        broadcastFileFlush();
                        listener.onFile(sender, fileName, baos.toByteArray());
                        saveToFile("[" + sender + "] sent a file: " + fileName);
                    }
                    case Protocol.GAME -> {
                        String moveData = dis.readUTF();
                        listener.onGameMove(moveData);
                        broadcast(Protocol.GAME, moveData, dos);
                    }
                    default -> {
                    }
                }
            }
        } catch (IOException e) {
            listener.onMessage("-- A client disconnected --");
        } finally {
            clients.remove(dos);
        }
    }

    // ── Broadcast ──────────────────────────
    // Sends to all clients except sender. Pass null to send to everyone.
    public static void broadcast(String type, String msg, DataOutputStream sender) {
        System.out.println("Broadcasting: " + type + " msg=" + msg + " clients=" + clients.size());

        synchronized (clients) {
            Iterator<DataOutputStream> it = clients.iterator();
            while (it.hasNext()) {
                DataOutputStream client = it.next();
                if (client != sender) {
                    try {
                        client.writeUTF(type);
                        client.writeUTF(msg);
                    } catch (IOException e) {
                        it.remove();
                    }
                }
            }
        }
    }

    public static void broadcastFile(String sender, String fileName, byte[] fileData, DataOutputStream exclude) {
        synchronized (clients) {
            for (DataOutputStream out : clients) {
                if (out != exclude) {
                    try {
                        out.writeUTF(Protocol.FILE);
                        out.writeUTF(sender);
                        out.writeUTF(fileName);
                        out.writeLong(fileData.length);
                        out.write(fileData);
                        out.flush();
                    } catch (IOException e) {
                        clients.remove(out);
                    }
                }
            }
        }
    }

    public static void broadcastFileHeader(String sender, String fileName, long fileSize) {
        synchronized (clients) {
            for (DataOutputStream out : clients) {
                try {
                    out.writeUTF(Protocol.FILE);
                    out.writeUTF(sender);
                    out.writeUTF(fileName);
                    out.writeLong(fileSize);
                } catch (IOException e) {
                    clients.remove(out);
                }
            }
        }
    }

    public static void broadcastFileChunk(byte[] buffer, int length) {
        synchronized (clients) {
            for (DataOutputStream out : clients) {
                try {
                    out.write(buffer, 0, length);
                } catch (IOException e) {
                    clients.remove(out);
                }
            }
        }
    }

    public static void broadcastFileFlush() {
        synchronized (clients) {
            for (DataOutputStream out : clients) {
                try {
                    out.flush();
                } catch (IOException e) {
                    clients.remove(out);
                }
            }
        }
    }

    // ── Save to file ───────────────────────
    public static void saveToFile(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            fw.write("[" + timestamp + "] " + msg + "\n");
        } catch (IOException e) {
            System.out.println("Log error: " + e.getMessage());
        }
    }
}