package data_structures;

import domain.ParticipantEntry;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyLinkedList {

    public static class Node {

        private final ParticipantEntry entry;
        private Node next;
        private Node previous;
        private final Lock lock = new ReentrantLock();
        public Node(ParticipantEntry entry) {
            this.entry = entry;
            this.next = null;
            this.previous = null;
        }

        public Node(ParticipantEntry entry, Node next, Node previous) {
            this.entry = entry;
            this.next = next;
            this.previous = previous;
        }

        public ParticipantEntry getEntry() {
            return entry;
        }

        public Node getNext() {
            return next;
        }

        public Node getPrevious() {
            return previous;
        }

        public Lock getLock() {
            return lock;
        }

    }

    public MyLinkedList() {
        head = new Node(null);
        tail = new Node(null);

        head.next = tail;
        tail.previous = head;
    }

    private final Node head;
    private final Node tail;

    public Node getHead() {
        return head;
    }
    public Node getTail() {
        return tail;
    }

    public void insertAfterNode(Node node, ParticipantEntry value) {
        var nextNode = node.next;
        var newNode = new Node(value);

        node.next = newNode;
        newNode.previous = node;

        newNode.next = nextNode;
        nextNode.previous = newNode;
    }

    public void insertBeforeNode(Node node, ParticipantEntry value) {
        var previousNode = node.previous;
        var newNode = new Node(value);

        node.previous = newNode;
        newNode.next = node;

        newNode.previous = previousNode;
        previousNode.next = newNode;
    }

    public void removeNode(Node node) {
        node.previous.next = node.next;
        node.next.previous = node.previous;
    }

    public void updateNode(Node node, ParticipantEntry entry) {
        var nodeEntry = node.getEntry();
        var newScore = nodeEntry.getScore() + entry.getScore();
        nodeEntry.setScore(newScore);
    }
}
