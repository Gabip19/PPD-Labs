package core.data_structures;

import network.models.ParticipantEntry;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedQueue {
    private final Queue<ParticipantEntry> queue = new LinkedList<>();
    private int activeConsumers = 0;
    private boolean producersAreRunning = false;
    private int size = 0;
    private final int maxSize;
    private final Object locker = new Object();

    public int getSize() {
        return size;
    }

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public SynchronizedQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    public void enqueue(ParticipantEntry element) {
        lock.lock();
        try {
            while (size == maxSize && activeConsumers != 0) {
                notFull.await();
            }

            if (activeConsumers == 0) {
                return;
            }

            queue.offer(element);
            size++;
            notEmpty.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public ParticipantEntry dequeue() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty() && producersAreRunning) {
                notEmpty.await();
                if (queue.isEmpty() && !producersAreRunning) {
                    return null;
                }
            }

            var elem = queue.poll();
            size--;
            notFull.signalAll();

            return elem;
        } finally {
            lock.unlock();
        }
    }

    public void startProducers() {
        lock.lock();
        producersAreRunning = true;
        lock.unlock();
    }

    public void stopProducers() {
        lock.lock();
        producersAreRunning = false;
        notEmpty.signalAll();
        lock.unlock();
    }

    public void startConsumer() {
        lock.lock();
        activeConsumers++;
        lock.unlock();
    }

    public void stopConsumer() {
        lock.lock();
        activeConsumers--;
        if (activeConsumers == 0) {
            synchronized (locker) {
                locker.notifyAll();
            }
        }
        lock.unlock();
    }
}
