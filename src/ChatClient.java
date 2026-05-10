import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4999;

        try (Socket socket = new Socket(host, port)) {
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your name: ");
            String name = keyboard.readLine();
            System.out.println("Connected! Type to chat.");

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            // reader thread
            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        String msg = dis.readUTF();
                        System.out.println(msg);
                    }
                } catch (IOException e) { System.out.println("Disconnected."); }
            });

            // writer thread
            Thread writer = new Thread(() -> {
                try {
                    while (true) {
                        String msg = keyboard.readLine();
                        dos.writeUTF("[" + name + "]: " + msg);
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