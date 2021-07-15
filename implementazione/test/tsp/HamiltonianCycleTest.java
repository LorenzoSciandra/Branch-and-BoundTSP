package tsp;

import graph.structures.Edge;
import graph.structures.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tsp.HamiltonianCycle.ResultState;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HamiltonianCycleTest {

    HamiltonianCycle tsp;
    Graph<Integer, Integer, Integer> dummy;

    @BeforeEach
    void setUp() {
        dummy = new Graph<>(false);
        dummy.addNodesEdge(1, 2, 1)
             .addNodesEdge(2, 3, 5)
             .addNodesEdge(3, 1, 7);

        tsp = new HamiltonianCycle(dummy, Integer.MAX_VALUE);
    }

    void updateGraphSolvable() {
        tsp.newSolutionFound(dummy, 13);
    }

    @Test
    void getGraph() {
        assertSame(dummy, tsp.getGraph());
    }

    @Test
    void getCostUnsolved() {
        assertEquals(ResultState.Unsolved, tsp.getState());
        assertEquals(Integer.MAX_VALUE, tsp.getCost());
    }

    @Test
    void getCost() {
        updateGraphSolvable();
        assertEquals(ResultState.Solvable, tsp.getState());
        assertEquals(13, tsp.getCost());
    }

    @Test
    void getStateSolvable() {
        assertEquals(ResultState.Unsolved, tsp.getState());
        updateGraphSolvable();
        assertEquals(ResultState.Solvable, tsp.getState());
        tsp.finalizeSolution();
        assertEquals(ResultState.Solved, tsp.getState());
    }

    @Test
    void getStateUnsolvable() {
        assertEquals(ResultState.Unsolved, tsp.getState());
        tsp.finalizeSolution();
        assertEquals(ResultState.Unsolvable, tsp.getState());
    }

    @Test
    void newSolutionFound() {
        Graph<Integer, Integer, Integer> dummy2 = new Graph<>(false);
        dummy2.addNodesEdge(1, 2, 1)
              .addNodesEdge(2, 3, 5)
              .addNodesEdge(3, 1, 6);

        assertEquals(ResultState.Unsolved, tsp.getState());
        assertNotSame(dummy2, tsp.getGraph());

        tsp.newSolutionFound(dummy2, 12);

        assertSame(dummy2, tsp.getGraph());
        assertEquals(12, tsp.getCost());
        assertEquals(ResultState.Solvable, tsp.getState());
    }

    @Test
    void getPath() {
        updateGraphSolvable();
        tsp.finalizeSolution();

        ArrayList<Edge<Integer, Integer>> path = tsp.getPath();
        List<Edge<Integer, Integer>> expectedPath = List.of(new Edge<>(1, 2, 1),
                                                            new Edge<>(2, 3, 5),
                                                            new Edge<>(3, 1, 7));

        assertEquals(expectedPath, path);
    }
}