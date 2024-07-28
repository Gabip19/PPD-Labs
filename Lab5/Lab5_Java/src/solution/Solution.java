package solution;

import data_structures.SynchronizedQueue;
import data_structures.ranking_list.RankingList;
import data_structures.ranking_list.SynchronizedRankingList;
import domain.ParticipantEntry;
import parallelism.ProcessingThread;
import parallelism.ReadTask;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Solution {
    private static int countriesNumber;
    private static int problemsNumber;
    private static int p;
    private static int producerThreadsNum;
    private static List<ParticipantEntry> results;
    private static int queueSize;
    private static int setNum;

    public static void writeResultsToFile(List<ParticipantEntry> resultsList, String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        resultsList.forEach(entry -> {
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
    }

    private static void compareResultsWithSequential() throws FileNotFoundException {
        var filePath = "output\\Clasament_seq.txt";
        Scanner scanner = new Scanner(new FileReader(filePath));

        for (var entry : results) {
            int id = scanner.nextInt();
            int score = scanner.nextInt();

            if (entry.getId() != id || entry.getScore() != score) {
                System.err.println("Results do not match.");
                return;
            }
        }
    }

    private static void runParallel() throws InterruptedException, IOException {
        int noConsumerThreads = p - producerThreadsNum;
        Thread[] threads = new Thread[noConsumerThreads];

        RankingList resultsList = new SynchronizedRankingList();
        SynchronizedQueue queue = new SynchronizedQueue(queueSize);

        queue.startProducers();

        var executor = startReadTasksExecutor(producerThreadsNum, queue);
        startConsumerThreads(noConsumerThreads, threads, queue, resultsList);

        var success = executor.awaitTermination(5, TimeUnit.MINUTES);

        queue.stopProducers();

        for (int i = 0; i < noConsumerThreads; i++) {
            threads[i].join();
        }

        if (success) {
            results = resultsList.getEntriesAsList();
            results.sort((t1, t2) -> {
                if (t1.getScore() == t2.getScore()) {
                    return t1.getId() - t2.getId();
                } else if (t1.getScore() < t2.getScore()) {
                    return 1;
                }
                return -1;
            });
            writeResultsToFile(results, "output\\Clasament.txt");
        }
    }

    private static ExecutorService startReadTasksExecutor(int noProducerThreads, SynchronizedQueue queue) {
        var executor = Executors.newFixedThreadPool(noProducerThreads);

        for (int i = 1; i <= countriesNumber; i++) {
            for (int j = 1; j <= problemsNumber; j++) {
                Runnable task = new ReadTask(i, j, setNum, queue);
                executor.execute(task);
            }
        }

        executor.shutdown();
        return executor;
    }

    private static void startConsumerThreads(int noConsumerThreads, Thread[] threads, SynchronizedQueue queue, RankingList resultsList) {
        for (int i = 0; i < noConsumerThreads; i++) {
            threads[i] = new ProcessingThread(queue, resultsList);
            threads[i].start();
        }
    }

    public static void Solve(String[] args) throws IOException, InterruptedException {
        queueSize = Integer.parseInt(args[0]);
        p = Integer.parseInt(args[1]);
        producerThreadsNum = Integer.parseInt(args[2]);
        countriesNumber = Integer.parseInt(args[3]);
        problemsNumber = Integer.parseInt(args[4]);
        int checkResult = Integer.parseInt(args[5]);

        setNum = 2;

        long start = System.nanoTime();
        runParallel();
        long end = System.nanoTime();
        System.out.println((double) (end - start) / 1E6);

        if (checkResult == 1) {
            compareResultsWithSequential();
        }
    }
}
