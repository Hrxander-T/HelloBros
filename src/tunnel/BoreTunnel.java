package tunnel;

import java.io.*;
import java.net.URISyntaxException;

public class BoreTunnel implements TunnelProvider {

    private Process process;
    private boolean stopped = false;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 3000;

    @Override
    public void start(int port, TunnelListener listener) {
        @SuppressWarnings("BusyWait")
        Thread t = new Thread(() -> {
            int attempts = 0;

            while (!stopped && attempts < MAX_RETRIES) {
                attempts++;
                if (attempts > 1) {
                    listener.onError("Tunnel failed, retrying (" + attempts + "/" + MAX_RETRIES + ")...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                try {
                    String borePath = findBore();
                    ProcessBuilder pb = new ProcessBuilder(
                            borePath, "local", String.valueOf(port), "--to", "bore.pub");
                    pb.redirectErrorStream(true);
                    process = pb.start();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    boolean connected = false;
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[bore] " + line);
                        if (line.contains("listening at")) {
                            String address = line.substring(line.lastIndexOf(":") +1).trim();
                            listener.onReady(address);
                            connected = true;
                            attempts = 0; // reset retries on success
                        }
                        if (line.contains("timed out") || line.contains("error")) {
                            break; // exit read loop, trigger retry
                        }
                    }

                    process.waitFor();

                    if (!stopped && connected) {
                        // was connected but dropped — notify and retry
                        listener.onError("Tunnel dropped, reconnecting...");
                        attempts = 0; // keep retrying indefinitely after a successful connection
                    }

                } catch (IOException | InterruptedException e) {
                    if (!stopped)
                        listener.onError("Bore error: " + e.getMessage());
                }
            }

            if (!stopped) {
                listener.onError("Could not connect to bore.pub after " + MAX_RETRIES
                        + " attempts. Check your internet connection.");
            }
        });

        t.setDaemon(true);
        t.start();
    }

    @Override
    public void stop() {
        stopped = true;
        if (process != null)
            process.destroy();
    }

    private String findBore() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String boreName = isWindows ? "bore.exe" : "bore";

        String appDir;
        try {
            appDir = new File(BoreTunnel.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI())
                    .getParentFile().getParent();
        } catch (URISyntaxException e) {
            appDir = ".";
        }

        String[] locations = {
                appDir + File.separator + boreName,
                "/opt/hellobros/" + boreName,
                "./" + boreName
        };

        for (String loc : locations) {
            if (new File(loc).exists())
                return loc;
        }

        return "./" + boreName; // fallback
    }
}