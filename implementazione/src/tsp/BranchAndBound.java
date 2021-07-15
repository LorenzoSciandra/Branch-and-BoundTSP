package tsp;

import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class BranchAndBound {
    // todo convert to priority queue
    public PriorityQueue<SubProblem> subProblemQueue;
    public Graph<Integer, Integer, Integer> graph;
    private Integer candidateNode;

    public BranchAndBound(Graph<Integer, Integer, Integer> graph, Integer candidateNode) {
        this.graph = graph.clone();
        this.subProblemQueue = new PriorityQueue<>();
        this.candidateNode = candidateNode;
    }

    public HamiltonianCycle solveProblem() throws UnsolvableProblemException {
        return solveProblem(false);
    }

    public HamiltonianCycle solveProblem(boolean ignoreOneWayNodes) throws UnsolvableProblemException {
        List<Integer> oneWayNodesKeys = graph.getNodes()
                                             .stream()
                                             .filter(node -> node.getDegree() < 2)
                                             .map(Node::getKey)
                                             .collect(Collectors.toUnmodifiableList());
        if (oneWayNodesKeys.size() > 0) {
            if (ignoreOneWayNodes) {
                oneWayNodesKeys.forEach(graph::removeNode);
                System.out.printf("Removing %d nodes.\n", oneWayNodesKeys.size());
            } else {
                throw new UnsolvableProblemException(oneWayNodesKeys);
            }
        }

        SubProblem rootProblem = new SubProblem(graph, new ArrayList<>(), new ArrayList<>(), candidateNode);
        subProblemQueue.add(rootProblem);
        HamiltonianCycle minHamiltonianCycle = new HamiltonianCycle(graph, Integer.MAX_VALUE);

        while (!subProblemQueue.isEmpty()) {

            SubProblem currentProblem = subProblemQueue.remove();

/*            System.out.println("Nodo corrente: " + currentProblem.getOneTree().toString());
            System.out.println("Costo: " + currentProblem.getLowerBound() + " ciclo: " + currentProblem
            .containsHamiltonianCycle() + " ammissibile: " + currentProblem.isFeasible());
            System.out.println("Archi forzati: " + currentProblem.getMandatoryEdges().toString());
            System.out.println("Archi vietati: " + currentProblem.getForbiddenEdges().toString());
            System.out.println("\n\n");

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            if (currentProblem.isFeasible()) {
                if (currentProblem.containsHamiltonianCycle()) {
                    if (minHamiltonianCycle.getCost() > currentProblem.getLowerBound()) {
                        // found better solution! Closing because candidate solution.
                        minHamiltonianCycle.newSolutionFound(currentProblem.getOneTree(),
                                                             currentProblem.getLowerBound());
                    }
                } else if (currentProblem.getLowerBound() < minHamiltonianCycle.getCost()) {
                    branch(currentProblem);
                }
            }
            // else closed because unfeasible
        }

        minHamiltonianCycle.finalizeSolution();

        return minHamiltonianCycle;
    }

    private void branch(SubProblem currentProblem) {

        HashMap<Integer, Integer> parentsVector = new HashMap<>();
        dfs(currentProblem.getOneTree().getNode(candidateNode), parentsVector, currentProblem.getOneTree());

        ArrayList<Edge<Integer, Integer>> subCycle = new ArrayList<>();

        int toNode = candidateNode;
        int fromNode = Integer.MAX_VALUE;

        //System.out.println("Sottociclo: ");

        while (fromNode != candidateNode) {
            fromNode = parentsVector.get(toNode);
            subCycle.add(currentProblem.getOneTree().getEdge(fromNode, toNode));
            //System.out.print("(" + fromNode + ", " + toNode + ") ");
            toNode = fromNode;
        }

        //System.out.println("\n");


        ArrayList<Edge<Integer, Integer>> mandatoryEdges = currentProblem.getMandatoryEdges();
        ArrayList<Edge<Integer, Integer>> forbiddenEdges = currentProblem.getForbiddenEdges();

        for (Edge<Integer, Integer> integerIntegerEdge : subCycle) {
            if (!(currentProblem.getMandatoryEdges().contains(integerIntegerEdge) ||
                  currentProblem.getMandatoryEdges().contains(integerIntegerEdge.inverse()))
            ) {
                forbiddenEdges.add(integerIntegerEdge);
                //System.out.println("Arco vietato: " + forbiddenEdges + "\n");
                SubProblem sp = new SubProblem(graph,
                                               new ArrayList<>(mandatoryEdges),
                                               new ArrayList<>(forbiddenEdges),
                                               candidateNode);
                subProblemQueue.add(sp);
                /*
                System.out.println("Figlio generato: " + sp.getOneTree().toString());
                System.out.println("Costo: " + sp.getLowerBound()
                 + " ciclo: " + sp.containsHamiltonianCycle());
                System.out.println("Archi forzati: " + sp.getMandatoryEdges().toString());
                System.out.println("Archi vietati: " + sp.getForbiddenEdges().toString());
                System.out.println("\n\n");*/

                forbiddenEdges.remove(integerIntegerEdge);
                mandatoryEdges.add(integerIntegerEdge);
            }
        }
    }

    private void dfs(@NotNull Node<Integer, Integer, Integer> nodoCorrente,
                     HashMap<Integer, Integer> vettorePadri,
                     Graph<Integer, Integer, Integer> grafo) {
        for (Edge<Integer, Integer> arcoUscente : nodoCorrente.getEdges()) {
            if (!vettorePadri.containsKey(arcoUscente.getTo())) {
                if (!vettorePadri.containsKey(nodoCorrente.getKey()) ||
                    !vettorePadri.get(nodoCorrente.getKey()).equals(arcoUscente.getTo())) {

                    vettorePadri.put(arcoUscente.getTo(), nodoCorrente.getKey());
                    dfs(grafo.getNode(arcoUscente.getTo()), vettorePadri, grafo);
                }
            }
        }
    }
}
