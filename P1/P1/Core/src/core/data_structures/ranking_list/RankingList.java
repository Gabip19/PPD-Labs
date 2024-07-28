package core.data_structures.ranking_list;

import core.data_structures.MyLinkedList;
import network.models.ParticipantEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RankingList implements IRankingList {
    protected final MyLinkedList entries = new MyLinkedList();
    protected final Map<Integer, Boolean> excludedIds;

    public RankingList(Map<Integer, Boolean> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public abstract void processParticipantEntry(ParticipantEntry entry);

    public List<ParticipantEntry> getEntriesAsList() {
        var entriesList = new ArrayList<ParticipantEntry>();
        var tail = entries.getTail();
        var currentNode = entries.getHead().getNext();

        while (currentNode != tail) {
            entriesList.add(currentNode.getEntry());
            currentNode = currentNode.getNext();
        }

        return entriesList;
    }
}
