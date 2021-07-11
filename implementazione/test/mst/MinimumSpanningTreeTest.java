package mst;

import graph.MinimumSpanningTree;
import graph.structures.Edge;
import graph.structures.Graph;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static graph.MinimumSpanningTree.kruskalAlgorithm;
import static org.junit.jupiter.api.Assertions.*;

public class MinimumSpanningTreeTest {

    private static @NotNull Graph<String, String, Integer> initGraph() {
        Graph<String, String, Integer> graph = new Graph<>(false);

        graph.addNodesEdge("A", "B", 7);
        graph.addNodesEdge("A", "D", 5);
        graph.addNodesEdge("B", "D", 9);
        graph.addNodesEdge("B", "C", 8);
        graph.addNodesEdge("B", "E", 7);
        graph.addNodesEdge("C", "E", 5);
        graph.addNodesEdge("D", "E", 15);
        graph.addNodesEdge("D", "F", 6);
        graph.addNodesEdge("F", "E", 8);
        graph.addNodesEdge("F", "G", 11);
        graph.addNodesEdge("G", "E", 9);

        return graph;
    }

    @Test
    public void kruskalAlgorithmLabelIntegerTest() throws Exception {
        Graph<String, String, Integer> graph = new Graph<>(false);
        Graph<String, String, Integer> mst;

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");

        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 3);
        graph.addEdge("A", "D", 5);
        graph.addEdge("B", "C", 2);
        graph.addEdge("B", "D", 3);
        graph.addEdge("C", "D", 4);

        mst = kruskalAlgorithm(graph, new ComparatorEdgeStringInteger());

        //check
        assertNotNull(mst.getEdge("A", "B"));
        assertNotNull(mst.getEdge("B", "D"));
        assertNotNull(mst.getEdge("B", "C"));

        assertNull(mst.getEdge("A", "C"));
        assertNull(mst.getEdge("A", "D"));
        assertNull(mst.getEdge("C", "D"));

        assertEquals(1, mst.getEdge("A", "B").getLabel());
        assertEquals(3, mst.getEdge("B", "D").getLabel());
        assertEquals(2, mst.getEdge("B", "C").getLabel());

        assertEquals(4, mst.nodeCount());
        assertEquals(3, mst.edgeCount());

        //not directed
        assertNull(mst.getEdge("C", "A"));
        assertNull(mst.getEdge("D", "A"));
        assertNull(mst.getEdge("D", "C"));
    }

    @Test
    public void kruskalAlgorithmLabelFloatTest() throws Exception {
        Graph<String, String, Float> graph = new Graph<>(false);
        Graph<String, String, Float> mst;

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");

        graph.addEdge("A", "B", Float.parseFloat("1.0"));
        graph.addEdge("A", "C", Float.parseFloat("3.1"));
        graph.addEdge("A", "D", Float.parseFloat("5.2"));
        graph.addEdge("B", "C", Float.parseFloat("2.3"));
        graph.addEdge("B", "D", Float.parseFloat("3.1"));
        graph.addEdge("C", "D", Float.parseFloat("4.4"));

        mst = kruskalAlgorithm(graph, new ComparatorEdgeStringFloat());

        //check
        assertNotNull(mst.getEdge("A", "B"));
        assertNotNull(mst.getEdge("B", "D"));
        assertNotNull(mst.getEdge("B", "C"));

        assertNull(mst.getEdge("A", "C"));
        assertNull(mst.getEdge("A", "D"));
        assertNull(mst.getEdge("C", "D"));

        assertEquals((float) 1.0, mst.getEdge("A", "B").getLabel());
        assertEquals((float) 3.1, mst.getEdge("B", "D").getLabel());
        assertEquals((float) 2.3, mst.getEdge("B", "C").getLabel());

        assertEquals(4, mst.nodeCount());
        assertEquals(3, mst.edgeCount());

        //not directed
        assertNull(mst.getEdge("C", "A"));
        assertNull(mst.getEdge("D", "A"));
        assertNull(mst.getEdge("D", "C"));
    }

    @Test
    public void kruskalAlgorithmLabelDoubleTest() throws Exception {
        Graph<String, String, Double> graph = new Graph<>(false);
        Graph<String, String, Double> mst;

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");

        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 3.2);
        graph.addEdge("A", "D", 5.1);
        graph.addEdge("B", "C", 2.3);
        graph.addEdge("B", "D", 3.2);
        graph.addEdge("C", "D", 4.2);

        mst = kruskalAlgorithm(graph, new ComparatorEdgeStringDouble());

        //check
        assertNotNull(mst.getEdge("A", "B"));
        assertNotNull(mst.getEdge("B", "D"));
        assertNotNull(mst.getEdge("B", "C"));

        assertNull(mst.getEdge("A", "C"));
        assertNull(mst.getEdge("A", "D"));
        assertNull(mst.getEdge("C", "D"));

        assertEquals(1.0, mst.getEdge("A", "B").getLabel());
        assertEquals(3.2, mst.getEdge("B", "D").getLabel());
        assertEquals(2.3, mst.getEdge("B", "C").getLabel());

        assertEquals(4, mst.nodeCount());
        assertEquals(3, mst.edgeCount());

        //not directed
        assertNull(mst.getEdge("C", "A"));
        assertNull(mst.getEdge("D", "A"));
        assertNull(mst.getEdge("D", "C"));
    }

    @Test
    void kruskalForTSP() {
        Graph<String, String, Integer> basicMST = MinimumSpanningTree.kruskalForTSP(initGraph(),
                                                                                    new ComparatorEdgeStringInteger(),
                                                                                    new ArrayList<>(0),
                                                                                    new ArrayList<>(0));
        assertTrue(basicMST.containsEdge("A", "B"));
        assertTrue(basicMST.containsEdge("A", "D"));
        assertTrue(basicMST.containsEdge("B", "E"));
        assertTrue(basicMST.containsEdge("D", "F"));
        assertTrue(basicMST.containsEdge("E", "C"));
        assertTrue(basicMST.containsEdge("E", "G"));

        assertEquals(MinimumSpanningTree.kruskalAlgorithm(initGraph(), new ComparatorEdgeStringInteger()), basicMST);

        Graph<String, String, Integer> mst2 = MinimumSpanningTree.kruskalForTSP(initGraph(),
                                                                                new ComparatorEdgeStringInteger(),
                                                                                List.of(new Edge<>("B", "C", 8)),
                                                                                new ArrayList<>(0));

        assertTrue(mst2.containsEdge("A", "B"));
        assertTrue(mst2.containsEdge("A", "D"));
        assertTrue(mst2.containsEdge("B", "C"));
        assertTrue(mst2.containsEdge("C", "E"));
        assertTrue(mst2.containsEdge("D", "F"));
        assertTrue(mst2.containsEdge("E", "G"));

        Graph<String, String, Integer> mst3 = MinimumSpanningTree.kruskalForTSP(initGraph(),
                                                                                new ComparatorEdgeStringInteger(),
                                                                                new ArrayList<>(0),
                                                                                List.of(new Edge<>("B", "E", 7)));

        assertFalse(mst3.containsEdge("B", "E"));
    }

    /**
     * Comparator used in sort
     * Every type needs a different comparator
     */
    private class ComparatorEdgeStringInteger implements Comparator<Edge<String, Integer>> {
        @Override
        public int compare(Edge<String, Integer> o1, Edge<String, Integer> o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    }

    private class ComparatorEdgeStringFloat implements Comparator<Edge<String, Float>> {
        @Override
        public int compare(Edge<String, Float> o1, Edge<String, Float> o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    }

    private class ComparatorEdgeStringDouble implements Comparator<Edge<String, Double>> {
        @Override
        public int compare(Edge<String, Double> o1, Edge<String, Double> o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    }
}
