package core.parallelism;

import core.data_structures.SynchronizedQueue;
import network.models.ParticipantEntry;
import network.models.Entry;

public class EnqueuingTask implements Runnable {
    private final SynchronizedQueue queue;
    private final Entry[] entries;
    private final int countryNum;

    public EnqueuingTask(SynchronizedQueue queue, Entry[] entries, int countryNum) {
        this.queue = queue;
        this.entries = entries;
        this.countryNum = countryNum;
    }

    @Override
    public void run() {
        for (var entry : entries) {
            queue.enqueue(new ParticipantEntry(entry.getId(), entry.getScore(), countryNum));
        }
    }
}
