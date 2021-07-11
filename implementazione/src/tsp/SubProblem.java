package tsp;

import graph.MinimumSpanningTree;
import graph.exceptions.GraphNodeMissingException;
import graph.structures.Edge;
import graph.structures.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SubProblem {

    private Graph<Integer, Integer, Integer> grafoOriginale;
    private List<Edge<Integer, Integer>> archiForzati;
    private List<Edge<Integer, Integer>> archiVietati;
    private Integer nodoCandidato;
    private Graph<Integer, Integer, Integer> unoTree;
    private int lowerBound;
    private boolean haCicloHamiltoniano;
    private boolean ammissibile;

    public SubProblem(Graph<Integer, Integer, Integer> grafoOriginale,
                      ArrayList<Edge<Integer, Integer>> archiForzati,
                      ArrayList<Edge<Integer, Integer>> archiVietati,
                      Integer nodoCandidato) {
        this.archiForzati = archiForzati;
        this.archiVietati = archiVietati;
        this.grafoOriginale = grafoOriginale;
        this.nodoCandidato = nodoCandidato;
        this.unoTree = calcola1Tree();
        this.lowerBound = costo1Tree();
        this.haCicloHamiltoniano = controllaCiclo();
        this.ammissibile = unoTree.getNodes().size() == grafoOriginale.getNodes().size() &&
                           unoTree.getEdges().size() == grafoOriginale.getNodes().size();
    }

    // evaluate
    private Graph<Integer, Integer, Integer> calcola1Tree() {
        Graph<Integer, Integer, Integer> grafoPerMST = grafoOriginale.clone();
        grafoPerMST.removeNode(nodoCandidato);

        Graph<Integer, Integer, Integer> mst = MinimumSpanningTree.kruskalForTSP(grafoPerMST,
                                                                                 new ComparatorIntegerEdge(),
                                                                                 archiForzati, archiVietati);
        mst.addNode(nodoCandidato);

        ArrayList<Edge<Integer, Integer>> archiIncidentiForzatiCandidato = new ArrayList<>();
        for (Edge<Integer, Integer> incidenteForzato : archiForzati) {
            if (Objects.equals(incidenteForzato.getFrom(), nodoCandidato) || Objects.equals(incidenteForzato.getTo(),
                                                                                            nodoCandidato)) {
                archiIncidentiForzatiCandidato.add(incidenteForzato);
            }
        }

        ArrayList<Edge<Integer, Integer>> archiIncidentiVietatiCandidato = new ArrayList<>();
        for (Edge<Integer, Integer> incidenteVietato : archiVietati) {
            if (Objects.equals(incidenteVietato.getFrom(), nodoCandidato) || Objects.equals(incidenteVietato.getTo(),
                                                                                            nodoCandidato)) {
                archiIncidentiVietatiCandidato.add(incidenteVietato);
            }
        }

        Edge<Integer, Integer> primoArcoTrovato = null;
        Edge<Integer, Integer> secondoArcoTrovato = null;
        if (archiIncidentiForzatiCandidato.size() >= 2) {
            for (Edge<Integer, Integer> e : archiIncidentiForzatiCandidato) {
                if (primoArcoTrovato == null) {
                    primoArcoTrovato = e;
                } else if (secondoArcoTrovato == null) {
                    secondoArcoTrovato = e;
                } else {
                    if (primoArcoTrovato.getLabel() < secondoArcoTrovato.getLabel()) {
                        if (e.getLabel() < secondoArcoTrovato.getLabel()) {
                            secondoArcoTrovato = e;
                        }
                    } else {
                        if (e.getLabel() < primoArcoTrovato.getLabel()) {
                            primoArcoTrovato = e;
                        }
                    }
                }
            }

        } else if (archiIncidentiForzatiCandidato.size() == 1) {
            primoArcoTrovato = archiIncidentiForzatiCandidato.get(0);

            for (Edge<Integer, Integer> arcoDaAggiungere : grafoOriginale.getEdges()) {
                if (!archiIncidentiVietatiCandidato.contains(arcoDaAggiungere)) {
                    if (Objects.equals(arcoDaAggiungere.getTo(), nodoCandidato) || Objects.equals(arcoDaAggiungere.getFrom(), nodoCandidato)) {
                        if (secondoArcoTrovato == null) {
                            secondoArcoTrovato = arcoDaAggiungere;
                        } else {
                            if (arcoDaAggiungere.getLabel() < secondoArcoTrovato.getLabel()) {
                                secondoArcoTrovato = arcoDaAggiungere;
                            }
                        }
                    }
                }
            }

        } else {
            for (Edge<Integer, Integer> arcoDaAggiungere : grafoOriginale.getEdges()) {
                if (!archiIncidentiVietatiCandidato.contains(arcoDaAggiungere)) {
                    if (Objects.equals(arcoDaAggiungere.getTo(), nodoCandidato) || Objects.equals(arcoDaAggiungere.getFrom(), nodoCandidato)) {
                        if (primoArcoTrovato == null) {
                            primoArcoTrovato = arcoDaAggiungere;
                        } else if (secondoArcoTrovato == null) {
                            secondoArcoTrovato = arcoDaAggiungere;
                        } else {
                            if (primoArcoTrovato.getLabel() < secondoArcoTrovato.getLabel()) {
                                if (arcoDaAggiungere.getLabel() < secondoArcoTrovato.getLabel()) {
                                    secondoArcoTrovato = arcoDaAggiungere;
                                }
                            } else {
                                if (arcoDaAggiungere.getLabel() < primoArcoTrovato.getLabel()) {
                                    primoArcoTrovato = arcoDaAggiungere;
                                }
                            }
                        }
                    }
                }
            }
        }


        if (primoArcoTrovato != null && secondoArcoTrovato != null) {
            try {
                mst.addEdge(primoArcoTrovato.getFrom(), secondoArcoTrovato.getTo(), secondoArcoTrovato.getLabel());
                mst.addEdge(secondoArcoTrovato.getFrom(), secondoArcoTrovato.getTo(), secondoArcoTrovato.getLabel());
            } catch (GraphNodeMissingException e) {
                e.printStackTrace();
            }
        }


        return mst;
    }

    private int costo1Tree() {
        int costo = 0;

        for (Edge<Integer, Integer> arco : this.unoTree.getEdges()) {
            costo = costo + arco.getLabel();
        }
        return costo;
    }

    private boolean controllaCiclo() {
        boolean controllo = true;

        for (int i = 0; i < unoTree.getNodes().size() && controllo; i++) {
            controllo = unoTree.getNodes().get(i).getEdges().size() == 2;
        }

        return controllo;
    }

    public Graph<Integer, Integer, Integer> getGrafoOriginale() {
        return grafoOriginale;
    }

    public List<Edge<Integer, Integer>> getArchiForzati() {
        return archiForzati;
    }

    public List<Edge<Integer, Integer>> getArchiVietati() {
        return archiVietati;
    }

    public Integer getNodoCandidato() {
        return nodoCandidato;
    }

    public Graph<Integer, Integer, Integer> getUnoTree() {
        return unoTree;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public boolean hasCicloHamiltoniano() {
        return haCicloHamiltoniano;
    }

    public boolean isAmmissibile() {
        return ammissibile;
    }

    private class ComparatorIntegerEdge implements Comparator<Edge<Integer, Integer>> {
        @Override
        public int compare(Edge<Integer, Integer> o1, Edge<Integer, Integer> o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    }
}
