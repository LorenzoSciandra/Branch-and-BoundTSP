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
    public PriorityQueue<SubProblem> subProblemQueue;
    public Graph<Integer, Integer, Integer> graph;
    private Integer candidateNode;

    public BranchAndBound(Graph<Integer, Integer, Integer> graph, Integer candidateNode) {
        this.graph = graph.clone();
        this.subProblemQueue = new PriorityQueue<>();
        this.candidateNode = candidateNode;
    }

    public TSPResult solveProblem() throws UnsolvableProblemException {
        return solveProblem(false);
    }

    public TSPResult solveProblem(boolean ignoreOneWayNodes) throws UnsolvableProblemException {
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

        TSPResult minTSPResult = new TSPResult(graph, Integer.MAX_VALUE);
        SubProblem rootProblem = new SubProblem(graph, new ArrayList<>(), new ArrayList<>(), candidateNode);
        subProblemQueue.add(rootProblem);
        minTSPResult.increaseNodeCount(1);

        while (!subProblemQueue.isEmpty()) {

            SubProblem currentProblem = subProblemQueue.remove();

            if (currentProblem.isFeasible()) {
                if (currentProblem.containsHamiltonianCycle()) {
                    if (minTSPResult.getCost() > currentProblem.getLowerBound()) {
                        // Found better solution! Closing because candidate solution.
                        minTSPResult.newSolutionFound(currentProblem.getOneTree(),
                                                      currentProblem.getLowerBound());
                        minTSPResult.increaseClosedNodesForBestCount(1);
                    }
                } else if (currentProblem.getLowerBound() < minTSPResult.getCost()) {
                    int newNodeCount = branch(currentProblem);
                    minTSPResult.increaseNodeCount(newNodeCount);
                    minTSPResult.increaseIntermediateNodes(1);
                } else {
                    minTSPResult.increaseClosedNodesForBound(1);
                }
            } else {
                minTSPResult.increaseClosedNodesForUnfeasibilityCount(1);
            }
            // else closed because unfeasible
        }

        minTSPResult.finalizeSolution();

        return minTSPResult;
    }

    private int branch(SubProblem currentProblem) {

        HashMap<Integer, Integer> parentsVector = new HashMap<>();
        dfs(currentProblem.getOneTree().getNode(candidateNode), parentsVector, currentProblem.getOneTree());
        int newNodeCount = 0;

        ArrayList<Edge<Integer, Integer>> subCycle = new ArrayList<>();

        int toNode = candidateNode;
        int fromNode = Integer.MAX_VALUE;

        while (fromNode != candidateNode) {
            fromNode = parentsVector.get(toNode);
            subCycle.add(currentProblem.getOneTree().getEdge(fromNode, toNode));
            //System.out.print("(" + fromNode + ", " + toNode + ") ");
            toNode = fromNode;
        }

        ArrayList<Edge<Integer, Integer>> mandatoryEdges = currentProblem.getMandatoryEdges();
        ArrayList<Edge<Integer, Integer>> forbiddenEdges = currentProblem.getForbiddenEdges();

        for (Edge<Integer, Integer> integerIntegerEdge : subCycle) {
            if (!(currentProblem.getMandatoryEdges().contains(integerIntegerEdge) ||
                  currentProblem.getMandatoryEdges().contains(integerIntegerEdge.inverse()))
            ) {
                forbiddenEdges.add(integerIntegerEdge);
                SubProblem sp = new SubProblem(graph,
                                               new ArrayList<>(mandatoryEdges),
                                               new ArrayList<>(forbiddenEdges),
                                               candidateNode);
                subProblemQueue.add(sp);
                newNodeCount++;

                forbiddenEdges.remove(integerIntegerEdge);
                mandatoryEdges.add(integerIntegerEdge);
            }
        }

        return newNodeCount;
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
