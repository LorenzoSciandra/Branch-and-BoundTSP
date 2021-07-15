package tsp;

import graph.structures.Edge;
import graph.structures.Graph;

import java.util.ArrayList;

public class TSPResult {

    private Graph<Integer, Integer, Integer> graph;
    private int cost;
    private ResultState state = ResultState.Unsolved;

    public TSPResult(Graph<Integer, Integer, Integer> graph, int cost) {
        this.cost = cost;
        this.graph = graph;
    }

    public Graph<Integer, Integer, Integer> getGraph() {
        return graph;
    }

    public int getCost() {
        return cost;
    }

    ResultState getState() {
        return state;
    }

    public void newSolutionFound(Graph<Integer, Integer, Integer> graph, int cost) {
        if (this.state != ResultState.Unsolved && this.state != ResultState.Solvable) {
            throw new IllegalStateException("Cannot update a solution that been finalized.");
        }

        this.graph = graph;
        this.cost = cost;

        this.state = ResultState.Solvable;
    }

    public void finalizeSolution() {
        if (this.state == ResultState.Solvable) {
            this.state = ResultState.Solved;
        } else if (this.state == ResultState.Unsolved) {
            this.state = ResultState.Unsolvable;
        } else {
            throw new IllegalStateException("Cannot finalize a solution already finalized.");
        }
    }

    public ArrayList<Edge<Integer, Integer>> getPath() throws IllegalStateException {
        if (state == ResultState.Unsolvable) {
            throw new IllegalStateException("The related problem has been deemed unsolvable, so no path exists.");
        } else if (state == ResultState.Unsolved) {
            throw new IllegalStateException("A solution to the related problem has yet to be found.");
        }

        ArrayList<Edge<Integer, Integer>> path = new ArrayList<>();

        int startNode = graph.getNodes().get(0).getKey();
        int fromNode = startNode;
        int prevNode = fromNode;

        // Until we haven't reached the beginning cycle through all the nodes.
        do {
            int i = 0;
            Edge<Integer, Integer> edge;

            // Look for an edge that brings us to a new node (in order to avoid going back and returning a 2-edge cycle)
            do {
                edge = graph.getNode(fromNode).getEdges().get(i);
                i++;
            } while (prevNode == edge.getTo());

            path.add(edge);
            // Keep in mind where we came from in the previous iteration
            prevNode = fromNode;
            fromNode = edge.getTo();
        } while (fromNode != startNode);

        return path;
    }

    @Override
    public String toString() {
        return switch (this.state) {
            case Solved -> String.format("A best solution has been found with cost %d. Its path is:\n%s\n\n",
                                         cost,
                                         getPath());
            case Solvable -> String.format("The problem is solvable. Current best cost: %d. Path:\n%s\n\n",
                                           cost,
                                           getPath());
            case Unsolvable -> "The problem has been deemed unsolvable. No solution has been found.";
            case Unsolved -> "No solution has been found yet.";
        };
    }

    public enum ResultState {
        Solved, Solvable, Unsolvable, Unsolved
    }

}
