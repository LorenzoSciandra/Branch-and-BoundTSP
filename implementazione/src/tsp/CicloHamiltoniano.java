package tsp;

import graph.structures.Graph;

public class CicloHamiltoniano {

    private Graph<Integer,Integer,Integer> grafo;
    private int costo;

    public CicloHamiltoniano(Graph<Integer,Integer,Integer> grafo, int costo){
        this.costo = costo;
        this.grafo = grafo;
    }

    public Graph<Integer, Integer, Integer> getGrafo() {
        return grafo;
    }

    public int getCosto() {
        return costo;
    }

    public void setGrafo(Graph<Integer, Integer, Integer> grafo) {
        this.grafo = grafo;
    }

    public void setCosto(int costo) {
        this.costo = costo;
    }

}
