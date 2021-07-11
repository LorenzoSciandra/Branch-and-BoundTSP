package tsp;

import graph.MinimumSpanningTree;
import graph.exceptions.GraphNodeMissingException;
import graph.structures.Edge;
import graph.structures.Graph;
import graph.structures.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SubProblem {

  private Graph<Integer, Integer, Integer> grafoOriginale;
  private List<Edge<Integer, Integer>> archiForzati;
  private List<Edge<Integer, Integer>> archiVietati;
  private Node<Integer,Integer,Integer> nodoCandidato;

  public SubProblem(Graph<Integer, Integer, Integer> grafoOriginale,
                    ArrayList<Edge<Integer, Integer>> archiForzati,
                    ArrayList<Edge<Integer, Integer>> archiVietati,
                    Node<Integer,Integer,Integer> nodoCandidato) {
      this.archiForzati = archiForzati;
      this.archiVietati = archiVietati;
      this.grafoOriginale = grafoOriginale;
      this.nodoCandidato = nodoCandidato;
  }

    private class ComparatorIntegerEdge implements Comparator<Edge<Integer, Integer>>{
        @Override
        public int compare(Edge<Integer, Integer> o1, Edge<Integer, Integer> o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    }

  // evaluate
  public Graph<Integer,Integer,Integer> calcola1Tree (){
      Graph<Integer,Integer,Integer> grafoPerMST = grafoOriginale.cloneGraph();
      grafoPerMST.removeNode(nodoCandidato.getKey());

      Graph<Integer,Integer,Integer> mst = MinimumSpanningTree.kruskalForTSP(grafoPerMST, new ComparatorIntegerEdge(),archiForzati, archiVietati);
      mst.addNode(nodoCandidato.getKey(),nodoCandidato.getValue());

      ArrayList<Edge<Integer,Integer>> archiIncidentiForzatiCandidato = new ArrayList<>();
      for (Edge<Integer,Integer> incidenteForzato: archiForzati){
          if(Objects.equals(incidenteForzato.getFrom(), nodoCandidato.getKey()) || Objects.equals(incidenteForzato.getTo(), nodoCandidato.getKey())){
              archiIncidentiForzatiCandidato.add(incidenteForzato);
          }
      }

      ArrayList<Edge<Integer,Integer>> archiIncidentiVietatiCandidato = new ArrayList<>();
      for (Edge<Integer,Integer> incidenteVietato: archiVietati){
          if(Objects.equals(incidenteVietato.getFrom(), nodoCandidato.getKey()) || Objects.equals(incidenteVietato.getTo(), nodoCandidato.getKey())){
              archiIncidentiVietatiCandidato.add(incidenteVietato);
          }
      }

      Edge<Integer,Integer> primoArcoTrovato = null;
      Edge<Integer,Integer> secondoArcoTrovato = null;
      if(archiIncidentiForzatiCandidato.size()>=2) {
        for(Edge<Integer,Integer> e: archiIncidentiForzatiCandidato){
            if(primoArcoTrovato == null){
                primoArcoTrovato = e;
            }
            else if(secondoArcoTrovato == null){
                secondoArcoTrovato = e;
            }
            else{
                if(primoArcoTrovato.getLabel() < secondoArcoTrovato.getLabel()){
                    if(e.getLabel() < secondoArcoTrovato.getLabel()){
                        secondoArcoTrovato = e;
                    }
                }
                else{
                    if(e.getLabel() < primoArcoTrovato.getLabel()){
                        primoArcoTrovato = e;
                    }
                }
            }
        }

      }
      else if(archiIncidentiForzatiCandidato.size()==1){
          primoArcoTrovato = archiIncidentiForzatiCandidato.get(0);

          for(Edge<Integer,Integer> arcoDaAggiungere: grafoOriginale.getEdges()){
              if(!archiIncidentiVietatiCandidato.contains(arcoDaAggiungere)){
                  if(Objects.equals(arcoDaAggiungere.getTo(), nodoCandidato.getKey()) || Objects.equals(arcoDaAggiungere.getFrom(), nodoCandidato.getKey())){
                      if(secondoArcoTrovato == null){
                          secondoArcoTrovato = arcoDaAggiungere;
                      }
                      else {
                          if(arcoDaAggiungere.getLabel() < secondoArcoTrovato.getLabel()){
                              secondoArcoTrovato = arcoDaAggiungere;
                          }
                      }
                  }
              }
          }

      }
      else{
          for(Edge<Integer,Integer> arcoDaAggiungere: grafoOriginale.getEdges()){
              if(!archiIncidentiVietatiCandidato.contains(arcoDaAggiungere)){
                  if(Objects.equals(arcoDaAggiungere.getTo(), nodoCandidato.getKey()) || Objects.equals(arcoDaAggiungere.getFrom(), nodoCandidato.getKey())) {
                      if(primoArcoTrovato == null){
                          primoArcoTrovato = arcoDaAggiungere;
                      }
                      else if(secondoArcoTrovato == null){
                          secondoArcoTrovato = arcoDaAggiungere;
                      }
                      else{
                          if(primoArcoTrovato.getLabel() < secondoArcoTrovato.getLabel()){
                              if(arcoDaAggiungere.getLabel() < secondoArcoTrovato.getLabel()){
                                  secondoArcoTrovato = arcoDaAggiungere;
                              }
                          }
                          else{
                              if(arcoDaAggiungere.getLabel() < primoArcoTrovato.getLabel()){
                                  primoArcoTrovato = arcoDaAggiungere;
                              }
                          }
                      }
                  }
              }
          }
      }


      if(primoArcoTrovato!=null && secondoArcoTrovato!=null){
          try {
              mst.addEdge(primoArcoTrovato.getFrom(),secondoArcoTrovato.getTo(),secondoArcoTrovato.getLabel());
              mst.addEdge(secondoArcoTrovato.getFrom(),secondoArcoTrovato.getTo(),secondoArcoTrovato.getLabel());
          } catch (GraphNodeMissingException e) {
              e.printStackTrace();
          }
      }


      return mst;
  }


  // branch
}
