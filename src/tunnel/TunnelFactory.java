package tunnel;

public class TunnelFactory {

    public enum Provider { BORE, NGROK }

    public static TunnelProvider create(Provider provider) {
        return switch (provider) {
            case BORE  -> new BoreTunnel();
            case NGROK -> new NgrokTunnel();
        };
    }
}
