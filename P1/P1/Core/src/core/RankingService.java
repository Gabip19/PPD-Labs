package core;

import core.data_structures.SynchronizedQueue;
import core.data_structures.ranking_list.RankingList;
import core.data_structures.ranking_list.SynchronizedRankingList;
import network.models.FinalResultsDto;
import network.models.ParticipantEntry;
import core.parallelism.ProcessingThread;
import core.parallelism.EnqueuingTask;
import network.models.Entry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RankingService {
    private final int producerThreadsNum;
    private final int consumerThreadsNum;
    private final int queueSize;
    private final int maxClientsNum;
    private SynchronizedQueue queue;
    private ExecutorService enqueuingThreadPool;
    private Thread[] consumerThreads;
    private RankingList resultsList;
    private List<ParticipantEntry> results = null;

    public RankingService(
            int producerThreadsNum,
            int consumerThreadsNum,
            int queueSize,
            int maxClientsNum,
            long cacheLifeSpanMS
    ) {
        this.producerThreadsNum = producerThreadsNum;
        this.consumerThreadsNum = consumerThreadsNum;
        this.queueSize = queueSize;
        this.maxClientsNum = maxClientsNum;
        this.CACHE_LIFE_SPAN_MS = cacheLifeSpanMS;

        initService();
    }

    private void initService() {
        log("Initializing service...");

        queue = new SynchronizedQueue(queueSize);
        resultsList = new SynchronizedRankingList();

        enqueuingThreadPool = Executors.newFixedThreadPool(producerThreadsNum);
        queue.startProducers();

        startConsumerThreads(queue, resultsList);

        lastCacheUpdate = null;
        serverFinished = false;

        log("Service initialized.");
    }

    public void awaitTerminationAndRestart() {
        lock.lock();
        log("Trying to compute the final results...");

        try {
            while (finishedClientsCounter != maxClientsNum) {
                log("Waiting for all clients to finish...");
                allClientsProcessed.await();
            }

            log("All clients finished.");
            enqueuingThreadPool.shutdown();

            var a = enqueuingThreadPool.awaitTermination(10, TimeUnit.MINUTES);

            queue.stopProducers();

            for (int i = 0; i < consumerThreadsNum; i++) {
                consumerThreads[i].join();
            }

            results = resultsList.getEntriesAsList();
            results.sort((t1, t2) -> {
                if (t1.getScore() == t2.getScore()) {
                    return t1.getId() - t2.getId();
                } else if (t1.getScore() < t2.getScore()) {
                    return 1;
                }
                return -1;
            });

            lastCacheUpdate = null;
            countriesRankingCache = computeCountriesRanking();

            serverFinished = true;

            log("Waking up all clients...");

            resultsReady.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        try {
            writeFinalResultsToFile(results, "output/final_results.txt");
            writeCountriesResultsToFile(countriesRankingCache, "output/countries_results.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFinalResultsToFile(List<ParticipantEntry> finalResultsList, String filePath) throws IOException {
        log("Writing the final participants results to file...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        finalResultsList.forEach(entry -> {
            try {
                writer.write(String.valueOf(entry.getId()));
                writer.write(' ');
                writer.write(String.valueOf(entry.getScore()));
                writer.write(' ');
                writer.write(String.valueOf(entry.getCountryNum()));
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writer.close();
        log("Writing to file finished.");
    }

    public static void writeCountriesResultsToFile(Entry[] countriesResults, String filePath) throws IOException {
        log("Writing the countries results to file...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        for (var entry : countriesResults) {
            try {
                writer.write(String.valueOf(entry.getId()));
                writer.write(' ');
                writer.write(String.valueOf(entry.getScore()));
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        writer.close();
        log("Writing to file finished.");
    }

//    private void compareResultsWithSequential() throws FileNotFoundException {
//        var filePath = "output\\Clasament_seq.txt";
//        Scanner scanner = new Scanner(new FileReader(filePath));
//
//        for (var entry : results) {
//            int id = scanner.nextInt();
//            int score = scanner.nextInt();
//
//            if (entry.getId() != id || entry.getScore() != score) {
//                System.err.println("Results do not match.");
//                return;
//            }
//        }
//    }

    private void startConsumerThreads(SynchronizedQueue queue, RankingList resultsList) {
        log("Starting consumer threads...");
        consumerThreads = new Thread[consumerThreadsNum];

        for (int i = 0; i < consumerThreadsNum; i++) {
            consumerThreads[i] = new ProcessingThread(queue, resultsList);
            consumerThreads[i].start();
        }
    }

    public void processEntriesBatch(Entry[] entries, int countryNum) {
        var enqueuingTask = new EnqueuingTask(queue, entries, countryNum);
        enqueuingThreadPool.execute(enqueuingTask);
    }

    private final long CACHE_LIFE_SPAN_MS;
    private LocalDateTime lastCacheUpdate = null;
    private Entry[] countriesRankingCache;

    public synchronized Entry[] computeCountriesRanking() {
        log("Computing the countries ranking...");
        if (lastCacheUpdate != null && ChronoUnit.MILLIS.between(lastCacheUpdate, LocalDateTime.now()) < CACHE_LIFE_SPAN_MS) {
            log("Using ranking cache.");
            return countriesRankingCache;
        }

        var entriesSet = new HashMap<Integer, Integer>();

        resultsList.getEntriesAsList().forEach(entry -> entriesSet.put(
            entry.getCountryNum(),
            entriesSet.getOrDefault(entry.getCountryNum(), 0) + entry.getScore()
        ));

        countriesRankingCache = entriesSet.entrySet()
                .stream()
                .map(entry -> new Entry(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(entry -> -1 * entry.getScore()))
                .toArray(Entry[]::new);

        lastCacheUpdate = LocalDateTime.now();

        return countriesRankingCache;
    }

    private final Lock lock = new ReentrantLock();
    private int finishedClientsCounter = 0;
    private final Condition allClientsProcessed = lock.newCondition();
    private boolean serverFinished = false;
    private final Condition resultsReady = lock.newCondition();

    public void incrementFinishedClientCounter() {
        lock.lock();
        finishedClientsCounter++;
        log("Client waiting for final results: " + finishedClientsCounter + "/" + maxClientsNum);
        if (finishedClientsCounter == maxClientsNum) {
            log("All clients finished processing.");
            log("Waking up the server thread...");
            allClientsProcessed.signalAll();
        }
        lock.unlock();
    }

    public FinalResultsDto getFinalResults() {
        lock.lock();
        try {
            while (!serverFinished) {
                resultsReady.await();
            }

            finishedClientsCounter--;
            if (finishedClientsCounter == 0) {
                log("Restarting the service...");
                initService();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }

        return new FinalResultsDto(
            countriesRankingCache,
            results.toArray(new ParticipantEntry[0])
        );
    }

    private static void log(String message) {
        System.out.println("[RANKING SERVICE]: " + message);
    }
}
