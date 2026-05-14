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

    private static String lastRoomID; // room ID used to join
    private static String lastName; // name used to connect

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
        lastName = a.name;
        lastRoomID = a.address;


        MessageListener listener = buildListener();

        if (isHost) {
            server = new Server(a.port, listener);
            server.start();
            if (lobby != null)
                lobby.setStatus("Starting tunnel...");
            startTunnel(a.port);
        } else {
            Thread t = new Thread(() -> {
                try {
                    int port = signaling.joinRoom(a.address); // a.address = roomID
                    client = new Client("bore.pub", port, a.name, listener);
                    client.start();
                } catch (Exception e) {
                    // notify UI
                    SwingUtilities.invokeLater(() -> {
                        if (chat != null)
                            chat.appendMessage("-- Room not found: " + a.address + " --");
                    });
                }
            });
            t.setDaemon(true);
            t.start();
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

    public static void cleanup() {
        signaling.deleteRoom();
        if (tunnel != null)
            tunnel.stop();
        if (server != null)
            server.stop();
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

    public static String getRoomID() {
        return signaling.getRoomID();
    }

    private static void startReconnectPoller() {
        @SuppressWarnings("BusyWait")
        Thread poller = new Thread(() -> {
            String roomID = signaling.getRoomID() != null
                    ? signaling.getRoomID()
                    : lastRoomID; // store roomID used to join

            int attempts = 0;
            while (attempts < 10) {
                try {
                    Thread.sleep(3000); // wait 3 seconds between attempts
                    int newPort = signaling.joinRoom(roomID);
                    System.out.println("Reconnecting to port: " + newPort);

                    client = new Client("bore.pub", newPort, lastName, buildListener());
                    client.start();

                    SwingUtilities.invokeLater(() -> {
                        if (chat != null) {
                            chat.appendMessage("-- Reconnected --");
                            chat.setConnected(true);
                        }
                    });
                    return; // success

                } catch (Exception e) {
                    attempts++;
                    System.out.println("Reconnect attempt " + attempts + " failed");
                }
            }
            SwingUtilities.invokeLater(() -> {
                if (chat != null)
                    chat.appendMessage("-- Could not reconnect --");
            });
        });
        poller.setDaemon(true);
        poller.start();
    }

    private final static SignalingClient signaling = new SignalingClient();

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
                if (!isHost) {
                    startReconnectPoller();
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

                int borePort = Integer.parseInt(address.trim());

                // register or update room on signaling server
                Thread t = new Thread(() -> {
                    try {
                        if (signaling.getRoomID() == null) {
                            String roomID = signaling.createRoom(borePort);
                            if (lobby != null)
                                lobby.showRoomID(roomID);
                            if (chat != null)
                                chat.showTunnelInfo(roomID);
                        } else {
                            signaling.updatePort(borePort);
                            if (lobby != null)
                                lobby.showRoomID(signaling.getRoomID());
                        }
                    } catch (Exception e) {
                        if (chat != null)
                            chat.appendMessage("-- Signaling error: " + e.getMessage() + " --");
                    }
                });
                t.setDaemon(true);
                t.start();
            }

            @Override
            public void onError(String error) {
                if (chat != null)
                    chat.appendMessage("-- Tunnel error: " + error + " --");

            }
        });
    }
}