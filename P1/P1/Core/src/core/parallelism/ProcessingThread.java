package core.parallelism;

import core.data_structures.SynchronizedQueue;
import core.data_structures.ranking_list.IRankingList;

public class ProcessingThread extends Thread {
    private final SynchronizedQueue queue;
    private final IRankingList resultList;

    public ProcessingThread(SynchronizedQueue queue, IRankingList resultList) {
        this.queue = queue;
        this.resultList = resultList;
    }

    @Override
    public void run() {
        queue.startConsumer();

        while (true) {
            try {
                var entry = queue.dequeue();

                if (entry == null) {
                    break;
                }

//                System.out.println("Dequeue: " + entry.getId() + " " + entry.getScore() + " " + entry.getCountryNum());

                resultList.processParticipantEntry(entry);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        queue.stopConsumer();
    }
}
