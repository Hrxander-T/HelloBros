package model;

public class ChatArgs {
    public final String name;
    public final int port;
    public final String address;
    public final boolean isHost;

    public ChatArgs(String name, int port, String address, boolean isHost) {
        this.name    = name;
        this.port    = port;
        this.address = address;
        this.isHost  = isHost;
    }
}