import java.io.*;
import java.net.URISyntaxException;

public class BoreTunnel implements TunnelProvider {

    private Process process;

    @Override
    public void start(int port, TunnelListener listener) {
        Thread t = new Thread(() -> {
            try {
                // ----------Getting bore path ----------------
                // detect OS
                boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
                String boreName = isWindows ? "bore.exe" : "bore";

                // check all possible locations in order
                // String[] locations = {
                // System.getProperty("java.home") + "/../" + boreName, // next to JAR
                // (packaged)
                // "C:\\Program Files\\HelloBros\\" + boreName, // Windows install
                // "/opt/hellobros/" + boreName, // Linux deb
                // "/opt/hellobros/bin/" + boreName, // fallback
                // "./" + boreName // development
                // };
                // get the directory where the app is installed
                String appDir = new File(ChatApp.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI())
                        .getParentFile()
                        .getParent(); // goes up from app/ to HelloBros/

                String[] locations = {
                        appDir + File.separator + boreName, // installed app (all platforms)
                        "./" + boreName // development
                };
                String borePath = "./bore"; // fallback
                for (String loc : locations) {
                    if (new File(loc).exists()) {
                        borePath = loc;
                        break;
                    }
                }
                // --------------- bore processing ------------------------
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
            } catch (URISyntaxException e) {
                listener.onError("AppDir error " + e.getMessage());

            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void stop() {
        if (process != null)
            process.destroy();
    }
}