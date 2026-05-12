package main;
import javax.swing.JFrame;
import network.Client;
import network.Server;
import tunnel.TunnelFactory;
import tunnel.TunnelProvider;
import ui.ChatScreen;
import ui.StartupScreen;

/**
 * Entry point of the chat application.
 * Handles screen transitions between Startup and Chat screens.
 */
public class ChatApp {

    // Main window - shared across all screens so we can switch between them
    static JFrame frame;

    public static void main(String[] args) {
        // Create the main application window
        frame = new JFrame("Hello Bros");
        frame.setSize(400, 500);

        // Exit the application when the window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the window on the screen
        frame.setLocationRelativeTo(null);

        // Make the window visible
        frame.setVisible(true);

        // Start by showing the startup screen (where user chooses Host or Join)
        showStartup();
    }

    // Sets up the startup screen and handles the user's choice
    static void showStartup() {
        // Create StartupScreen with anonymous inner class callbacks
        // This lets us override the default empty methods in StartupScreen
        StartupScreen startup = new StartupScreen(frame) {

            // Called when user clicks the "Host" button
            // Creates a new chat room that others can join
            @Override
            protected void onHost(String name, int port) {
                // Create ChatScreen first so we can pass it to Server
                ChatScreen chat = new ChatScreen(frame, name) {

                    // Override onSend to broadcast messages via the Server
                    @Override
                    protected void onSend(String msg) {
                        // Broadcast to all connected clients
                        Server.broadcast("MSG", "[" + name + "]: " + msg, null);
                        // Also save to local log file
                        Server.saveToFile("[" + name + "]: " + msg);
                    }
                };
                chat.show();

                // Create and start the server on the chosen port
                Server server = new Server(port, chat);
                server.start();

                TunnelProvider tunnel = TunnelFactory.create(TunnelFactory.Provider.BORE);
                tunnel.start(port, new TunnelProvider.TunnelListener() {
                    @Override
                    public void onReady(String address) {
                        chat.showTunnelInfo(address);
                        chat.appendMessage("-- Tunnel ready: " + address + " --");
                    }

                    @Override
                    public void onError(String error) {
                        chat.appendMessage("-- Tunnel error: " + error + " --");
                    }
                });
            }

            // Called when user clicks the "Join" button
            // Connects to an existing chat room
            @Override
            protected void onJoin(String name, String address, int port) {
                Client[] clientRef = new Client[1];

                ChatScreen chat = new ChatScreen(frame, name) {
                    @Override
                    protected void onSend(String msg) {
                        if (clientRef[0] != null)
                            clientRef[0].send(msg);
                    }

                    @Override
                    protected void onReconnect() {
                        Client client = new Client(address, port, name, this);
                        clientRef[0] = client;
                        client.start();
                    }
                };
                chat.show();

                Client client = new Client(address, port, name, chat);
                clientRef[0] = client;
                client.start();
            }
        };
        startup.show();
    }
}