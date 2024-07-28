package parallelism;

import data_structures.SynchronizedQueue;
import domain.ParticipantEntry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class ReadTask implements Runnable {
    private final SynchronizedQueue queue;
    private final int countryNum;
    private final int problemNum;
    private final int setNum;

    public ReadTask(int countryNum, int problemNum, int setNum, SynchronizedQueue queue) {
        this.countryNum = countryNum;
        this.problemNum = problemNum;
        this.setNum = setNum;
        this.queue = queue;
    }

    @Override
    public void run() {
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

            queue.enqueue(new ParticipantEntry(id, score, countryNum));
        }
    }
}
