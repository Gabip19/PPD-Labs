package server.utils;

public class ServerConfig {
    public final int PORT;
    public final int MAX_HANDLERS_THREADS;
    public final int READER_THREADS_NUM;
    public final int P;
    public final int MAX_CLIENTS_NUM;
    public final int QUEUE_SIZE;
    public final long CACHE_LIFE_SPAN_MS;

    public ServerConfig(int port, String[] args) {
        this.PORT = port;
        this.MAX_HANDLERS_THREADS = Integer.parseInt(args[0]);
        this.P = Integer.parseInt(args[1]);
        this.READER_THREADS_NUM = Integer.parseInt(args[2]);
        this.MAX_CLIENTS_NUM = Integer.parseInt(args[3]);
        this.QUEUE_SIZE = Integer.parseInt(args[4]);
        this.CACHE_LIFE_SPAN_MS = Integer.parseInt(args[5]);
    }
}
