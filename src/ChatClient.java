import java.io.*;
import java.net.*;

public class ChatClient {

    static final int DEFAULT_PORT = 4999;
    static volatile boolean running = true; // shutdown flag

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port    = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        try (
            Socket socket = new Socket(host, port);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.print("Enter your name: ");
            String name = keyboard.readLine();
            System.out.println("Connected! Type a message or /file path/to/file. Type bye to quit.");

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream  dis = new DataInputStream(socket.getInputStream());

            Thread reader = new Thread(() -> {
                try {
                    while (running) {
                        String type = dis.readUTF();
                        if (type.equals("MSG")) {
                            System.out.println(dis.readUTF());
                        }
                    }
                } catch (IOException e) {
                    if (running) System.out.println("Disconnected from server.");
                }
            });

            Thread writer = new Thread(() -> {
                try {
                    while (running) {
                        String input = keyboard.readLine();
                        if (input == null) break;

                        if (input.startsWith("/file ")) {
                            sendFile(input.substring(6).trim(), name, dos);

                        } else {
                            dos.writeUTF("MSG");
                            dos.writeUTF("[" + name + "]: " + input);

                            if (input.equals("bye")) {
                                running = false;
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    if (running) System.out.println("Send error: " + e.getMessage());
                }
            });

            reader.setDaemon(true);
            reader.start();
            writer.start();
            writer.join(); // wait for writer (user quits)

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Goodbye.");
    }

    static void sendFile(String filePath, String name, DataOutputStream dos) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }

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

            System.out.println("File sent: " + file.getName());

        } catch (IOException e) {
            System.out.println("File send error: " + e.getMessage());
        }
    }
}