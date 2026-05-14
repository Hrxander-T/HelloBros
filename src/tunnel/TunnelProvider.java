package tunnel;

public interface TunnelProvider {

    interface TunnelListener {
        void onReady(String address);

        void onError(String error);
    }

    void start(int port, TunnelListener listener);

    void stop();
}