package tsp;

import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;

import java.util.ArrayList;
import java.util.List;

public class BranchAndBound {

    public List<SubProblem> problemi;
    public Graph<Integer, Integer, Integer> grafo;
    private Integer nodoCandidato;

    public BranchAndBound(Graph<Integer, Integer, Integer> grafo, Integer nodoCandidato) {
        this.grafo = grafo;
        SubProblem sp = new SubProblem(grafo, new ArrayList<>(), new ArrayList<>(), nodoCandidato);
        problemi = new ArrayList<>();
        problemi.add(sp);
        this.nodoCandidato = nodoCandidato;
    }

    public CicloHamiltoniano branchAndBoundTsp() {

        CicloHamiltoniano minimoCicloHamiltoniano = new CicloHamiltoniano(grafo, Integer.MAX_VALUE);

        while (!problemi.isEmpty()) {
            SubProblem problemaCorrente = problemi.get(0);
            problemi.remove(0);
            if (problemaCorrente.hasCicloHamiltoniano()) {
                if (minimoCicloHamiltoniano.getCosto() > problemaCorrente.getLowerBound()) {
                    minimoCicloHamiltoniano.setCosto(problemaCorrente.getLowerBound());
                    minimoCicloHamiltoniano.setGrafo(problemaCorrente.getUnoTree());
                }
            } else {
                if (problemaCorrente.isAmmissibile() && problemaCorrente.getLowerBound() < minimoCicloHamiltoniano.getCosto()) {
                    branching(problemaCorrente);
                }
            }
        }

        return minimoCicloHamiltoniano;
    }

    private void branching(SubProblem problemaCorrente) {
        List<Edge<Integer, Integer>> sottoCiclo = dfs(problemaCorrente.getUnoTree()
                                                                      .getNode(nodoCandidato), new ArrayList<>());

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

    private List<Edge<Integer, Integer>> dfs(Node<Integer, Integer, Integer> nodoCorrente,
                                             ArrayList<Edge<Integer, Integer>> cicloInterno) {
        if (nodoCorrente.getKey().equals(nodoCandidato) && cicloInterno.size() > 0) {
            return cicloInterno;
        } else {
            for (Edge<Integer, Integer> arcoIncidente : nodoCorrente.getEdges()) {
                cicloInterno.add(arcoIncidente);
                dfs(grafo.getNode(arcoIncidente.getTo()), cicloInterno);
            }
            return cicloInterno;
        }
    }
}
