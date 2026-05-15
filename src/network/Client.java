package network;

import java.io.*;
import java.net.*;

public class Client {

    private final String host;
    private final int port;
    private final String name;
    private final MessageListener listener;
    private DataOutputStream dos;

    public Client(String host, int port, String name, MessageListener listener) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.listener = listener;
    }

    // ==================== Connect ====================

    public void start() {
        Thread t = new Thread(() -> {
            try (Socket socket = new Socket(host, port)) {
                Thread.sleep(500);
                dos = new DataOutputStream(socket.getOutputStream());
                DataInputStream dis = new DataInputStream(socket.getInputStream());

                listener.onMessage("-- Connected to " + host + ":" + port + " --");

                while (true) {
                    String type = dis.readUTF();
                    switch (type) {
                        case Protocol.PING     -> dis.readUTF();
                        case Protocol.MSG      -> listener.onMessage(dis.readUTF());
                        case Protocol.REACTION -> handleReaction(dis);
                        case Protocol.GAME     -> listener.onGameMove(dis.readUTF());
                        case Protocol.FILE     -> handleFile(dis);
                        default                -> {}
                    }
                }
            } catch (IOException e) {
                listener.onDisconnected();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ==================== Send ====================

    public void send(String msg) {
        try {
            dos.writeUTF(Protocol.MSG);
            dos.writeUTF(msg);
        } catch (IOException e) {
            listener.onMessage("Send error: " + e.getMessage());
        }
    }

    public void sendReaction(String messageId, String emoji) {
        try {
            dos.writeUTF(Protocol.REACTION);
            dos.writeUTF(messageId + ":" + emoji + ":" + name);
        } catch (IOException e) {
            listener.onMessage("Reaction error: " + e.getMessage());
        }
    }

    public void sendFile(File file) {
        new Thread(() -> {
            if (dos == null) return;
            try (FileInputStream fis = new FileInputStream(file)) {
                long total = file.length();
                long sent = 0;

                dos.writeUTF(Protocol.FILE);
                dos.writeUTF(name);
                dos.writeUTF(file.getName());
                dos.writeLong(total);

                byte[] buffer = new byte[8192];
                int bytesRead;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                    baos.write(buffer, 0, bytesRead);
                    sent += bytesRead;
                    listener.onFileSendProgress((int) (sent * 100 / total), null);
                }
                dos.flush();
                listener.onFileSendProgress(100, baos.toByteArray());

            } catch (IOException e) {
                listener.onMessage("File send error: " + e.getMessage());
            }
        }).start();
    }

    public void sendMove(int row, int col) {
        if (dos == null) return;
        try {
            dos.writeUTF(Protocol.GAME);
            dos.writeUTF(row + "," + col);
        } catch (IOException e) {
            listener.onMessage("Move send error: " + e.getMessage());
        }
    }

    public void sendGameSignal(String signal) {
        if (dos == null) return;
        try {
            dos.writeUTF(Protocol.GAME);
            dos.writeUTF(signal);
        } catch (IOException e) {
            listener.onMessage("Signal error: " + e.getMessage());
        }
    }

    // ==================== Private Handlers ====================

    private void handleReaction(DataInputStream dis) throws IOException {
        String payload = dis.readUTF(); // "msgId:emoji:sender"
        String[] parts = payload.split(":", 3);
        listener.onReaction(parts[0], parts[1], parts[2]);
    }

    private void handleFile(DataInputStream dis) throws IOException {
        String sender   = dis.readUTF();
        String fileName = dis.readUTF();
        long fileSize   = dis.readLong();

        byte[] buffer = new byte[8192];
        long remaining = fileSize;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (remaining > 0) {
            int toRead   = (int) Math.min(buffer.length, remaining);
            int bytesRead = dis.read(buffer, 0, toRead);
            if (bytesRead == -1) break;
            remaining -= bytesRead;
            baos.write(buffer, 0, bytesRead);
        }

        listener.onFile(sender, fileName, baos.toByteArray());
    }
}