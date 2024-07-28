import server.Server;
import server.utils.ServerConfig;

public class Main {
    private final static int PORT = 3333;

    public static void main(String[] args) {
        ServerConfig config = new ServerConfig(PORT, args);
        Server server = new Server(config);
        server.start();
    }
}