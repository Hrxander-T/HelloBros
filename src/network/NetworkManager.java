package network;

import ui.ChatScreen;
import ui.GameScreen;
import ui.LobbyArgs;
import javax.swing.SwingUtilities;

public class NetworkManager {

    private static Server server;
    private static Client client;
    private static boolean started = false;
    private static boolean isHost;

    private static ChatScreen chat;
    private static GameScreen game;

    public static void init(ChatScreen c, GameScreen g) {
        chat = c;
        game = g;
    }

    public static void connect(LobbyArgs a) {
        if (started) return;
        started = true;
        isHost  = a.isHost;

        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(String msg) {
                chat.appendMessage(msg);
            }

            @Override
            public void onDisconnected() {
                chat.appendMessage("-- Disconnected --");
                chat.setConnected(false);
            }

            @Override
            public void onGameMove(String moveData) {
                String[] parts = moveData.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                SwingUtilities.invokeLater(() -> game.receiveMove(row, col));
            }
        };

        if (isHost) {
            server = new Server(a.port, listener);
            server.start();
        } else {
            client = new Client(a.address, a.port, a.name, listener);
            client.start();
        }
    }

    public static void sendMessage(String name, String msg) {
        if (isHost) {
            Server.broadcast("MSG", "[" + name + "]: " + msg, null);
            Server.saveToFile("[" + name + "]: " + msg);
        } else {
            if (client != null) client.send(msg);
        }
    }

    public static void sendMove(int row, int col) {
        if (isHost) {
            Server.broadcast("GAME", row + "," + col, null);
        } else {
            if (client != null) client.sendMove(row, col);
        }
    }

    public static void sendFile(java.io.File file) {
        if (client != null) client.sendFile(file);
    }

    public static void reset() {
        started = false;
        server  = null;
        client  = null;
    }

    public static boolean isHost() { return isHost; }
}