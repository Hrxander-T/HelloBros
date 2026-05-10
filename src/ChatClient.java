import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 4999)) {
            System.out.println("Connected to server!");

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
        }
    }
}