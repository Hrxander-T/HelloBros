// Server program : receive a message from client , send a reply
import java.io.*;
import java.net.*;

public class server1 {
    public static void main(String args[]) throws IOException {
        try (// Create a server socket on port 4999
                ServerSocket ss = new ServerSocket(4999); // Wait and accept a connection
                Socket s1 = ss.accept()) {

            // Create data input stream and read the client ’s message
            InputStream s1In = s1.getInputStream();
            DataInputStream dis = new DataInputStream(s1In);
            String st = dis.readUTF();
            System.out.println(st);

            // Create data output stream and send a reply
            OutputStream s1out = s1.getOutputStream();
            try (DataOutputStream dos = new DataOutputStream(s1out)) {
                dos.writeUTF(" Hello Client1 from server - First Message ");
                // Close all streams and socket
            }
           
        }
    }
}