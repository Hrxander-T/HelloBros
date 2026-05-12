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
        this.host     = host;
        this.port     = port;
        this.name     = name;
        this.listener = listener;
    }

    public void start() {
        Thread clientThread = new Thread(() -> {
            try {
                try (Socket socket = new Socket(host, port)) {
                    Thread.sleep(500);
                    dos = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(socket.getInputStream());

                    listener.onMessage("-- Connected to " + host + ":" + port + " --");

                    while (true) {
                        String type = dis.readUTF();
                        if (type.equals("MSG")) {
                            listener.onMessage(dis.readUTF());
                        } else if (type.equals("PING")) {
                            dis.readUTF();
                        }
                    }
                }

            } catch (IOException e) {
                listener.onDisconnected();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        clientThread.setDaemon(true);
        clientThread.start();
    }

    public void send(String msg) {
        if (dos == null) return;
        try {
            dos.writeUTF("MSG");
            dos.writeUTF("[" + name + "]: " + msg);
        } catch (IOException e) {
            listener.onMessage("Send error: " + e.getMessage());
        }
    }

    public void sendFile(File file) {
        if (dos == null) return;
        try {
            byte[] fileData;
            try (FileInputStream fis = new FileInputStream(file)) {
                fileData = fis.readAllBytes();
            }
            dos.writeUTF("FILE");
            dos.writeUTF(name);
            dos.writeUTF(file.getName());
            dos.writeLong(fileData.length);
            dos.write(fileData);
            dos.flush();
            listener.onMessage("File sent: " + file.getName());
        } catch (IOException e) {
            listener.onMessage("File send error: " + e.getMessage());
        }
    }
}