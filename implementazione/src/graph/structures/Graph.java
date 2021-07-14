package graph.structures;

import graph.exceptions.GraphEdgeMissingException;
import graph.exceptions.GraphNodeMissingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Graph<K, V, E> implements Cloneable {

    private final HashMap<K, Node<K, V, E>> nodes;

    private final boolean directed;

    public Graph(boolean directed) {
        nodes = new HashMap<>();
        this.directed = directed;
    }

    public boolean isDirected() {
        return directed;
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int edgeCount() {
        int edgesCounter = nodes.values().stream().mapToInt(Node::getDegree).sum();
        return directed ? edgesCounter : edgesCounter / 2;
    }

    public Graph<K, V, E> addNode(@NotNull K key) {
        addNode(key, null);
        return this;
    }

    public Graph<K, V, E> addNode(@NotNull K key, @Nullable V value) {
        if (!containsNode(key)) {
            nodes.put(key, new Node<>(key, value));
        }
        return this;
    }

    public @Nullable Node<K, V, E> getNode(K key) {
        return nodes.get(key);
    }

    public boolean containsNode(K key) {
        return nodes.containsKey(key);
    }

    public ArrayList<Node<K, V, E>> getNodes() {
        return new ArrayList<>(this.nodes.values());
    }

    public Graph<K, V, E> removeNode(K key) {
        for (Node<K, V, E> node : nodes.values()) {
            node.removeEdge(key);
        }

        nodes.remove(key);

        return this;
    }

    public Graph<K, V, E> addEdge(@NotNull Edge<K, E> edge) throws GraphNodeMissingException {
        return this.addEdge(edge.getFrom(), edge.getTo(), edge.getLabel());
    }

    @SuppressWarnings("Duplicates")
    public Graph<K, V, E> addEdge(@NotNull K from, @NotNull K to, @Nullable E label) throws GraphNodeMissingException {
        Node<K, V, E> fromNode = nodes.get(from);
        Node<K, V, E> toNode = nodes.get(to);

        if (fromNode == null) {
            if (toNode == null) {
                throw new GraphNodeMissingException("Missing Nodes FROM and TO in graph.");
            } else {
                throw new GraphNodeMissingException("Missing Node FROM in graph.");
            }
        } else {
            if (toNode == null) {
                throw new GraphNodeMissingException("Missing Node TO in graph.");
            }
        }

        if (!fromNode.hasEdge(to)) {
            fromNode.addEdge(to, label);
            if (!directed) {
                toNode.addEdge(from, label);
            }
        }

        return this;
    }

    public Graph<K, V, E> addNodesEdge(@NotNull K fromKey, @NotNull K toKey, @Nullable E label) {
        return addNodesEdge(fromKey, null, toKey, null, label);
    }

    public Graph<K, V, E> addNodesEdge(@NotNull K fromKey, @Nullable V fromValue, @NotNull K toKey, @Nullable V toValue,
                                       @Nullable E label) {

        Node<K, V, E> fromNode = nodes.get(fromKey);
        Node<K, V, E> toNode = nodes.get(toKey);

        if (fromNode == null) {
            addNode(fromKey, fromValue);
            fromNode = nodes.get(fromKey);
        }

        if (toNode == null) {
            addNode(toKey, toValue);
            toNode = nodes.get(toKey);
        }

        if (!fromNode.hasEdge(toKey)) {
            fromNode.addEdge(toKey, label);
            if (!directed) {
                toNode.addEdge(fromKey, label);
            }
        }

        return this;
    }

    public @Nullable
    Edge<K, E> getEdge(K from, K to) {
        // Thanks prof.
        Node<K, V, E> fromNode = nodes.get(from);

        if (fromNode != null) {
            return fromNode.getEdge(to);
        } else {
            return null;
        }
    }

    public boolean containsEdge(K from, K to) {
        return getEdge(from, to) != null;
    }

    public ArrayList<Edge<K, E>> getEdges() {
        return getEdges(false);
    }

    /**
     * Returns all edges of the graph.
     *
     * @param returnAllEdges set to true if ALL edges are needed (useful with undirected graphs if both copies of an
     *                       edge are needed: (1,2) and (2,1) for example).
     * @implNote If the graph is undirected, only one edge is returned (this graph implementation uses two directed
     * edges for every undirected edge).
     */
    public ArrayList<Edge<K, E>> getEdges(boolean returnAllEdges) {
        ArrayList<Edge<K, E>> edges = new ArrayList<>();
        if (directed || returnAllEdges) {
            for (Node<K, V, E> node : nodes.values()) {
                edges.addAll(node.getEdges());
            }
        } else {
            HashMap<K, HashSet<K>> edgesPresent = new HashMap<>();

            // If the graph is undirected, an edge is sufficent; we don't need the inverse.
            for (Node<K, V, E> node : nodes.values()) {
                for (Edge<K, E> edge : node.getEdges()) {
                    K fromNode = edge.getFrom();
                    K toNode = edge.getTo();

                    boolean inversePresent = edgesPresent.containsKey(toNode) &&
                                             edgesPresent.get(toNode).contains(fromNode);

                    if (!inversePresent) {
                        HashSet<K> forwardStar = edgesPresent.computeIfAbsent(fromNode, k -> new HashSet<>());
                        forwardStar.add(toNode);
                        edges.add(edge);
                    }
                }
            }
        }
        return edges;
    }

    public ArrayList<Node<K, V, E>> getAdjacentNodes(K key) {
        ArrayList<Node<K, V, E>> adjacentNodes = new ArrayList<>();
        nodes.get(key).getAdjacentNodesKeys().forEach(k -> adjacentNodes.add(nodes.get(k)));
        return adjacentNodes;
    }

    @SuppressWarnings("Duplicates")

    public Graph<K, V, E> updateEdge(@NotNull K from, @NotNull K to, @Nullable E newLabel) throws GraphNodeMissingException,
        GraphEdgeMissingException {
        Node<K, V, E> fromNode = nodes.get(from);
        Node<K, V, E> toNode = nodes.get(to);

        if (fromNode == null) {
            if (toNode == null) {
                throw new GraphNodeMissingException("Missing Nodes FROM and TO in graph.");
            } else {
                throw new GraphNodeMissingException("Missing Node FROM in graph.");
            }
        } else {
            if (toNode == null) {
                throw new GraphNodeMissingException("Missing Node TO in graph.");
            }
        }

        // We should update an edge only if it already exists.
        // This check is needed because we use a property of HashMaps:
        // if we add a value with a key that already exists, the old value is replaced.
        // This removed the need to make a custom method.
        if (fromNode.hasEdge(to)) {
            fromNode.addEdge(to, newLabel);
            if (!directed) {
                toNode.addEdge(from, newLabel);
            }
        } else {
            throw new GraphEdgeMissingException("Missing EDGE in graph.");
        }

        return this;
    }

    public Graph<K, V, E> removeEdge(@NotNull K from, @NotNull K to) throws GraphNodeMissingException {
        Node<K, V, E> fromNode = nodes.get(from);
        Node<K, V, E> toNode = nodes.get(to);

        if (fromNode != null) {
            fromNode.removeEdge(to);
            if (!directed) {
                if (toNode != null) {
                    toNode.removeEdge(from);
                } else {
                    throw new GraphNodeMissingException("Missing Node TO in graph");
                }
            }
        } else {
            throw new GraphNodeMissingException("Missing Node FROM in graph.");
        }

        return this;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Graph<K, V, E> clone() {
        Graph<K, V, E> newGraph = new Graph<>(this.directed);

        // copy all edges of all nodes.
        for (Node<K, V, E> node : getNodes()) {
            for (Edge<K, E> edge : node.getEdges()) {
                Node<K, V, E> toNode = getNode(edge.getTo());
                //noinspection ConstantConditions
                newGraph.addNodesEdge(node.getKey(), node.getValue(),
                                      toNode.getKey(), toNode.getValue(),
                                      edge.getLabel());
            }
        }

        return newGraph;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Graph<?, ?, ?> graph = (Graph<?, ?, ?>) o;
        return directed == graph.directed && nodes.equals(graph.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, directed);
    }

    public String toString(){
        StringBuilder r = new StringBuilder();

        for (Edge<K,E> edge: getEdges()) {
            r.append("(").append(edge.getFrom()).append(", ").append(edge.getTo()).append(")   ");
        }

        return r.toString();
    }
}
