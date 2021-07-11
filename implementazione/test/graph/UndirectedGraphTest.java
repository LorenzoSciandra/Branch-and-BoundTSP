package graph;

import graph.exceptions.GraphEdgeMissingException;
import graph.exceptions.GraphNodeMissingException;
import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UndirectedGraphTest {
    Graph<Integer, Integer, Character> graph;

    @BeforeEach
    @Test
    public void init() {
        graph = new Graph<>(false);

        assertEquals(0, graph.nodeCount());
        assertEquals(0, graph.edgeCount());

        graph.addNodesEdge(1, 1, 2, 2, 'a');
        graph.addNodesEdge(2, 2, 3, 3, 'b');
        graph.addNodesEdge(2, 2, 4, 4, 'c');
    }

    @Test
    public void isDirected() {
        assertFalse(graph.isDirected());
    }

    @Test
    public void nodeCount() {
        assertEquals(4, graph.nodeCount());
        graph.addNode(5, 5);
        assertEquals(5, graph.nodeCount());
    }

    @Test
    public void edgeCount() {
        assertEquals(3, graph.edgeCount());
        graph.addNodesEdge(2, 2, 5, 5, 'd');
        assertEquals(4, graph.edgeCount());
    }

    @Test
    public void addNode() {
        graph.addNode(5, 5);
        assertEquals(5, graph.nodeCount());
        assertEquals(5, graph.getNode(5).getValue());

        assertThrows(IllegalArgumentException.class, () -> graph.addNode(null, null));
        assertEquals(5, graph.nodeCount());

        graph.addNode(6, null);
        assertEquals(6, graph.nodeCount());

        assertThrows(IllegalArgumentException.class, () -> graph.addNode(null, 6));
        assertEquals(6, graph.nodeCount());
    }

    @Test
    public void getNode() {
        assertEquals(1, graph.getNode(1).getValue());
        assertNull(graph.getNode(5));
        assertNull(graph.getNode(null));
    }

    @Test
    public void containsNode() {
        assertTrue(graph.containsNode(1));
        assertFalse(graph.containsNode(5));
        assertFalse(graph.containsNode(null));
    }

    @Test
    public void removeNode() {
        graph.addNode(5, 5);
        assertEquals(5, graph.nodeCount());

        graph.removeNode(5);
        assertNull(graph.getNode(5));
        assertEquals(4, graph.nodeCount());

        graph.removeNode(2);
        assertEquals(3, graph.nodeCount());
        assertEquals(0, graph.edgeCount());

        assertEquals(0, graph.getNode(1).edgesCount());
        assertEquals(0, graph.getNode(3).edgesCount());
        assertEquals(0, graph.getNode(4).edgesCount());
    }

    @Test
    public void removeNodeMissing() {
        graph.removeNode(5);
        assertEquals(4, graph.nodeCount());
    }

    @Test
    public void removeNodeNull() {
        graph.removeNode(null);
        assertEquals(4, graph.nodeCount());
    }

    @Test
    public void addEdge() throws GraphNodeMissingException {
        graph.addNode(5, 5);
        graph.addEdge(5, 2, 'd');
        assertEquals('d', graph.getEdge(5, 2).getLabel());
        assertEquals('d', graph.getEdge(2, 5).getLabel());

        assertEquals(4, graph.edgeCount());
    }

    @Test
    public void addEdgeNonExisting() {
        assertThrows(GraphNodeMissingException.class, () -> graph.addEdge(5, 1, 'd'));
    }

    @Test
    public void addEdgeFromNonExistingNode() {
        assertThrows(GraphNodeMissingException.class, () -> graph.addEdge(5, 1, 'd'));
    }

    @Test
    public void addEdgeToNonExistingNode() {
        assertThrows(GraphNodeMissingException.class, () -> graph.addEdge(1, 5, 'd'));
    }

    @Test
    public void addEdgeForNonExistingNodes() {
        assertThrows(GraphNodeMissingException.class, () -> graph.addEdge(5, 6, 'd'));
        assertThrows(GraphNodeMissingException.class, () -> graph.addEdge(6, 5, 'd'));
    }

    @Test
    public void addNodesEdge() {
        graph.addNodesEdge(5, 5, 6, 6, 'd');
        assertTrue(graph.containsNode(5));
        assertTrue(graph.containsNode(6));
        assertEquals(5, graph.getNode(5).getValue());
        assertEquals(6, graph.getNode(6).getValue());
        assertEquals('d', graph.getEdge(5, 6).getLabel());
        assertEquals('d', graph.getEdge(6, 5).getLabel());
        assertEquals(4, graph.edgeCount());

        graph.addNodesEdge(2, 2, 7, 7, 'e');
        assertEquals(7, graph.nodeCount());
        assertEquals(5, graph.edgeCount());
        assertEquals('e', graph.getEdge(2, 7).getLabel());
        assertEquals('e', graph.getEdge(7, 2).getLabel());

        graph.addNodesEdge(1, 1, 3, 3, 'f');
        assertEquals(7, graph.nodeCount());
        assertEquals(6, graph.edgeCount());
        assertEquals('f', graph.getEdge(1, 3).getLabel());
        assertEquals('f', graph.getEdge(3, 1).getLabel());

        assertThrows(IllegalArgumentException.class, () -> graph.addNodesEdge(1, 1, null, null, 'n'));
        assertEquals(7, graph.nodeCount());
        assertEquals(6, graph.edgeCount());

        assertThrows(IllegalArgumentException.class, () -> graph.addNodesEdge(null, null, 2, 2, 'n'));
        assertEquals(7, graph.nodeCount());
        assertEquals(6, graph.edgeCount());

        assertThrows(IllegalArgumentException.class, () -> graph.addNodesEdge(null, null, null, null, 'n'));
        assertEquals(7, graph.nodeCount());
        assertEquals(6, graph.edgeCount());
    }

    @Test
    public void getEdge() {
        assertEquals('a', graph.getEdge(1, 2).getLabel());
        assertEquals('a', graph.getEdge(2, 1).getLabel());
        assertNull(graph.getEdge(1, 3));
        assertNull(graph.getEdge(null, 2));
        assertNull(graph.getEdge(1, null));
        assertNull(graph.getEdge(null, null));
    }

    @Test
    public void getEdges() {
        List<Edge<Integer, Character>> edges = graph.getEdges();
        assertEquals(6, edges.size());
    }

    @Test
    public void getAdjacentNodes() {
        assertEquals(3, graph.getAdjacentNodes(2).size());
    }

    @Test
    public void containsEdge() {
        assertTrue(graph.containsEdge(1, 2));
        assertTrue(graph.containsEdge(2, 1));
        assertFalse(graph.containsEdge(1, 3));
        assertFalse(graph.containsEdge(3, 1));
        assertFalse(graph.containsEdge(null, 2));
        assertFalse(graph.containsEdge(2, null));
    }

    @Test
    public void updateEdge() throws GraphNodeMissingException, GraphEdgeMissingException {
        assertEquals('a', graph.getEdge(1, 2).getLabel());
        assertEquals('a', graph.getEdge(2, 1).getLabel());

        graph.updateEdge(1, 2, '1');
        assertEquals('1', graph.getEdge(1, 2).getLabel());
        assertEquals('1', graph.getEdge(2, 1).getLabel());

        graph.updateEdge(2, 1, '2');
        assertEquals('2', graph.getEdge(1, 2).getLabel());
        assertEquals('2', graph.getEdge(2, 1).getLabel());
    }

    @Test
    public void updateEdgeNotExisting() {
        assertFalse(graph.containsEdge(3, 1));
        assertThrows(GraphEdgeMissingException.class, () -> graph.updateEdge(3, 1, 'c'));
    }

    @Test
    public void updateNonExistingEdge() {
        assertThrows(GraphNodeMissingException.class, () -> graph.updateEdge(1, 5, 'c'));
        assertThrows(GraphNodeMissingException.class, () -> graph.updateEdge(5, 2, 'c'));
        assertThrows(GraphNodeMissingException.class, () -> graph.updateEdge(5, 6, 'c'));
        assertThrows(GraphNodeMissingException.class, () -> graph.updateEdge(6, 5, 'c'));
    }

    @Test
    public void removeEdge() throws GraphNodeMissingException {
        assertEquals(3, graph.edgeCount());
        graph.removeEdge(2, 3);
        assertEquals(2, graph.edgeCount());
    }

    @Test
    public void removeEdgeMissingFrom() {
        assertEquals(3, graph.edgeCount());
        assertThrows(GraphNodeMissingException.class, () -> graph.removeEdge(5, 1));
    }

    @Test
    public void removeEdgeMissing() {
        assertEquals(3, graph.edgeCount());
        assertDoesNotThrow(() -> graph.removeEdge(3, 1));
        assertEquals(3, graph.edgeCount());
    }

    @Test
    public void removeEdgeNullValue() {
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge(null, 2));
    }

    @Test
    public void removeEdgeValueNull() {
        assertEquals(3, graph.edgeCount());
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge(2, null));
        assertEquals(3, graph.edgeCount());
    }

    @Test
    void getNodes() {
        ArrayList<Node<Integer, Integer, Character>> nodes = graph.getNodes();
        assertEquals(4, nodes.size());
    }

    @Test
    void cloneTest() {
        Graph<Integer, Integer, Character> clonedGraph = graph.clone();

        assertNotSame(clonedGraph, graph);

        for (Node<Integer, Integer, Character> originalNode : graph.getNodes()) {
            Node<Integer, Integer, Character> cloneNode = clonedGraph.getNode(originalNode.getKey());

            assertNotSame(originalNode, cloneNode);
            assertEquals(originalNode, cloneNode);
        }

        for (Edge<Integer, Character> edge : graph.getEdges()) {
            Edge<Integer, Character> clonedEdge = clonedGraph.getEdge(edge.getFrom(), edge.getTo());

            assertNotSame(edge, clonedEdge);
            assertEquals(edge, clonedEdge);
        }

    }

    @Test
    void testEquals() {
        Graph<Integer, Integer, Character> graph2 = new Graph<>(false);

        graph2.addNodesEdge(1, 1, 2, 2, 'a');
        graph2.addNodesEdge(2, 2, 3, 3, 'b');
        graph2.addNodesEdge(2, 2, 4, 4, 'c');

        assertEquals(graph, graph2);
    }
}
