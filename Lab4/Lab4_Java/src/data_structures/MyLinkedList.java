package data_structures;

import domain.ParticipantEntry;

public class MyLinkedList {

    public static class Node {
        private final ParticipantEntry entry;
        private Node next;
        private Node previous;

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
    }

    private Node head = null;
    private Node tail = null;

    public Node getHead() {
        return head;
    }

    public Node getTail() {
        return tail;
    }

    public void insertAfterNode(Node node, ParticipantEntry value) {
        if (node == null) {
            insertFirst(value);
            return;
        }

        var nextNode = node.next;
        var newNode = new Node(value);

        node.next = newNode;
        newNode.next = nextNode;
        newNode.previous = node;

        if (node != tail) {
            nextNode.previous = newNode;
        } else {
            tail = newNode;
        }
    }

    public void insertBeforeNode(Node node, ParticipantEntry value) {
        if (node == null) {
            insertLast(value);
            return;
        }

        var previousNode = node.previous;
        var newNode = new Node(value);

        node.previous = newNode;
        newNode.previous = previousNode;
        newNode.next = node;

        if (node != head) {
            previousNode.next = newNode;
        } else {
            head = newNode;
        }
    }

    public void removeNode(Node node) {
        if (node == head && node == tail) {
            head = null;
            tail = null;
        } else if (node == head) {
            head = node.next;
            head.previous = null;
        } else if (node == tail) {
            tail = node.previous;
            tail.next = null;
        } else {
            node.previous.next = node.next;
            node.next.previous = node.previous;
        }
    }

    public void insertLast(ParticipantEntry value) {
        var newNode = new Node(value);

        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.previous = tail;
            tail = newNode;
        }
    }

    public void insertFirst(ParticipantEntry value) {
        var newNode = new Node(value);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            head.previous = newNode;
            newNode.next = head;
            head = newNode;
        }
    }
}
