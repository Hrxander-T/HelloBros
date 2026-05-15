package network;

import java.io.IOException;
import javax.swing.SwingUtilities;
import model.LobbyArgs;
import tunnel.TunnelFactory;
import tunnel.TunnelProvider;
import ui.ChatScreen;
import ui.GameScreen;
import ui.LobbyScreen;

public class NetworkManager {

    // ==================== Fields ====================

    private static Server server;
    private static Client client;
    private static TunnelProvider tunnel;
    private static MessageListener listener;

    private static final SignalingClient signaling = new SignalingClient();

    private static String lastRoomID;
    private static String lastName;
    private static boolean started = false;
    private static boolean isHost = false;

    private static ChatScreen chat;
    private static GameScreen game;
    private static LobbyScreen lobby;

    // ==================== Init ====================

    public static void init(ChatScreen c, GameScreen g, LobbyScreen l) {
        chat = c;
        game = g;
        lobby = l;
    }

    // ==================== Connect ====================

    public static void connect(LobbyArgs a) {
        if (started)
            return;
        started = true;
        isHost = a.isHost;
        lastName = a.name;
        lastRoomID = a.address;

        listener = buildListener();

        if (isHost) {
            server = new Server(a.port, listener);
            server.start();
            if (lobby != null)
                lobby.setStatus("Starting tunnel...");
            startTunnel(a.port);
        } else {
            new Thread(() -> {
                try {
                    int port = signaling.joinRoom(a.address);
                    client = new Client("bore.pub", port, a.name, listener);
                    client.start();
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        if (chat != null)
                            chat.appendMessage("-- Room not found: " + a.address + " --");
                    });
                }
            }).start();
        }
    }

    // ==================== Send ====================

    public static void sendMessage(String name, String id, String msg) {
        String formatted = id + "|[" + name + "]: " + msg;
        if (isHost) {
            Server.broadcast(Protocol.MSG, formatted, null);
            Server.saveToFile(formatted);
        } else if (client != null) {
            client.send(formatted);
        }
    }

    public static void sendReaction(String messageId, String emoji) {
        String payload = messageId + ":" + emoji + ":" + lastName;
        if (isHost) {
            Server.broadcast(Protocol.REACTION, payload, null);
        } else if (client != null) {
            client.sendReaction(messageId, emoji);
        }
        if (chat != null)
            chat.appendReaction(messageId, emoji, lastName);
    }

    public static void sendFile(java.io.File file) {
        new Thread(() -> {
            if (isHost) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                    long total = file.length();
                    long sent = 0;

                    Server.broadcastFileHeader(lastName, file.getName(), total);

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        Server.broadcastFileChunk(buffer, bytesRead);
                        baos.write(buffer, 0, bytesRead);
                        sent += bytesRead;
                        listener.onFileSendProgress((int) (sent * 100 / total), null);
                    }

                    Server.broadcastFileFlush();
                    listener.onFileSendProgress(100, baos.toByteArray());

                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        if (chat != null)
                            chat.appendMessage("File send error: " + e.getMessage());
                    });
                }
            } else if (client != null) {
                client.sendFile(file);
            }
        }).start();
    }

    public static void sendMove(int row, int col) {
        String moveData = row + "," + col;
        if (isHost) {
            Server.broadcast(Protocol.GAME, moveData, null);
        } else if (client != null) {
            client.sendMove(row, col);
        }
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
        String payload = Protocol.GAME_ACCEPT + ":" + hostGoesFirst;
        if (isHost) {
            Server.broadcast(Protocol.GAME, payload, null);
            SwingUtilities.invokeLater(() -> game.receiveGameAccept(hostGoesFirst));
        } else if (client != null) {
            client.sendGameSignal(payload);
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

    // ==================== State ====================

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

    private static String tunnelAddress = null;

    // ==================== Listener ====================

    private static MessageListener buildListener() {
        return new MessageListener() {

            @Override
            public void onMessage(String msg) {
                SwingUtilities.invokeLater(() -> {
                    if (chat != null)
                        chat.appendMessage(msg);
                    if (game != null) {
                        if (msg.contains("New client connected"))
                            game.setConnectionStatus("Opponent connected");
                        if (msg.contains("client disconnected"))
                            game.setConnectionStatus("Opponent disconnected");
                    }
                });
            }

            @Override
            public void onReaction(String msgID, String emoji, String sender) {
                SwingUtilities.invokeLater(() -> {
                    if (chat != null)
                        chat.appendReaction(msgID, emoji, sender);
                });
            }

            @Override
            public void onFile(String sender, String fileName, byte[] data) {
                SwingUtilities.invokeLater(() -> {
                    if (chat != null)
                        chat.appendFile(sender, fileName, data);
                });
            }

            @Override
            public void onFileSendProgress(int percent, byte[] completedData) {
                SwingUtilities.invokeLater(() -> {
                    if (chat != null)
                        chat.updateSendProgress(percent, completedData);
                });
            }

            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    if (chat != null) {
                        chat.appendMessage("-- Disconnected --");
                        chat.setConnected(false);
                    }
                });
                if (!isHost)
                    startReconnectPoller();
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
                                game.receiveMove(
                                        Integer.parseInt(parts[0]),
                                        Integer.parseInt(parts[1]));
                            }
                        }
                    }
                });
            }
        };
    }

    // ==================== Private Helpers ====================

    private static void startTunnel(int port) {
        tunnel = TunnelFactory.create(TunnelFactory.Provider.BORE);
        tunnel.start(port, new TunnelProvider.TunnelListener() {
            @Override
            public void onReady(String address) {
                int borePort = Integer.parseInt(address.trim());
                new Thread(() -> {
                    try {
                        if (signaling.getRoomID() == null) {
                            String roomID = signaling.createRoom(borePort);
                            SwingUtilities.invokeLater(() -> {
                                if (lobby != null)
                                    lobby.showRoomID(roomID);
                                if (chat != null)
                                    chat.showTunnelInfo(roomID);
                            });
                        } else {
                            signaling.updatePort(borePort);
                            SwingUtilities.invokeLater(() -> {
                                if (lobby != null)
                                    lobby.showRoomID(signaling.getRoomID());
                            });
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            if (chat != null)
                                chat.appendMessage("-- Signaling error: " + e.getMessage() + " --");
                        });
                    }
                }).start();
            }

            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    if (chat != null)
                        chat.appendMessage("-- Tunnel error: " + error + " --");
                });
            }
        });
    }

    @SuppressWarnings("BusyWait")
    private static void startReconnectPoller() {
        new Thread(() -> {
            String roomID = signaling.getRoomID() != null ? signaling.getRoomID() : lastRoomID;
            int attempts = 0;

            while (attempts < 10) {
                try {
                    Thread.sleep(3000);
                    int newPort = signaling.joinRoom(roomID);
                    client = new Client("bore.pub", newPort, lastName, buildListener());
                    client.start();
                    SwingUtilities.invokeLater(() -> {
                        if (chat != null) {
                            chat.appendMessage("-- Reconnected --");
                            chat.setConnected(true);
                        }
                    });
                    return;
                } catch (Exception e) {
                    attempts++;
                    System.out.println("Reconnect attempt " + attempts + " failed");
                }
            }

            SwingUtilities.invokeLater(() -> {
                if (chat != null) chat.appendMessage("-- Could not reconnect --");
            });
        }).start();
    }
}