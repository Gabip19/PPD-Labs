package data_structures.ranking_list;

import domain.ParticipantEntry;

import java.util.concurrent.ConcurrentHashMap;

public class SynchronizedRankingList extends RankingList {
    public SynchronizedRankingList() {
        super(new ConcurrentHashMap<>());
    }

    @Override
    public void processParticipantEntry(ParticipantEntry entry) {
        if (excludedIds.containsKey(entry.getId())) {
            return;
        }

        if (entry.getScore() == -1) {
            excludedIds.put(entry.getId(), true);
        }

        var tail = entries.getTail();

        var prevNode = entries.getHead();
        prevNode.getLock().lock();

        var currentNode = prevNode.getNext();
        currentNode.getLock().lock();

        while (currentNode != tail) {
            if (currentNode.getEntry().getId() == entry.getId()) {
                if (entry.getScore() == -1) {
                    var nextNode = currentNode.getNext();
                    nextNode.getLock().lock();

                    entries.removeNode(currentNode);

                    nextNode.getLock().unlock();
                } else {
                    entries.updateNode(currentNode, entry);
                }

                prevNode.getLock().unlock();
                currentNode.getLock().unlock();

                return;
            }

            prevNode.getLock().unlock();
            prevNode = currentNode;

            currentNode = prevNode.getNext();
            currentNode.getLock().lock();
        }

        if (entry.getScore() != -1 && !excludedIds.containsKey(entry.getId())) {
            entries.insertAfterNode(prevNode, entry);
        }

        prevNode.getLock().unlock();
        currentNode.getLock().unlock();
    }
}
