package client;

import network.models.Entry;
import network.models.FinalResultsDto;
import network.requests.Request;
import network.requests.RequestType;
import network.responses.Response;
import network.responses.ResponseType;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ClientTask implements Runnable {
    private final int countryNum;
    private final int problemsNum;
    private final int setNum;

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 3333;
    public final int CHUNK_SIZE;
    public final int DELAY_BETWEEN_BLOCKS_MS;

    private Socket client;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ClientTask(int countryNum, int problemsNum, int setNum, int chunkSize, int delayBetweenBlocksMs) {
        this.countryNum = countryNum;
        this.problemsNum = problemsNum;
        this.setNum = setNum;
        CHUNK_SIZE = chunkSize;
        DELAY_BETWEEN_BLOCKS_MS = delayBetweenBlocksMs;
    }

    @Override
    public void run() {
        try {
            connectToServer();
            processEntriesAsync();
            requestFinalResults();
            sendCloseRequest();
        } catch (IOException | ExecutionException | InterruptedException e) {
            log("[ERROR] " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void requestPartialCountriesRanking() throws ExecutionException, InterruptedException {
        log("Sending partial ranking request...");

        var response = sendRequestToServerAsync(new Request().setType(RequestType.PARTIAL)).get();

        if (response.getType() == ResponseType.OK) {
            log("Partial ranking request finished successfully.");

            Entry[] ranking = (Entry[]) response.getData();

            StringBuilder rank = new StringBuilder();
            for (var entry : ranking) {
                rank.append("Country: ").append(entry.getId()).append(" Score: ").append(entry.getScore()).append(", ");
            }

            log(rank.toString());
        }
    }

    private void requestFinalResults() throws ExecutionException, InterruptedException {
        log("Sending final results request...");

        var response = sendRequestToServerAsync(new Request().setType(RequestType.FINAL)).get();

        if (response.getType() == ResponseType.OK) {
            log("Final results request finished successfully.");

            FinalResultsDto finalResults = (FinalResultsDto) response.getData();

            StringBuilder rank = new StringBuilder();
            for (var entry : finalResults.getCountriesResults()) {
                rank.append("Country: ").append(entry.getId()).append(" Score: ").append(entry.getScore()).append(", ");
            }

            log(rank.toString());
        }
    }

    private void connectToServer() throws IOException, ExecutionException, InterruptedException {
        log("Connecting to server...");

        client = new Socket(SERVER_IP, SERVER_PORT);
        inputStream = new ObjectInputStream(client.getInputStream());
        outputStream = new ObjectOutputStream(client.getOutputStream());
        outputStream.flush();

        var sendTask = sendRequestToServerAsync(
            new Request()
            .setType(RequestType.START)
            .setData(countryNum)
        );

        if (sendTask.get().getType() != ResponseType.READY) {
            throw new IOException("Connection failed.");
        }

        log("Server ready to handle requests.");
    }

    private void closeConnection() {
        log("Closing connection to server...");

        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (client != null)
                client.close();
        } catch (IOException e) {
            log("[ERROR] Closing failed: " + e.getMessage());
        }
    }

    private void sendCloseRequest() throws ExecutionException, InterruptedException {
        log("Sending close request...");

        var response = sendRequestToServerAsync(new Request().setType(RequestType.STOP)).get();

        if (response.getType() == ResponseType.OK) {
            log("Close request finished successfully.");
        }
    }

    private void processEntriesAsync() throws InterruptedException, ExecutionException {
        log("Starting to process the entries.");

        var sendTask = CompletableFuture.completedFuture(
            new Response().setType(ResponseType.OK)
        );

        for (int i = 1; i <= problemsNum; i++) {
            var filePath = "Client\\input\\set" + setNum + "\\RezultateC" + countryNum + "_P" + i + ".txt";

            ArrayList<Entry> entriesChunk = new ArrayList<>();

            Scanner scanner;
            try {
                scanner = new Scanner(new FileReader(filePath));
            } catch (FileNotFoundException e) {
                log("[ERROR] File not found: " + filePath);
                continue;
            }

            while (scanner.hasNextInt()) {
                int id = scanner.nextInt();
                int score = scanner.nextInt();

                entriesChunk.add(new Entry(id, score));
                if (entriesChunk.size() == CHUNK_SIZE) {
                    Thread.sleep(DELAY_BETWEEN_BLOCKS_MS);
                    sendTask = sendEntriesChunkAsync(sendTask, entriesChunk);
                    entriesChunk = new ArrayList<>();
                }
            }

            if (!entriesChunk.isEmpty()) {
                sendTask = sendEntriesChunkAsync(sendTask, entriesChunk);
            }

            if (sendTask.get().getType() != ResponseType.OK) {
                log("[ERROR] Server responded with an error: " + sendTask.get().getData());
            }

            requestPartialCountriesRanking();
        }
    }

    private CompletableFuture<Response> sendRequestToServerAsync(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                outputStream.writeObject(request);
                outputStream.flush();

                return (Response) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {
            log("[ERROR]: Sending the request to the server failed: " + ex.getMessage());
            return new Response().setType(ResponseType.ERROR).setData(ex.getMessage());
        });
    }

    private CompletableFuture<Response> sendEntriesChunkAsync(CompletableFuture<Response> sendTask, ArrayList<Entry> entriesChunk) throws InterruptedException, ExecutionException {
        log("Sending entries chunk...");

        try {
            if (sendTask.get().getType() == ResponseType.OK) {
                sendTask = sendRequestToServerAsync(
                    new Request()
                        .setType(RequestType.PROCESS)
                        .setData(entriesChunk.toArray(new Entry[0]))
                );

                log("Entries chunk sent.");
            } else {
                log("[ERROR] Server responded with an error: " + sendTask.get().getData());
            }
        } catch (InterruptedException | ExecutionException e) {
            log("[ERROR] An error occurred while trying to send the entries chunk.");
            throw e;
        }

        return sendTask;
    }

    private void log(String message) {
        System.out.println("[Client " + countryNum + " " + LocalTime.now() + "]: " + message);
    }
}
