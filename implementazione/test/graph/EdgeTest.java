package graph;

import graph.structures.Edge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeTest {
    private Edge<Integer, Character> edge;

    @BeforeEach
    public void setUp() {
        edge = new Edge<>(1, 2, 'a');
    }

    @Test
    public void getFrom() {
        assertEquals(1, edge.getFrom().intValue());
    }

    @Test
    public void getTo() {
        assertEquals(2, edge.getTo().intValue());
    }

    @Test
    public void getLabel() {
        assertEquals('a', edge.getLabel().charValue());
    }

    @Test
    void inverse() {
        Edge<Integer, Character> inverse = edge.inverse();
        assertEquals('a', inverse.getLabel().charValue());
        assertEquals(2, inverse.getFrom().intValue());
        assertEquals(1, inverse.getTo().intValue());

    }

    @Test
    public void toString1() {
        assertEquals("Edge{from=1, to=2, label=a}", edge.toString());
    }

    @Test
    void testEquals() {
        assertEquals(edge, new Edge<>(1, 2, 'a'));
        assertNotEquals(edge, new Edge<>(1, 2, 'b'));
    }
}