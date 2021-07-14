package tsp;

import graph.structures.Graph;
import org.junit.jupiter.api.Test;

public class BranchAndBoundTest {

    @Test
    public void esempioPDFGrosso() throws Exception {
        Graph<Integer, Integer, Integer> graph = new Graph<>(false);
        graph.addNodesEdge(1, 2, 5);
        graph.addNodesEdge(1, 3, 8);
        graph.addNodesEdge(1, 4, 3);
        graph.addNodesEdge(1, 5, 5);
        graph.addNodesEdge(2, 3, 4);
        graph.addNodesEdge(2, 4, 6);
        graph.addNodesEdge(2, 5, 2);
        graph.addNodesEdge(3, 4, 10);
        graph.addNodesEdge(3, 5, 3);
        graph.addNodesEdge(4, 5, 1);

        BranchAndBound bnb = new BranchAndBound(graph, 1);
        long time1 = System.currentTimeMillis();
        HamiltonianCycle cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }

    @Test
    public void esempioProvaEsame() throws Exception {
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

        BranchAndBound bnb = new BranchAndBound(graph, 1);
        long time1 = System.currentTimeMillis();
        HamiltonianCycle cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }
}
