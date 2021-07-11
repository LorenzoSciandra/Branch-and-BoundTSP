package graph.structures;

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

    @Override
    public String toString() {
        return "Edge{" +
                "from=" + from +
                ", to=" + to +
                ", label=" + label +
                '}';
    }
}