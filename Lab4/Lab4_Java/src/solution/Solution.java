package solution;

import data_structures.SynchronizedQueue;
import data_structures.ranking_list.RankingList;
import data_structures.ranking_list.SynchronizedRankingList;
import domain.ParticipantEntry;
import threads.ProcessingThread;
import threads.ReaderThread;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Solution {
    private static int countriesNumber;
    private static int problemsNumber;
    private static int p;
    private static int producerThreadsNum;
    private static List<ParticipantEntry> results;
    private static int setNum;

    public static void writeResultsToFile(List<ParticipantEntry> resultsList, String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        resultsList.forEach(entry -> {
            try {
                writer.write(String.valueOf(entry.getId()));
                writer.write(' ');
                writer.write(String.valueOf(entry.getScore()));
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

    private static void runSequential() throws IOException {
        RankingList resultsList = new RankingList();

        for (int i = 1; i <= countriesNumber; i++) {
            for (int j = 1; j <= problemsNumber; j++) {
                var filePath = "input\\set"+ setNum + "\\RezultateC" + i + "_P" + j + ".txt";
                Scanner scanner = new Scanner(new FileReader(filePath));
                while (scanner.hasNextInt()) {
                    int id = scanner.nextInt();
                    int score = scanner.nextInt();

                    var participantEntry = new ParticipantEntry(id, score);
                    resultsList.processParticipantEntry(participantEntry);
                }
            }
        }

        results = resultsList.getEntriesAsList();
        writeResultsToFile(results, "output\\Clasament_seq.txt");
    }

    private static void runParallel() throws InterruptedException {
        Thread[] threads = new Thread[p];

        int noProducerThreads = producerThreadsNum;

        RankingList resultsList = new SynchronizedRankingList();
        SynchronizedQueue queue = new SynchronizedQueue();

        startProducerThreads(noProducerThreads, threads, queue, resultsList);
        startConsumerThreads(noProducerThreads, threads, queue, resultsList);

        for (int i = 0; i < p; i++) {
            threads[i].join();
        }

        results = resultsList.getEntriesAsList();
    }

    private static void startProducerThreads(int noProducerThreads, Thread[] threads, SynchronizedQueue queue, RankingList resultsList) {
        int totalProblemsNum = countriesNumber * problemsNumber;
        int problemsPerThread = totalProblemsNum / noProducerThreads;
        int remainingProblems = totalProblemsNum % noProducerThreads;

        int start = 0;
        int end;
        for (int i = 0; i < noProducerThreads; i++) {
            int currentPbPerThread = problemsPerThread;

            if (remainingProblems != 0) {
                remainingProblems--;
                currentPbPerThread++;
            }
            end = start + currentPbPerThread;

            threads[i] = new ReaderThread(queue, problemsNumber, start, end, setNum, resultsList);
            threads[i].start();

            start += currentPbPerThread;
        }
    }

    private static void startConsumerThreads(int noProducerThreads, Thread[] threads, SynchronizedQueue queue, RankingList resultsList) {
        for (int i = noProducerThreads; i < p; i++) {
            threads[i] = new ProcessingThread(queue, resultsList);
            threads[i].start();
        }
    }

    public static void Solve(String[] args) throws IOException, InterruptedException {
        int runOption = Integer.parseInt(args[0]);
        p = Integer.parseInt(args[1]);
        producerThreadsNum = Integer.parseInt(args[2]);
        countriesNumber = Integer.parseInt(args[3]);
        problemsNumber = Integer.parseInt(args[4]);
        int checkResult = Integer.parseInt(args[5]);

        setNum = 2;

        switch (runOption) {
            case 0 -> {
                long start = System.nanoTime();
                runSequential();
                long end = System.nanoTime();
                System.out.println((double) (end - start) / 1E6);
            }
            case 1 -> {
                long start = System.nanoTime();
                runParallel();
                long end = System.nanoTime();
                System.out.println((double) (end - start) / 1E6);
            }
        }

        if (checkResult == 1) {
            compareResultsWithSequential();
        }
    }
}
