package graph.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Node<K, V, E> {
    private K key;
    private V value;

    private HashMap<K, E> edges;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
        edges = new HashMap<>();
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public E getLabel(K to) {
        return edges.get(to);
    }

    public boolean hasEdge(K to) {
        return edges.containsKey(to);
    }

    public void addEdge(K to, E label) {
        edges.put(to, label);
    }

    public Edge<K, E> getEdge(K to) {
        if (edges.containsKey(to)) {
            return new Edge<>(this.key, to, edges.get(to));
        } else {
            return null;
        }
    }

    public ArrayList<Edge<K, E>> getEdges() {
        ArrayList<Edge<K, E>> edgeList = new ArrayList<>();
        this.edges.forEach((k, e) -> edgeList.add(new Edge<>(this.key, k, e)));
        return edgeList;
    }

    public ArrayList<K> getAdjacentNodesKeys() {
        return new ArrayList<>(this.edges.keySet());
    }

    public int edgesCount() {
        return edges.size();
    }

    public void removeEdge(K to) {
        edges.remove(to);
    }

    @Override
    public String toString() {
        return "Node{" +
                "key=" + key +
                ", value=" + value +
                ", edges=" + edges +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?, ?, ?> node = (Node<?, ?, ?>) o;
        return key.equals(node.key) && Objects.equals(value, node.value) && edges.equals(node.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, edges);
    }
}