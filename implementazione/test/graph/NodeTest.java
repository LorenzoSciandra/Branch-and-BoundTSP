package graph;

import graph.structures.Edge;
import graph.structures.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTest {
    private Node<Integer, Integer, Character> node;

    @BeforeEach
    public void setUp() {
        node = new Node<>(1, 1);
        node.addEdge(2, 'a');
    }

    @Test
    public void getKey() {
        assertEquals(1, node.getKey().intValue());
    }

    @Test
    public void getValue() {
        assertEquals(1, node.getValue().intValue());
    }

    @Test
    public void getLabel() {
        assertEquals('a', node.getLabel(2).charValue());
    }

    @Test
    public void hasEdge() {
        assertTrue(node.hasEdge(2));
    }

    @Test
    public void getEdge() {
        Edge<Integer, Character> edge = node.getEdge(2);
        assertNotNull(edge);
        assertEquals(1, edge.getFrom().intValue());
        assertEquals(2, edge.getTo().intValue());
        assertEquals('a', edge.getLabel().charValue());
    }

    @Test
    public void getEdges() {
        List<Edge<Integer, Character>> edgeList = node.getEdges();
        assertNotNull(edgeList);
        assertFalse(edgeList.isEmpty());
        Edge<Integer, Character> edge = edgeList.get(0);
        assertEquals(1, edge.getFrom().intValue());
        assertEquals(2, edge.getTo().intValue());
        assertEquals('a', edge.getLabel().charValue());
    }

    @Test
    public void getAdjacentNodes() {
        assertEquals(2, node.getAdjacentNodesKeys().get(0).intValue());
    }

    @Test
    public void edgesCount() {
        assertEquals(1, node.edgesCount());
    }

    @Test
    public void removeEdge() {
        assertEquals(1, node.edgesCount());
        node.removeEdge(2);
        assertEquals(0, node.edgesCount());
    }

    @Test
    public void addEdge() {
        assertEquals(1, node.edgesCount());
        node.addEdge(3, 'b');
        assertEquals(2, node.edgesCount());

        Edge<Integer, Character> edge = node.getEdge(3);
        assertNotNull(edge);
        assertEquals(1, edge.getFrom().intValue());
        assertEquals(3, edge.getTo().intValue());
        assertEquals('b', edge.getLabel().charValue());
    }

    @Test
    public void toString1() {
        assertEquals("Node{key=1, value=1, edges={2=a}}", node.toString());
    }
}