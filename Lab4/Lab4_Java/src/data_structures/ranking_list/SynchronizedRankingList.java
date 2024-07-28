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

        synchronized (entries) {
            var currentNode = entries.getHead();
            while (currentNode != null) {
                if (currentNode.getEntry().getId() == entry.getId()) {
                    handleFoundEntry(entry, currentNode);
                    break;
                }

                currentNode = currentNode.getNext();
            }

            handleNotFoundEntry(entry, currentNode);
        }
    }
}
