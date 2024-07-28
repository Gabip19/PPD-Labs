package data_structures.ranking_list;

import com.sun.tools.javac.Main;
import data_structures.MyLinkedList;
import domain.ParticipantEntry;
import solution.Solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingList implements IRankingList {
    protected final MyLinkedList entries = new MyLinkedList();
    protected final Map<Integer, Boolean> excludedIds;

    public RankingList() {
        this.excludedIds = new HashMap<>();
    }

    public RankingList(Map<Integer, Boolean> excludedIds) {
        this.excludedIds = excludedIds;
    }

    public void processParticipantEntry(ParticipantEntry entry) {
        if (excludedIds.containsKey(entry.getId())) {
            return;
        }

        if (entry.getScore() == -1) {
            excludedIds.put(entry.getId(), true);
        }

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

    protected void handleFoundEntry(ParticipantEntry entry, MyLinkedList.Node currentNode) {
        var currentEntry = currentNode.getEntry();

        if (entry.getScore() == -1) {
            entries.removeNode(currentNode);
        } else {
            var newScore = currentEntry.getScore() + entry.getScore();
            currentEntry.setScore(newScore);

            var insertAfterNode = findNodeToInsertAfter(currentNode.getPrevious(), currentEntry);
            entries.insertAfterNode(insertAfterNode, currentEntry);
            entries.removeNode(currentNode);
        }
    }

    protected void handleNotFoundEntry(ParticipantEntry entry, MyLinkedList.Node currentNode) {
        if (currentNode == null && entry.getScore() != -1) {
            var insertAfterNode = findNodeToInsertAfter(entries.getTail(), entry);
            entries.insertAfterNode(insertAfterNode, entry);
        }
    }

    protected MyLinkedList.Node findNodeToInsertAfter(MyLinkedList.Node startNode, ParticipantEntry entry) {
        var currentNode = startNode;
        var score = entry.getScore();
        var id = entry.getId();

        while (currentNode != null && currentNode.getEntry().getScore() < score) {
            currentNode = currentNode.getPrevious();
        }

        while (currentNode != null && currentNode.getEntry().getScore() == score &&
                currentNode.getEntry().getId() > id) {
            currentNode = currentNode.getPrevious();
        }

        return currentNode;
    }

    public List<ParticipantEntry> getEntriesAsList() {
        var entriesList = new ArrayList<ParticipantEntry>();
        var currentNode = entries.getHead();

        while (currentNode != null) {
            entriesList.add(currentNode.getEntry());
            currentNode = currentNode.getNext();
        }

        return entriesList;
    }

    public void writeResultsToFile() {
        try {
            Solution.writeResultsToFile(getEntriesAsList(), "output\\Clasament.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
