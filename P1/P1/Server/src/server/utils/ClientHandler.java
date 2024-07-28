package server.utils;

import core.RankingService;
import network.models.Entry;
import network.requests.Request;
import network.responses.Response;
import network.responses.ResponseType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class ClientHandler implements Runnable {
    private final Socket client;
    private final RankingService rankingService;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    boolean clientConnected;
    private int countryNum;

    public ClientHandler(Socket client, RankingService rankingService) {
        this.rankingService = rankingService;
        this.clientConnected = true;
        this.client = client;

        log("Client handler created.");
    }

    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(client.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(client.getInputStream());

            log("Client handler started.");

            while (clientConnected) {
                try {
                    Request request = (Request) inputStream.readObject();

                    processRequestAsync(new Request()
                            .setType(request.getType())
                            .setData(request.getData())
                    ).thenAccept(this::sendResponse);
                } catch (ClassNotFoundException | IllegalArgumentException e) {
                    log("[ERROR] " + e.getMessage());
                    sendResponse(new Response()
                            .setType(ResponseType.ERROR)
                            .setData(e.getMessage())
                    );
                }
            }
        } catch (IOException e) {
            log("[ERROR] " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        log("Closing connection to client...");

        clientConnected = false;

        try {
            inputStream.close();
            outputStream.close();
            client.close();
        } catch (IOException e) {
            log("[ERROR] Closing failed: " + e.getMessage());
        }
    }

    private CompletableFuture<Response> processRequestAsync(Request request) {
        log("Received " + request.getType() + " request.");

        switch (request.getType()) {
            case START -> {
                countryNum = (int) request.getData();

                return CompletableFuture.completedFuture(
                    new Response().setType(ResponseType.READY)
                );
            }
            case STOP -> {
                clientConnected = false;

                return CompletableFuture.completedFuture(
                    new Response().setType(ResponseType.OK)
                );
            }
            case PROCESS -> {
                var entries = (Entry[]) request.getData();

                log("Received: " + entries.length + " entries.");

                return CompletableFuture.supplyAsync(() -> {
                    rankingService.processEntriesBatch(entries, countryNum);
                    return new Response().setType(ResponseType.OK);
                });
            }
            case PARTIAL -> {
                return CompletableFuture.supplyAsync(() -> {
                    var ranking = rankingService.computeCountriesRanking();
                    return new Response()
                            .setType(ResponseType.OK)
                            .setData(ranking);
                });
            }
            case FINAL -> {
                return CompletableFuture.supplyAsync(() -> {
                    log("Incrementing the FINISHED client counter.");
                    rankingService.incrementFinishedClientCounter();

                    var results = rankingService.getFinalResults();
                    return new Response()
                            .setType(ResponseType.OK)
                            .setData(results);
                });
            }
            default -> throw new IllegalArgumentException("Unknown request type.");
        }
    }

    private void sendResponse(Response response) {
        log("Sending " + response.getType() + " response.");

        try {
            outputStream.writeObject(response);
            outputStream.flush();
        } catch (IOException e) {
            log("[ERROR]: Sending the response to the client failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void log(String message) {
        if (countryNum == 0) {
            System.out.println("[Handler]: " + message);
        } else {
            System.out.println("[Handler C" + countryNum + "]: " + message);
        }
    }
}
