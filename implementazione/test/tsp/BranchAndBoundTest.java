package tsp;

import graph.structures.Graph;
import org.junit.jupiter.api.Test;

public class BranchAndBoundTest {

    @Test
    public void esempioPDFGrosso() throws UnsolvableProblemException {
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
        TSPResult cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }

    @Test
    public void esempioProvaEsame() throws UnsolvableProblemException {
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
        TSPResult cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }

    @Test
    public void esempioGeeksForGeeks() throws UnsolvableProblemException {
        Graph<Integer, Integer, Integer> graph = new Graph<>(false);

        graph.addNodesEdge(1, 2, 10);
        graph.addNodesEdge(1, 3, 15);
        graph.addNodesEdge(1, 4, 20);
        graph.addNodesEdge(2, 3, 35);
        graph.addNodesEdge(2, 4, 25);
        graph.addNodesEdge(3, 4, 30);

        BranchAndBound bnb = new BranchAndBound(graph, 1);
        long time1 = System.currentTimeMillis();
        TSPResult cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }

    @Test
    public void esempioEserciziMoodle() throws UnsolvableProblemException {
        Graph<Integer, Integer, Integer> graph = new Graph<>(false);

        graph.addNodesEdge(1, 2, 5);
        graph.addNodesEdge(1, 3, 4);
        graph.addNodesEdge(1, 4, 7);
        graph.addNodesEdge(1, 5, 2);
        graph.addNodesEdge(1, 6, 6);
        graph.addNodesEdge(2, 3, 8);
        graph.addNodesEdge(2, 4, 9);
        graph.addNodesEdge(2, 5, 2);
        graph.addNodesEdge(2, 6, 2);
        graph.addNodesEdge(3, 4, 4);
        graph.addNodesEdge(3, 5, 4);
        graph.addNodesEdge(3, 6, 6);
        graph.addNodesEdge(4, 5, 5);
        graph.addNodesEdge(4, 6, 8);
        graph.addNodesEdge(5, 6, 7);

        BranchAndBound bnb = new BranchAndBound(graph, 1);
        long time1 = System.currentTimeMillis();
        TSPResult cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }

    @Test
    public void esempioUniversitarioOnline() throws UnsolvableProblemException {
        Graph<Integer, Integer, Integer> graph = new Graph<>(false);

        graph.addNodesEdge(1, 2, 12);
        graph.addNodesEdge(1, 3, 10);
        graph.addNodesEdge(1, 4, 19);
        graph.addNodesEdge(1, 5, 8);
        graph.addNodesEdge(2, 3, 3);
        graph.addNodesEdge(2, 4, 7);
        graph.addNodesEdge(2, 5, 2);
        graph.addNodesEdge(3, 4, 6);
        graph.addNodesEdge(3, 5, 20);
        graph.addNodesEdge(4, 5, 4);

        BranchAndBound bnb = new BranchAndBound(graph, 1);
        long time1 = System.currentTimeMillis();
        TSPResult cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }
    @Test
    public void esempioPolitecnicoOnline() throws UnsolvableProblemException {
        Graph<Integer, Integer, Integer> graph = new Graph<>(false);

        graph.addNodesEdge(1, 2, 86);
        graph.addNodesEdge(1, 3, 49);
        graph.addNodesEdge(1, 4, 57);
        graph.addNodesEdge(1, 5, 31);
        graph.addNodesEdge(1, 6, 69);
        graph.addNodesEdge(1, 7, 50);
        graph.addNodesEdge(2, 3, 68);
        graph.addNodesEdge(2, 4, 79);
        graph.addNodesEdge(2, 5, 93);
        graph.addNodesEdge(2, 6, 24);
        graph.addNodesEdge(2, 7, 5);
        graph.addNodesEdge(3, 4, 16);
        graph.addNodesEdge(3, 5, 7);
        graph.addNodesEdge(3, 6, 72);
        graph.addNodesEdge(3, 7, 67);
        graph.addNodesEdge(4, 5, 90);
        graph.addNodesEdge(4, 6, 69);
        graph.addNodesEdge(4, 7, 1);
        graph.addNodesEdge(5, 6, 86);
        graph.addNodesEdge(5, 7, 59);
        graph.addNodesEdge(6, 7, 81);

        BranchAndBound bnb = new BranchAndBound(graph, 1);
        long time1 = System.currentTimeMillis();
        TSPResult cycle = bnb.solveProblem();
        long time = System.currentTimeMillis()-time1;

        System.out.printf("Costo: %d\n", cycle.getCost());
        System.out.printf("Percorso: %s\n", cycle.getGraph().getEdges().toString());

        System.out.println("Tempo d'esecuzione: " + time + " millisecondi" );
    }
}
