package tsp;

import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BranchAndBound {

    public List<SubProblem> problemi;
    public Graph<Integer, Integer, Integer> grafo;
    private Integer nodoCandidato;

    public BranchAndBound(Graph<Integer, Integer, Integer> grafo, Integer nodoCandidato) {
        this.grafo = grafo;
        this.problemi = new ArrayList<>();
        this.nodoCandidato = nodoCandidato;
    }

    public HamiltonianCycle branchAndBoundTsp() {
        SubProblem sp = new SubProblem(grafo, new ArrayList<>(), new ArrayList<>(), nodoCandidato);
        problemi.add(sp);
        HamiltonianCycle minimumHamiltonianCycle = new HamiltonianCycle(grafo, Integer.MAX_VALUE);

        while (!problemi.isEmpty()) {
            SubProblem problemaCorrente = problemi.get(0);
            problemi.remove(0);
            if (problemaCorrente.hasCicloHamiltoniano()) {
                if (minimumHamiltonianCycle.getCost() > problemaCorrente.getLowerBound()) {
                    minimumHamiltonianCycle.setCost(problemaCorrente.getLowerBound());
                    minimumHamiltonianCycle.setGraph(problemaCorrente.getUnoTree());
                }
            } else {
                if (problemaCorrente.isAmmissibile() && problemaCorrente.getLowerBound() < minimumHamiltonianCycle.getCost()) {
                    branching(problemaCorrente);
                }
            }
        }

        return minimumHamiltonianCycle;
    }

    private void branching(SubProblem problemaCorrente) {

        HashMap<Integer,Integer> vettorePadri = new HashMap<>();
        dfs(Objects.requireNonNull(problemaCorrente.getUnoTree().getNode(nodoCandidato)), vettorePadri, problemaCorrente.getUnoTree());

        ArrayList<Edge<Integer,Integer>> sottoCiclo = new ArrayList<>();

        int nodoTo = nodoCandidato;
        int nodoFrom = Integer.MAX_VALUE;
        while(nodoFrom != nodoCandidato){
            nodoFrom = vettorePadri.get(nodoTo);
            sottoCiclo.add(problemaCorrente.getUnoTree().getEdge(nodoFrom,nodoTo));
            nodoTo = nodoFrom;
        }

        ArrayList<Edge<Integer, Integer>> archiForzati = new ArrayList<>();
        ArrayList<Edge<Integer, Integer>> archiVietati = new ArrayList<>();

        for (int i = 0; i < sottoCiclo.size(); i++) {
            archiVietati.add(sottoCiclo.get(i));
            SubProblem sp = new SubProblem(grafo, archiVietati, archiForzati, nodoCandidato);
            problemi.add(sp);
            archiVietati.remove(0);
            archiForzati.add(sottoCiclo.get(i));
        }
    }

    private void dfs(Node<Integer, Integer, Integer> nodoCorrente , HashMap<Integer,Integer> vettorePadri, Graph<Integer,Integer,Integer> grafo) {

        for(Edge<Integer, Integer> arcoUscente: nodoCorrente.getEdges()){
            if(!vettorePadri.containsKey(arcoUscente.getTo())){
                if(!vettorePadri.containsKey(nodoCorrente.getKey()) ||
                        !vettorePadri.get(nodoCorrente.getKey()).equals(arcoUscente.getTo())){

                    vettorePadri.put(arcoUscente.getTo(),nodoCorrente.getKey());
                    dfs(Objects.requireNonNull(grafo.getNode(arcoUscente.getTo())),vettorePadri,grafo);
                }

            }
        }

    }
}
