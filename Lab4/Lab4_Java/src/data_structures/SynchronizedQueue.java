package data_structures;

import domain.ParticipantEntry;

import java.util.LinkedList;
import java.util.Queue;

public class SynchronizedQueue {
    private final Queue<ParticipantEntry> queue = new LinkedList<>();
    private int activeProducers = 0;
    private int activeConsumers = 0;
    private final Object lock = new Object();

    public synchronized void enqueue(ParticipantEntry element) {
        queue.offer(element);
        notify();
    }

    public synchronized ParticipantEntry dequeue() throws InterruptedException {
        while (queue.isEmpty() && activeProducers != 0) {
            wait();
            if (queue.isEmpty() && activeProducers == 0) {
                return null;
            }
        }
        return queue.poll();
    }

    public synchronized void startProducer() {
        activeProducers++;
    }

    public synchronized void stopProducer() {
        activeProducers--;
        if (activeProducers == 0) {
            notifyAll();
        }
    }

    public synchronized void startConsumer() {
        activeConsumers++;
    }

    public synchronized void stopConsumer() {
        activeConsumers--;
        if (activeConsumers == 0) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    public void waitForProcessingToFinish() {
        int activeProds;
        int activeCons;

        synchronized (this) {
            activeCons = activeConsumers;
            activeProds = activeProducers;
        }

        if (activeCons != 0 && activeProds != 0) {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
