package graph.structures;

import graph.exceptions.GraphEdgeMissingException;
import graph.exceptions.GraphNodeMissingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Graph<K, V, E> implements Cloneable {

    private HashMap<K, Node<K, V, E>> nodes;

    private boolean directed;

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
        int edgesCounter = nodes.values().stream().mapToInt(Node::edgesCount).sum();
        return directed ? edgesCounter : edgesCounter / 2;
    }

    public void addNode(@NotNull K key) {
        addNode(key, null);
    }

    public void addNode(@NotNull K key, @Nullable V value) {
        if (key != null && !containsNode(key)) {
            nodes.put(key, new Node<>(key, value));
        }
    }

    public @Nullable
    Node<K, V, E> getNode(K key) {
        return nodes.get(key);
    }

    public boolean containsNode(K key) {
        return nodes.containsKey(key);
    }

    public ArrayList<Node<K, V, E>> getNodes() {
        return new ArrayList<>(this.nodes.values());
    }

    public void removeNode(K key) {
        for (Node<K, V, E> node : nodes.values()) {
            node.removeEdge(key);
        }

        nodes.remove(key);
    }

    @SuppressWarnings("Duplicates")
    public void addEdge(@NotNull K from, @NotNull K to, @Nullable E label) throws GraphNodeMissingException {
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
    }

    public void addNodesEdge(@NotNull K fromKey, @NotNull K toKey, @Nullable E label) {
        addNodesEdge(fromKey, null, toKey, null, label);
    }

    public void addNodesEdge(@NotNull K fromKey, @Nullable V fromValue, @NotNull K toKey, @Nullable V toValue,
                             @Nullable E label) {
        if (fromKey == null || toKey == null) {
            return;
        }

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
        ArrayList<Edge<K, E>> edges = new ArrayList<>();
        for (Node<K, V, E> node : nodes.values()) {
            edges.addAll(node.getEdges());
        }
        return edges;
    }

    public ArrayList<Node<K, V, E>> getAdjacentNodes(K key) {
        ArrayList<Node<K, V, E>> adjacentNodes = new ArrayList<>();
        nodes.get(key).getAdjacentNodesKeys().forEach(k -> adjacentNodes.add(nodes.get(k)));
        return adjacentNodes;
    }

    @SuppressWarnings("Duplicates")

    public void updateEdge(@NotNull K from, @NotNull K to, @Nullable E newLabel) throws GraphNodeMissingException,
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
    }

    public void removeEdge(@NotNull K from, @NotNull K to) throws GraphNodeMissingException {
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
    }

    @Override
    public Graph<K, V, E> clone() {
        Graph<K, V, E> newGraph = new Graph<>(this.directed);

        // copy all edges of all nodes.
        for (Node<K, V, E> node : getNodes()) {
            for (Edge<K, E> edge : node.getEdges()) {
                Node<K, V, E> toNode = getNode(edge.getTo());
                newGraph.addNodesEdge(node.getKey(), node.getValue(),
                                      toNode.getKey(), toNode.getValue(),
                                      edge.getLabel());
            }
        }

        return newGraph;
    }
}
