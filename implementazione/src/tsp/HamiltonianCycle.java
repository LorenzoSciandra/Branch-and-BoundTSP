package tsp;

import graph.structures.Graph;

public class HamiltonianCycle {

    private Graph<Integer, Integer, Integer> graph;
    private int cost;

    public HamiltonianCycle(Graph<Integer, Integer, Integer> graph, int cost) {
        this.cost = cost;
        this.graph = graph;
    }

    public Graph<Integer, Integer, Integer> getGraph() {
        return graph;
    }

    public void setGraph(Graph<Integer, Integer, Integer> graph) {
        this.graph = graph;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

}
