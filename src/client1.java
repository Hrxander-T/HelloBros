// Client program : send a message to server , receive a reply
import java.io.*;
import java.net.*;

public class client1 {
    public static void main(String args[]) throws IOException {
        // Create data output stream and send a message
        try ( // Create a socket to connect to the server
                Socket s2 = new Socket("localhost", 4999); // Create data output stream and send a message
                OutputStream s1out = s2.getOutputStream()) {
            try (DataOutputStream dos = new DataOutputStream(s1out)) {
                dos.writeUTF(" Hello Server1 , this is from client - Message#1");
                // Create data input stream and read the server ’s reply
                InputStream s1In = s2.getInputStream();
                DataInputStream dis = new DataInputStream(s1In);
                String st = dis.readUTF();
                System.out.println(st);
                // Close all streams and socket
            }
        }
    }
}