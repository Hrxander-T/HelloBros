import java.io.*;
import java.net.*;

public class server2 {
    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(4999)) {
            System.out.println("Waiting...");
            try (Socket s = ss.accept()) {
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
                
                while (true) {
                    String received = dis.readUTF();          // wait for client
                    System.out.println("Client: " + received);
                    if (received.equals("bye")) break;
                    
                    System.out.print("You: ");
                    String reply = keyboard.readLine();        // server types reply
                    dos.writeUTF(reply);
                    if (reply.equals("bye")) break;
                }
            }
        }
    }
}