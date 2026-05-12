package tunnel;
import java.io.*;

public class NgrokTunnel implements TunnelProvider {

    private Process process;

    @Override
    public void start(int port, TunnelListener listener) {
        Thread t = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "ngrok", "tcp", String.valueOf(port));
                pb.redirectErrorStream(true);
                process = pb.start();

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[ngrok] " + line);
                    // ngrok outputs JSON, parse forwarding address
                    if (line.contains("tcp://")) {
                        String address = line.replaceAll(".*tcp://", "").trim();
                        listener.onReady(address);
                    }
                }

            } catch (IOException e) {
                listener.onError("Ngrok error: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void stop() {
        if (process != null) process.destroy();
    }
}