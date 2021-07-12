package tsp;

import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BranchAndBound {

    // todo convert to priority queue
    public List<SubProblem> subProblemQueue;
    public Graph<Integer, Integer, Integer> graph;
    private Integer candidateNode;

    public BranchAndBound(Graph<Integer, Integer, Integer> graph, Integer candidateNode) {
        this.graph = graph;
        this.subProblemQueue = new ArrayList<>();
        this.candidateNode = candidateNode;
    }

    public HamiltonianCycle solveProblem() {
        SubProblem rootProblem = new SubProblem(graph, candidateNode);
        subProblemQueue.add(rootProblem);
        HamiltonianCycle minHamiltonianCycle = new HamiltonianCycle(graph, Integer.MAX_VALUE);

        while (!subProblemQueue.isEmpty()) {
            SubProblem currentProblem = subProblemQueue.remove(0);
            if (currentProblem.containsHamiltonianCycle()) {
                if (minHamiltonianCycle.getCost() > currentProblem.getLowerBound()) {
                    // found better solution! Closing because candidate solution.
                    minHamiltonianCycle.setCost(currentProblem.getLowerBound());
                    minHamiltonianCycle.setGraph(currentProblem.getOneTree());
                }
            } else if (currentProblem.isFeasible() && currentProblem.getLowerBound() < minHamiltonianCycle.getCost()) {
                branch(currentProblem);
            }
            // else closed because unfeasible
        }

        return minHamiltonianCycle;
    }

    private void branch(SubProblem currentProblem) {

        HashMap<Integer, Integer> parentsVector = new HashMap<>();
        dfs(currentProblem.getOneTree().getNode(candidateNode), parentsVector, currentProblem.getOneTree());

        ArrayList<Edge<Integer, Integer>> subCycle = new ArrayList<>();

        int toNode = candidateNode;
        int fromNode = Integer.MAX_VALUE;
        while (fromNode != candidateNode) {
            fromNode = parentsVector.get(toNode);
            subCycle.add(currentProblem.getOneTree().getEdge(fromNode, toNode));
            toNode = fromNode;
        }

        ArrayList<Edge<Integer, Integer>> mandatoryEdges = new ArrayList<>();
        ArrayList<Edge<Integer, Integer>> forbiddenEdges = new ArrayList<>();

        for (Edge<Integer, Integer> integerIntegerEdge : subCycle) {
            forbiddenEdges.add(integerIntegerEdge);
            SubProblem sp = new SubProblem(graph,
                                           ((List<Edge<Integer, Integer>>) forbiddenEdges.clone()),
                                           ((List<Edge<Integer, Integer>>) mandatoryEdges.clone()),
                                           candidateNode);
            subProblemQueue.add(sp);
            forbiddenEdges.remove(0);
            mandatoryEdges.add(integerIntegerEdge);
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
