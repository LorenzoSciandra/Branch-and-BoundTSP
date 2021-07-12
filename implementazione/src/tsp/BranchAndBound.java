package tsp;

import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BranchAndBound {

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
        HamiltonianCycle minimumHamiltonianCycle = new HamiltonianCycle(graph, Integer.MAX_VALUE);

        while (!subProblemQueue.isEmpty()) {
            SubProblem problemaCorrente = subProblemQueue.get(0);
            subProblemQueue.remove(0);
            if (problemaCorrente.containsHamiltonianCycle()) {
                if (minimumHamiltonianCycle.getCost() > problemaCorrente.getLowerBound()) {
                    minimumHamiltonianCycle.setCost(problemaCorrente.getLowerBound());
                    minimumHamiltonianCycle.setGraph(problemaCorrente.getOneTree());
                }
            } else {
                if (problemaCorrente.isFeasible() && problemaCorrente.getLowerBound() < minimumHamiltonianCycle.getCost()) {
                    branching(problemaCorrente);
                }
            }
        }

        return minimumHamiltonianCycle;
    }

    private void branching(SubProblem problemaCorrente) {

        HashMap<Integer, Integer> vettorePadri = new HashMap<>();
        dfs(Objects.requireNonNull(problemaCorrente.getOneTree()
                                                   .getNode(candidateNode)), vettorePadri,
            problemaCorrente.getOneTree());

        ArrayList<Edge<Integer, Integer>> sottoCiclo = new ArrayList<>();

        int nodoTo = candidateNode;
        int nodoFrom = Integer.MAX_VALUE;
        while (nodoFrom != candidateNode) {
            nodoFrom = vettorePadri.get(nodoTo);
            sottoCiclo.add(problemaCorrente.getOneTree().getEdge(nodoFrom, nodoTo));
            nodoTo = nodoFrom;
        }

        ArrayList<Edge<Integer, Integer>> archiForzati = new ArrayList<>();
        ArrayList<Edge<Integer, Integer>> archiVietati = new ArrayList<>();

        for (int i = 0; i < sottoCiclo.size(); i++) {
            archiVietati.add(sottoCiclo.get(i));
            SubProblem sp = new SubProblem(graph, archiVietati, archiForzati, candidateNode);
            subProblemQueue.add(sp);
            archiVietati.remove(0);
            archiForzati.add(sottoCiclo.get(i));
        }
    }

    private void dfs(Node<Integer, Integer, Integer> nodoCorrente, HashMap<Integer, Integer> vettorePadri,
                     Graph<Integer, Integer, Integer> grafo) {

        for (Edge<Integer, Integer> arcoUscente : nodoCorrente.getEdges()) {
            if (!vettorePadri.containsKey(arcoUscente.getTo())) {
                if (!vettorePadri.containsKey(nodoCorrente.getKey()) ||
                    !vettorePadri.get(nodoCorrente.getKey()).equals(arcoUscente.getTo())) {

                    vettorePadri.put(arcoUscente.getTo(), nodoCorrente.getKey());
                    dfs(Objects.requireNonNull(grafo.getNode(arcoUscente.getTo())), vettorePadri, grafo);
                }

            }
        }

    }
}
