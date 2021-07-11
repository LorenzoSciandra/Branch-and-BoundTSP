package graph.structures;

import java.util.HashMap;
import java.util.Objects;

public class Edge<K, E> {
    private K from;
    private K to;
    private E label;

    public Edge(K from, K to, E label) {
        this.from = from;
        this.to = to;
        this.label = label;
    }

    public K getFrom() {
        return from;
    }

    public K getTo() {
        return to;
    }

    public E getLabel() {
        return label;
    }

    public Edge<K, E> inverse() {
        return new Edge<>(to, from, label);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "from=" + from +
                ", to=" + to +
                ", label=" + label +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?, ?> edge = (Edge<?, ?>) o;
        return from.equals(edge.from) && to.equals(edge.to) && label.equals(edge.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, label);
    }
}