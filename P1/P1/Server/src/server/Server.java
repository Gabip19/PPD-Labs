package server;

import core.RankingService;
import server.utils.ClientHandler;
import server.utils.ServerConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ServerConfig config;
    private ServerSocket serverSocket;
    private ExecutorService clientHandlersThreadPool;
    private RankingService rankingService;

    public Server(ServerConfig config) {
        this.config = config;
    }

    public void start() {
        try {
            initServer();
            int activeClientsNum = 0;

            while (true) {
                while (activeClientsNum < config.MAX_CLIENTS_NUM) {
                    Socket client = serverSocket.accept();
                    System.out.println("Client connected...");

                    activeClientsNum++;

                    handleNewClientConnection(client);
                }

                rankingService.awaitTerminationAndRestart();
                activeClientsNum = 0;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initServer() throws IOException {
        System.out.println("Starting server...");

        serverSocket = new ServerSocket(config.PORT);
        clientHandlersThreadPool = Executors.newFixedThreadPool(config.MAX_HANDLERS_THREADS);

        rankingService = new RankingService(
            config.READER_THREADS_NUM,
            config.P - config.READER_THREADS_NUM,
            config.QUEUE_SIZE,
            config.MAX_CLIENTS_NUM,
            config.CACHE_LIFE_SPAN_MS
        );

        System.out.println("Server resources have been initialized.");
        System.out.println("Server started on port " + config.PORT + ".\n");
    }

    private void handleNewClientConnection(Socket client) {
        System.out.println("Starting client handler...");
        clientHandlersThreadPool.execute(new ClientHandler(client, rankingService));
    }
}
