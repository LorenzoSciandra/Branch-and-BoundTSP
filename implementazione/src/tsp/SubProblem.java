package tsp;

import graph.exceptions.GraphNodeMissingException;
import graph.structures.Edge;
import graph.structures.Graph;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static graph.MinimumSpanningTree.kruskalForTSP;

public class SubProblem {

    private Graph<Integer, Integer, Integer> originalGraph;
    private List<Edge<Integer, Integer>> mandatoryEdges;
    private List<Edge<Integer, Integer>> forbiddenEdges;
    private Integer candidateNode;
    private Graph<Integer, Integer, Integer> oneTree;
    private int lowerBound;
    private boolean containsHamiltonianCycle;
    private boolean feasible;

    public SubProblem(Graph<Integer, Integer, Integer> originalGraph, Integer candidateNode) {
        this(originalGraph, new ArrayList<>(0), new ArrayList<>(0), candidateNode);
    }

    public SubProblem(@NotNull Graph<Integer, Integer, Integer> originalGraph,
                      ArrayList<Edge<Integer, Integer>> mandatoryEdges,
                      ArrayList<Edge<Integer, Integer>> forbiddenEdges,
                      Integer candidateNode) {
        this.mandatoryEdges = mandatoryEdges;
        this.forbiddenEdges = forbiddenEdges;
        this.originalGraph = originalGraph;
        this.candidateNode = candidateNode;

        // The sub-problems automatically evaluate themselves, in order to be ready for further branching.
        this.oneTree = compute1Tree();
        this.lowerBound = costo1Tree();
        this.containsHamiltonianCycle = controllaCiclo();
        this.feasible = oneTree.getNodes().size() == originalGraph.getNodes().size() &&
                        oneTree.getEdges().size() == originalGraph.getNodes().size();
    }

    private @NotNull Graph<Integer, Integer, Integer> compute1Tree() {
        // Calculate the minimum spanning tree with the original graph minus the candidateNode.
        // Then, add it back.
        Graph<Integer, Integer, Integer> mst = kruskalForTSP(originalGraph.clone()
                                                                          .removeNode(candidateNode),
                                                             ComparatorIntegerEdge.getInstance(),
                                                             mandatoryEdges,
                                                             forbiddenEdges).addNode(candidateNode);

        List<Edge<Integer, Integer>> mandatoryEdgesIncidentOnCandidate = mandatoryEdges.stream()
                                                                                       .filter((edge) -> edge.isIncidentFor(candidateNode))
                                                                                       .collect(Collectors.toList());
        List<Edge<Integer, Integer>> forbiddenEdgesIncidentOnCandidate = forbiddenEdges.stream()
                                                                                       .filter((edge) -> edge.isIncidentFor(candidateNode))
                                                                                       .collect(Collectors.toList());

        Edge<Integer, Integer> firstEdge = null, secondEdge = null;
        if (mandatoryEdgesIncidentOnCandidate.size() >= 2) {
            // Look for the two least expensive edges.
            firstEdge = mandatoryEdgesIncidentOnCandidate.get(0);
            secondEdge = mandatoryEdgesIncidentOnCandidate.get(1);

            for (Edge<Integer, Integer> e : mandatoryEdgesIncidentOnCandidate) {
                if (firstEdge.getLabel() < secondEdge.getLabel()) {
                    if (e.getLabel() < secondEdge.getLabel()) {
                        secondEdge = e;
                    }
                } else {
                    if (e.getLabel() < firstEdge.getLabel()) {
                        firstEdge = e;
                    }
                }
            }
        } else if (mandatoryEdgesIncidentOnCandidate.size() == 1) {
            firstEdge = mandatoryEdgesIncidentOnCandidate.get(0);

            // Look for the cheapest edge that incides on candidateNode and that isn't forbidden.
            secondEdge = originalGraph.getEdges()
                                      .stream()
                                      .filter((edge) -> !forbiddenEdgesIncidentOnCandidate.contains(edge) && edge.isIncidentFor(candidateNode))
                                      .reduce(originalGraph.getEdges().get(0),
                                              (localMin, edge) -> edge.getLabel() < localMin.getLabel() ?
                                                  edge :
                                                  localMin);
        } else {
            for (Edge<Integer, Integer> edge : originalGraph.getEdges()) {
                if (!forbiddenEdgesIncidentOnCandidate.contains(edge) && edge.isIncidentFor(candidateNode)) {
                    if (firstEdge == null) {
                        firstEdge = edge;
                    } else if (secondEdge == null) {
                        secondEdge = edge;
                    } else {
                        if (firstEdge.getLabel() < secondEdge.getLabel()) {
                            if (edge.getLabel() < secondEdge.getLabel()) {
                                secondEdge = edge;
                            }
                        } else {
                            if (edge.getLabel() < firstEdge.getLabel()) {
                                firstEdge = edge;
                            }
                        }

                    }
                }
            }
        }

        if (firstEdge != null && secondEdge != null) {
            try {
                mst.addEdge(firstEdge).addEdge(secondEdge);
            } catch (GraphNodeMissingException e) {
                e.printStackTrace();
            }
        }

        return mst;
    }

    private int costo1Tree() {
        int costo = 0;

        for (Edge<Integer, Integer> arco : this.oneTree.getEdges()) {
            costo = costo + arco.getLabel();
        }
        return costo;
    }

    private boolean controllaCiclo() {
        boolean controllo = true;

        for (int i = 0; i < oneTree.getNodes().size() && controllo; i++) {
            controllo = oneTree.getNodes().get(i).getEdges().size() == 2;
        }

        return controllo;
    }

    public Graph<Integer, Integer, Integer> getOriginalGraph() {
        return originalGraph;
    }

    public List<Edge<Integer, Integer>> getMandatoryEdges() {
        return mandatoryEdges;
    }

    public List<Edge<Integer, Integer>> getForbiddenEdges() {
        return forbiddenEdges;
    }

    public Integer getCandidateNode() {
        return candidateNode;
    }

    public Graph<Integer, Integer, Integer> getOneTree() {
        return oneTree;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public boolean hasCicloHamiltoniano() {
        return containsHamiltonianCycle;
    }

    public boolean isFeasible() {
        return feasible;
    }

    private static class ComparatorIntegerEdge implements Comparator<Edge<Integer, Integer>> {

        private static ComparatorIntegerEdge instance = null;

        public synchronized static ComparatorIntegerEdge getInstance() {
            if (instance == null) {
                instance = new ComparatorIntegerEdge();
            }

            return instance;
        }

        @Override
        public int compare(Edge<Integer, Integer> o1, Edge<Integer, Integer> o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    }
}
