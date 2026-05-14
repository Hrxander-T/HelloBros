package network;

import javax.swing.SwingUtilities;
import tunnel.TunnelFactory;
import tunnel.TunnelProvider;
import ui.ChatScreen;
import ui.GameScreen;
import ui.LobbyArgs;
import ui.LobbyScreen;

public class NetworkManager {

    private static Server server;
    private static Client client;
    private static TunnelProvider tunnel;
    private static String tunnelAddress = null;

    private static boolean started = false;
    private static boolean isHost = false;

    private static ChatScreen chat;
    private static GameScreen game;
    private static LobbyScreen lobby;

    public static void init(ChatScreen c, GameScreen g, LobbyScreen l) {
        chat = c;
        game = g;
        lobby = l;
    }

    public static void connect(LobbyArgs a) {
        if (started)
            return;
        started = true;
        isHost = a.isHost;

        MessageListener listener = buildListener();

        if (isHost) {
            server = new Server(a.port, listener);
            server.start();
            startTunnel(a.port);
        } else {
            client = new Client(a.address, a.port, a.name, listener);
            client.start();
        }
    }

    public static void sendMessage(String name, String msg) {
        String formatted = "[" + name + "]: " + msg;
        if (isHost) {
            Server.broadcast(Protocol.MSG, formatted, null);
            Server.saveToFile(formatted);
        } else if (client != null) {
            client.send(formatted);
        }
    }

    public static void sendMove(int row, int col) {
        String moveData = row + "," + col;
        if (isHost) {
            Server.broadcast(Protocol.GAME, moveData, null);
        } else if (client != null) {
            client.sendMove(row, col);
        }
    }

    public static void sendFile(java.io.File file) {
        if (client != null)
            client.sendFile(file);
    }

    public static void sendGameRequest() {
        if (isHost) {
            Server.broadcast(Protocol.GAME, Protocol.GAME_REQUEST, null);
        } else if (client != null) {
            client.sendGameSignal(Protocol.GAME_REQUEST);
        }
    }

    public static void sendGameAccept() {
        boolean hostGoesFirst = Math.random() < 0.5;
        if (isHost) {
            Server.broadcast(Protocol.GAME, Protocol.GAME_ACCEPT + ":" + hostGoesFirst, null);
            SwingUtilities.invokeLater(() -> game.receiveGameAccept(hostGoesFirst));
        } else if (client != null) {
            client.sendGameSignal(Protocol.GAME_ACCEPT + ":" + hostGoesFirst);
            SwingUtilities.invokeLater(() -> game.receiveGameAccept(hostGoesFirst));
        }
    }

    public static void sendGameDecline() {
        if (isHost) {
            Server.broadcast(Protocol.GAME, Protocol.GAME_DECLINE, null);
        } else if (client != null) {
            client.sendGameSignal(Protocol.GAME_DECLINE);
        }
    }

    public static void reset() {
        if (tunnel != null) {
            tunnel.stop();
            tunnel = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }
        client = null;
        started = false;
        isHost = false;
        tunnelAddress = null;
    }

    public static boolean isHost() {
        return isHost;
    }

    public static boolean isStarted() {
        return started;
    }

    public static String getTunnelAddress() {
        return tunnelAddress;
    }

    private static MessageListener buildListener() {
        return new MessageListener() {
            @Override
            public void onMessage(String msg) {
                if (chat != null)
                    chat.appendMessage(msg);
                if (msg.contains("New client connected")) {
                    if (game != null)
                        game.setConnectionStatus("Opponent connected");
                }
                if (msg.contains("client disconnected")) {
                    if (game != null)
                        game.setConnectionStatus("Opponent disconnected");
                }
            }

            @Override
            public void onDisconnected() {
                if (chat != null) {
                    chat.appendMessage("-- Disconnected --");
                    chat.setConnected(false);
                }
            }

            @Override
            public void onGameMove(String moveData) {
                if (game == null)
                    return;

                SwingUtilities.invokeLater(() -> {
                    if (moveData.startsWith(Protocol.GAME_ACCEPT + ":")) {
                        boolean hostGoesFirst = Boolean.parseBoolean(moveData.split(":")[1]);
                        game.receiveGameAccept(isHost ? hostGoesFirst : !hostGoesFirst);
                    } else {
                        switch (moveData) {
                            case Protocol.GAME_REQUEST -> game.receiveGameRequest();
                            case Protocol.GAME_DECLINE -> game.receiveGameDecline();
                            case Protocol.GAME_RESET -> game.receiveReset();
                            default -> {
                                String[] parts = moveData.split(",");
                                game.receiveMove(Integer.parseInt(parts[0]),
                                        Integer.parseInt(parts[1]));
                            }
                        }
                    }
                });
            }
        };
    }

    private static void startTunnel(int port) {
        tunnel = TunnelFactory.create(TunnelFactory.Provider.BORE);
        tunnel.start(port, new TunnelProvider.TunnelListener() {
            @Override
            public void onReady(String address) {
                tunnelAddress = address;
                if (lobby != null)
                    lobby.showTunnelInfo(address);
                if (chat != null) {
                    chat.showTunnelInfo(address);
                    chat.appendMessage("-- Tunnel ready: " + address + " --");
                }
            }

            @Override
            public void onError(String error) {
                if (chat != null)
                    chat.appendMessage("-- Tunnel error: " + error + " --");
                if (lobby != null)
                    lobby.showTunnelInfo("Tunnel failed");
            }
        });
    }
}