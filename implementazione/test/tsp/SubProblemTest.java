package tsp;

import graph.MinimumSpanningTree;
import graph.structures.Edge;
import graph.structures.Graph;
import mst.MinimumSpanningTreeTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static graph.MinimumSpanningTree.kruskalAlgorithm;
import static org.junit.jupiter.api.Assertions.*;

public class SubProblemTest {
    private static @NotNull Graph<Integer, Integer, Integer> initGraph() {
        Graph<Integer, Integer, Integer> graph = new Graph<>(false);

        graph.addNodesEdge(1, 2, 5);
        graph.addNodesEdge(1, 3, 2);
        graph.addNodesEdge(1, 4, 3);
        graph.addNodesEdge(1, 5, 8);
        graph.addNodesEdge(2, 3, 6);
        graph.addNodesEdge(2, 4, 7);
        graph.addNodesEdge(2, 5, 10);
        graph.addNodesEdge(3, 4, 8);
        graph.addNodesEdge(3, 5, 2);
        graph.addNodesEdge(4, 5, 1);

        return graph;
    }

    @Test
    public void unoTreeNodoRadiceTest() throws Exception {
        SubProblem radice = new SubProblem(initGraph(),new ArrayList<>(),new ArrayList<>(),1);

        Graph<Integer, Integer, Integer> unoTreeRadice  = radice.getOneTree();

        assertTrue(unoTreeRadice.containsEdge(1, 3));
        assertTrue(unoTreeRadice.containsEdge(2, 3));
        assertTrue(unoTreeRadice.containsEdge(1, 4));
        assertTrue(unoTreeRadice.containsEdge(4, 5));
        assertTrue(unoTreeRadice.containsEdge(3, 5));

        assertEquals(radice.getLowerBound(),14);
        assertFalse(radice.containsHamiltonianCycle());
        assertTrue(radice.isFeasible());
    }

    @Test
    public void unoTreeNodo2Test() throws Exception {
        ArrayList<Edge<Integer,Integer>> mandatoryEdges = new ArrayList<>();
        ArrayList<Edge<Integer,Integer>> forbiddenEdges = new ArrayList<>();

        forbiddenEdges.add(new Edge<>(1,3,2));

        SubProblem nodo2 = new SubProblem(initGraph(), mandatoryEdges, forbiddenEdges,1);

        Graph<Integer, Integer, Integer> unoTreeNodo2  = nodo2.getOneTree();

        assertTrue(unoTreeNodo2.containsEdge(1, 2));
        assertTrue(unoTreeNodo2.containsEdge(2, 3));
        assertTrue(unoTreeNodo2.containsEdge(1, 4));
        assertTrue(unoTreeNodo2.containsEdge(4, 5));
        assertTrue(unoTreeNodo2.containsEdge(3, 5));

        assertEquals(nodo2.getLowerBound(),17);
        assertTrue(nodo2.containsHamiltonianCycle());
        assertTrue(nodo2.isFeasible());
    }

    @Test
    public void unoTreeNodo3Test() throws Exception {
        ArrayList<Edge<Integer,Integer>> mandatoryEdges = new ArrayList<>();
        ArrayList<Edge<Integer,Integer>> forbiddenEdges = new ArrayList<>();

        mandatoryEdges.add(new Edge<>(1,3,2));
        forbiddenEdges.add(new Edge<>(3,5,2));

        SubProblem nodo3 = new SubProblem(initGraph(), mandatoryEdges, forbiddenEdges,1);

        Graph<Integer, Integer, Integer> unoTreeNodo3  = nodo3.getOneTree();

        assertTrue(unoTreeNodo3.containsEdge(1, 3));
        assertTrue(unoTreeNodo3.containsEdge(2, 3));
        assertTrue(unoTreeNodo3.containsEdge(1, 4));
        assertTrue(unoTreeNodo3.containsEdge(4, 5));
        assertTrue(unoTreeNodo3.containsEdge(2, 4));

        assertEquals(nodo3.getLowerBound(),19);
        assertFalse(nodo3.containsHamiltonianCycle());
        assertTrue(nodo3.isFeasible());
    }

    @Test
    public void unoTreeNodo4Test() throws Exception {
        ArrayList<Edge<Integer,Integer>> mandatoryEdges = new ArrayList<>();
        ArrayList<Edge<Integer,Integer>> forbiddenEdges = new ArrayList<>();

        mandatoryEdges.add(new Edge<>(1,3,2));
        mandatoryEdges.add(new Edge<>(3,5,2));
        forbiddenEdges.add(new Edge<>(5,4,1));

        SubProblem nodo4 = new SubProblem(initGraph(), mandatoryEdges, forbiddenEdges,1);

        Graph<Integer, Integer, Integer> unoTreeNodo4  = nodo4.getOneTree();


        assertTrue(unoTreeNodo4.containsEdge(1, 3));
        assertTrue(unoTreeNodo4.containsEdge(2, 3));
        assertTrue(unoTreeNodo4.containsEdge(1, 4));
        assertTrue(unoTreeNodo4.containsEdge(3, 5));
        assertTrue(unoTreeNodo4.containsEdge(2, 4));

        assertEquals(nodo4.getLowerBound(),20);
        assertFalse(nodo4.containsHamiltonianCycle());
        assertTrue(nodo4.isFeasible());
    }

    @Test
    public void unoTreeNodo5Test() throws Exception {
        ArrayList<Edge<Integer,Integer>> mandatoryEdges = new ArrayList<>();
        ArrayList<Edge<Integer,Integer>> forbiddenEdges = new ArrayList<>();

        mandatoryEdges.add(new Edge<>(1,3,2));
        mandatoryEdges.add(new Edge<>(3,5,2));
        mandatoryEdges.add(new Edge<>(5,4,1));
        forbiddenEdges.add(new Edge<>(1,4,3));

        SubProblem nodo5 = new SubProblem(initGraph(), mandatoryEdges, forbiddenEdges,1);

        Graph<Integer, Integer, Integer> unoTreeNodo5  = nodo5.getOneTree();


        assertTrue(unoTreeNodo5.containsEdge(1, 3));
        assertTrue(unoTreeNodo5.containsEdge(2, 3));
        assertTrue(unoTreeNodo5.containsEdge(1, 2));
        assertTrue(unoTreeNodo5.containsEdge(4, 5));
        assertTrue(unoTreeNodo5.containsEdge(3, 5));

        assertEquals(nodo5.getLowerBound(),16);
        assertFalse(nodo5.containsHamiltonianCycle());
        assertTrue(nodo5.isFeasible());
    }

    @Test
    public void unoTreeNodo6Test() throws Exception {
        ArrayList<Edge<Integer,Integer>> mandatoryEdges = new ArrayList<>();
        ArrayList<Edge<Integer,Integer>> forbiddenEdges = new ArrayList<>();

        mandatoryEdges.add(new Edge<>(1,3,2));
        mandatoryEdges.add(new Edge<>(3,5,2));
        mandatoryEdges.add(new Edge<>(5,4,1));
        forbiddenEdges.add(new Edge<>(1,4,3));
        forbiddenEdges.add(new Edge<>(1,2,5));

        SubProblem nodo6 = new SubProblem(initGraph(), mandatoryEdges, forbiddenEdges,1);

        Graph<Integer, Integer, Integer> unoTreeNodo6  = nodo6.getOneTree();


        assertTrue(unoTreeNodo6.containsEdge(1, 3));
        assertTrue(unoTreeNodo6.containsEdge(2, 3));
        assertTrue(unoTreeNodo6.containsEdge(1, 5));
        assertTrue(unoTreeNodo6.containsEdge(4, 5));
        assertTrue(unoTreeNodo6.containsEdge(3, 5));

        assertEquals(nodo6.getLowerBound(),19);
        assertFalse(nodo6.containsHamiltonianCycle());
        assertTrue(nodo6.isFeasible());
    }
    @Test
    public void unoTreeNodo7Test() throws Exception {
        ArrayList<Edge<Integer,Integer>> mandatoryEdges = new ArrayList<>();
        ArrayList<Edge<Integer,Integer>> forbiddenEdges = new ArrayList<>();

        mandatoryEdges.add(new Edge<>(1,3,2));
        mandatoryEdges.add(new Edge<>(3,5,2));
        mandatoryEdges.add(new Edge<>(5,4,1));
        mandatoryEdges.add(new Edge<>(1,2,5));
        forbiddenEdges.add(new Edge<>(1,4,3));
        forbiddenEdges.add(new Edge<>(2,3,6));

        SubProblem nodo6 = new SubProblem(initGraph(), mandatoryEdges, forbiddenEdges,1);

        Graph<Integer, Integer, Integer> unoTreeNodo6  = nodo6.getOneTree();


        assertTrue(unoTreeNodo6.containsEdge(1, 3));
        assertTrue(unoTreeNodo6.containsEdge(2, 4));
        assertTrue(unoTreeNodo6.containsEdge(1, 2));
        assertTrue(unoTreeNodo6.containsEdge(4, 5));
        assertTrue(unoTreeNodo6.containsEdge(3, 5));

        assertEquals(nodo6.getLowerBound(),17);
        assertTrue(nodo6.containsHamiltonianCycle());
        assertTrue(nodo6.isFeasible());
    }

}
