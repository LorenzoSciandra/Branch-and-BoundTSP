package tsp;

import graph.exceptions.GraphNodeMissingException;
import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static graph.MinimumSpanningTree.kruskalForTSP;

public class SubProblem implements Comparable<SubProblem> {

    private Graph<Integer, Integer, Integer> originalGraph;
    private ArrayList<Edge<Integer, Integer>> mandatoryEdges;
    private ArrayList<Edge<Integer, Integer>> forbiddenEdges;
    private Integer candidateNode;
    private Integer subProblemTreeLevel;
    private Graph<Integer, Integer, Integer> oneTree;
    private int lowerBound;
    private boolean containsHamiltonianCycle;
    private boolean feasible;

    /**
     * Constructor made to be used to generate a root SubProblem.
     *
     * @param originalGraph The original graph to work on
     * @param candidateNode The candidate node for this sub problem
     */
    public SubProblem(Graph<Integer, Integer, Integer> originalGraph, Integer candidateNode) {
        this(originalGraph, new ArrayList<>(0), new ArrayList<>(0), candidateNode, 0);
    }

    /**
     * Generic constructor for a SubProblem at any level
     *
     * @param originalGraph       The original graph to work on
     * @param mandatoryEdges      A list of edges that must be included in the path
     * @param forbiddenEdges      A list of edges that must not be included in the path
     * @param candidateNode       The candidate node for this sub problem
     * @param subProblemTreeLevel The level of the sub problem in the search tree
     */
    public SubProblem(@NotNull Graph<Integer, Integer, Integer> originalGraph,
                      ArrayList<Edge<Integer, Integer>> mandatoryEdges,
                      ArrayList<Edge<Integer, Integer>> forbiddenEdges,
                      Integer candidateNode,
                      Integer subProblemTreeLevel) {
        this.mandatoryEdges = mandatoryEdges;
        this.forbiddenEdges = forbiddenEdges;
        this.originalGraph = originalGraph;
        this.candidateNode = candidateNode;
        this.subProblemTreeLevel = subProblemTreeLevel;

        // The sub-problems automatically evaluate themselves, in order to be ready for further branching.
        this.oneTree = compute1Tree();
        this.lowerBound = compute1TreeCost();
        this.containsHamiltonianCycle = checkForHamiltonianCycle();
        this.feasible = oneTree.getNodes().size() == originalGraph.getNodes().size() &&
                        oneTree.getEdges().size() == originalGraph.getNodes().size();
    }

    private @NotNull Graph<Integer, Integer, Integer> compute1Tree() {
        // Calculate the minimum spanning tree with the original graph minus the candidateNode.
        // Then, add it back.
        ArrayList<Edge<Integer, Integer>> realMandatory = (ArrayList<Edge<Integer, Integer>>) mandatoryEdges.clone();

        for (Edge<Integer, Integer> mandatory : mandatoryEdges) {
            if (mandatory.getTo().equals(candidateNode) || mandatory.getFrom().equals(candidateNode)) {
                realMandatory.remove(mandatory);
            }
        }


        Graph<Integer, Integer, Integer> mst = kruskalForTSP(originalGraph.clone()
                                                                          .removeNode(candidateNode),
                                                             ComparatorIntegerEdge.getInstance(),
                                                             realMandatory,
                                                             forbiddenEdges).addNode(candidateNode);

        List<Edge<Integer, Integer>> incidentMandatoryEdges = mandatoryEdges.stream()
                                                                            .filter((edge) -> edge.isIncidentFor(candidateNode))
                                                                            .collect(Collectors.toList());
        List<Edge<Integer, Integer>> incidentForbiddenEdges = forbiddenEdges.stream()
                                                                            .filter((edge) -> edge.isIncidentFor(candidateNode))
                                                                            .collect(Collectors.toList());

        if (mst.getNodes().size() == originalGraph.getNodes().size()) {
            Edge<Integer, Integer> firstEdge = null, secondEdge = null;
            if (incidentMandatoryEdges.size() >= 2) {
                // Look for the two least expensive edges.
                for (Edge<Integer, Integer> edge : incidentMandatoryEdges) {
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
            } else if (incidentMandatoryEdges.size() == 1) {
                firstEdge = incidentMandatoryEdges.get(0);

                for (Edge<Integer, Integer> edge : originalGraph.getEdges()) {
                    if (!(incidentForbiddenEdges.contains(edge) || incidentForbiddenEdges.contains(edge.inverse())) && (edge.isIncidentFor(candidateNode)) && (!(firstEdge.equals(edge) || firstEdge.inverse()
                                                                                                                                                                                                    .equals(edge)))) {
                        if (secondEdge == null) {
                            secondEdge = edge;
                            //System.out.println("Arco trovato:" + edge.getFrom() + " " + edge.getTo() + "\n");
                        } else if (secondEdge.getLabel() > edge.getLabel()) {
                            secondEdge = edge;
                            //System.out.println("Arco aggiornato:" + edge.getFrom() + " " + edge.getTo() + "\n");
                        }
                    }
                }

            } else {
                for (Edge<Integer, Integer> edge : originalGraph.getEdges()) {
                    if (!(incidentForbiddenEdges.contains(edge) || incidentForbiddenEdges.contains(edge.inverse())) && edge.isIncidentFor(candidateNode)) {
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
                    //System.out.println("Archi trovati: " + firstEdge.toString() + " e " + secondEdge.toString());
                    mst.addEdge(firstEdge).addEdge(secondEdge);
                } catch (GraphNodeMissingException e) {
                    e.printStackTrace();
                }
            }
        }

        return mst;
    }

    private int compute1TreeCost() {
        return this.oneTree.getEdges()
                           .stream()
                           .mapToInt(Edge::getLabel)
                           .sum();
    }

    private boolean checkForHamiltonianCycle() {
        return oneTree.getNodes()
                      .stream()
                      .map(Node::getDegree)
                      .allMatch(degree -> degree == 2);
    }

    public Graph<Integer, Integer, Integer> getOriginalGraph() {
        return originalGraph;
    }

    public ArrayList<Edge<Integer, Integer>> getMandatoryEdges() {
        return mandatoryEdges;
    }

    public ArrayList<Edge<Integer, Integer>> getForbiddenEdges() {
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

    public boolean containsHamiltonianCycle() {
        return containsHamiltonianCycle;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public Integer getSubProblemTreeLevel() {
        return subProblemTreeLevel;
    }

    @Override
    public int compareTo(@NotNull SubProblem o) {
        // First order by search tree level
        /*int levelComparison = Integer.compare(this.subProblemTreeLevel, o.subProblemTreeLevel);
        if (levelComparison != 0) {
            return levelComparison;
        }*/

        // If two Nodes/SubProblems are at the same level, order them by lowest lowerBound
        int boundComparison = Integer.compare(this.lowerBound, o.lowerBound);
        if (boundComparison != 0) {
            return boundComparison;
        }

        // Finally, try to order Nodes with the same lowerBound giving priority to SubProblems that have a HamCycle.
        if (this.containsHamiltonianCycle) {
            return -1;
        } else {
            return 1;
        }

    }

    public String toString() {
        String s = getOneTree().toString();
        s = s + "\n costo: " + getLowerBound();
        s = s + "\n Archi forzati: " + getMandatoryEdges().toString();
        s = s + "\n Archi vietati: " + getForbiddenEdges().toString();
        return s;
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
