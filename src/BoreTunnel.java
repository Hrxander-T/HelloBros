import java.io.*;

public class BoreTunnel implements TunnelProvider {

    private Process process;

    @Override
    public void start(int port, TunnelListener listener) {
        Thread t = new Thread(() -> {
            try {
                String borePath = new File("./bore").exists() ? "./bore" : "bore";
                ProcessBuilder pb = new ProcessBuilder(
                    borePath, "local", String.valueOf(port), "--to", "bore.pub");
                pb.redirectErrorStream(true);
                process = pb.start();

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[bore] " + line);
                    if (line.contains("listening at")) {
                        String address = line.substring(line.indexOf("bore.pub")).trim();
                        listener.onReady(address);
                    }
                }

            } catch (IOException e) {
                listener.onError("Bore error: " + e.getMessage());
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