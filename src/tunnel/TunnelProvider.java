package tunnel;
public interface TunnelProvider {
    void start(int port, TunnelListener listener);
    void stop();

    interface TunnelListener {
        void onReady(String address);
        void onError(String error);
    }
}