import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4999;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected!");

            Thread reader = new Thread(() -> {
                try {
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    while (true) {
                        String msg = dis.readUTF();
                        System.out.println("Friend: " + msg);
                        if (msg.equals("bye")) { System.exit(0); }
                    }
                } catch (IOException e) { System.out.println("Disconnected."); }
            });

            Thread writer = new Thread(() -> {
                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
                        String msg = keyboard.readLine();
                        dos.writeUTF(msg);
                        if (msg.equals("bye")) { System.exit(0); }
                    }
                } catch (IOException e) {}
            });

            reader.start();
            writer.start();
            reader.join();
            writer.join();
        }
    }
}