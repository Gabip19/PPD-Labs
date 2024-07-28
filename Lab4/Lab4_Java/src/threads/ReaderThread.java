package threads;

import data_structures.SynchronizedQueue;
import data_structures.ranking_list.RankingList;
import domain.ParticipantEntry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class ReaderThread extends Thread {
    private final SynchronizedQueue queue;
    private final int problemsNumber;
    private final int start;
    private final int end;
    private final int setNum;
    private final RankingList resultsList;

    public ReaderThread(SynchronizedQueue queue, int problemsNumber, int start, int end, int setNum, RankingList resultsList) {
        this.queue = queue;
        this.problemsNumber = problemsNumber;
        this.start = start;
        this.end = end;
        this.setNum = setNum;
        this.resultsList = resultsList;
    }

    @Override
    public void run() {
        queue.startProducer();

        for (int i = start; i < end; i++) {
            int countryNum = i / problemsNumber + 1;
            int problemNum = i % problemsNumber + 1;

            var filePath = "input\\set" + setNum + "\\RezultateC" + countryNum + "_P" + problemNum + ".txt";
            Scanner scanner;
            try {
                scanner = new Scanner(new FileReader(filePath));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            while (scanner.hasNextInt()) {
                int id = scanner.nextInt();
                int score = scanner.nextInt();

                queue.enqueue(new ParticipantEntry(id, score));
            }
        }

        queue.stopProducer();

        if (start == 0) {
            queue.waitForProcessingToFinish();
            resultsList.writeResultsToFile();
        }
    }
}
