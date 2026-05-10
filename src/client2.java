import java.io.*;
import java.net.*;

public class client2 {
    public static void main(String[] args) throws IOException {
        try (Socket s = new Socket("localhost", 4999)) {
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            
            while (true) {
                System.out.print("You: ");
                String msg = keyboard.readLine();          // client types first
                dos.writeUTF(msg);
                if (msg.equals("bye")) break;
                
                String reply = dis.readUTF();              // wait for server
                System.out.println("Server: " + reply);
                if (reply.equals("bye")) break;
            }
        }
    }
}