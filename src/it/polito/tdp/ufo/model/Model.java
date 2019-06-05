package it.polito.tdp.ufo.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.ufo.db.SightingsDAO;

public class Model 
{
	Map<String, String> backVisit;
	//classe privata del modello, dato che tanto la usa solo lui
	private class EdgeTraversedGraphListener implements TraversalListener<String, DefaultEdge> 
	{

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {			
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) 
		{
			String sourceVertex = grafo.getEdgeSource(ev.getEdge()) ;
			String targetVertex = grafo.getEdgeTarget(ev.getEdge()) ;
			
			/* se il grafo è orientato, allora souce  == parent, target == child */
			/* se il grafo non è orientato, potrebbe anche esser al contratio */
			
			if( !backVisit.containsKey(targetVertex) && backVisit.containsKey(sourceVertex) ) 
			{
				backVisit.put(targetVertex, sourceVertex) ;
			} 
			else if(!backVisit.containsKey(sourceVertex) && backVisit.containsKey(targetVertex)) 
			{
				backVisit.put(sourceVertex, targetVertex) ;
			}			
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<String> e) {			
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<String> e) {			
		}
		
	}

	private SimpleDirectedGraph<String, DefaultEdge> grafo;
	
	private List<Sighting> avvistamenti;
	
	private List<String>predecessori;
	private List<String>successori;

	
	private SightingsDAO dao;
	
	public Model()
	{
		avvistamenti = new ArrayList<Sighting>();
		dao = new SightingsDAO();
		avvistamenti = dao.getSightings();
		
		predecessori = new ArrayList<String>();
		successori = new ArrayList<String>();
		
		grafo = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	}

	public List<Integer> getAnniAvvistamenti() 
	{
		List<Integer> res = dao.getYearSightings();
		Collections.sort(res);
		return res;
	}

	public Integer getNumeroAvvistamenti(int anno) 
	{
		int res = 0;
		for (Sighting a : avvistamenti)
		{
			if (a.getDatetime().getYear() == anno)
				res++;
		}
		return res;
	}

	public Set<String> creaGrafo(int anno) 
	{
		// vertici
		Set<String> statiInteressati = new HashSet<String>();
		for (Sighting s : avvistamenti)
		{
			if (s.getDatetime().getYear() == anno)
			{
				if (!statiInteressati.contains(s.getState()))
					statiInteressati.add(s.getState());
			}
		}
		Graphs.addAllVertices(this.grafo, statiInteressati);
		
		//archi
		List<Sighting> avvistamentiAnnoInteresse = dao.getSightingsSelectedYear(anno);
		for (Sighting s1 : avvistamentiAnnoInteresse)
		{
			for (Sighting s2 : avvistamentiAnnoInteresse)
			{
				if ((!s1.equals(s2)) && 
						s1.getState().compareTo(s2.getState()) != 0 &&
						s1.getDatetime().isAfter(s2.getDatetime()))
				{
					// creo arco
					
					if (!grafo.containsEdge(s1.getState(), s2.getState()))
						grafo.addEdge(s1.getState(), s2.getState());
				}
			}
		}
		System.out.println(grafo.vertexSet().size() + "-" + grafo.edgeSet().size());
		return grafo.vertexSet();
	}

	/*
	 * Predecessori e successori sostituibili con liste apposite
	 * Collegati errato, neighbours mi stampa solo i vicini, io voglio tutti i raggiungibili (iteratore)
	 * */
	public void analizza(String stato)
	{
		
		
		
		//immediatamente successivi
		/*for (DefaultEdge e : grafo.outgoingEdgesOf(stato))
		{
			String possibileS = grafo.getEdgeTarget(e);
			if (successori.contains(possibileS))
				successori.add(possibileS);
		}
		
		
		//immediatamente precedenti
		for (DefaultEdge e : grafo.incomingEdgesOf(stato))
		{
			String possibileP = grafo.getEdgeTarget(e);
			if (predecessori.contains(possibileP))
				predecessori.add(possibileP);
		}*/
		
		
		
		
		// inutile ai fini dell'esercizio
		//List<String> direttiVicini = Graphs.neighborListOf(grafo, stato);
		
		// visita in ampiezza
		List<String> collegati = new ArrayList<String>();
		backVisit = new HashMap<>();

		GraphIterator<String, DefaultEdge> it = new BreadthFirstIterator<>(this.grafo, stato);
		
		//classe listener interna a modello (questa classe)
		it.addTraversalListener(new Model.EdgeTraversedGraphListener());
		
		backVisit.put(stato, null); //devo inserire manualmente il punto di partenza (che non ha padre -> null)

		while (it.hasNext())
		{
			collegati.add(it.next());
		}
		
		System.out.println("Vertici raggiungibili: " + collegati);
	}

	public List<String> getPredecessori(String stato)
	{
		predecessori = Graphs.predecessorListOf(this.grafo, stato);
		return predecessori;
	}

	

	public List<String> getSuccessori(String stato)
	{
		successori = Graphs.successorListOf(this.grafo, stato);
		return successori;
	}

	// PUNTO 2: utilizzo strutture dati precedenti
	
	private List<String> soluzioneBest;
	private int nPassiBest;
	
	public void calcolaSequenza(String statoPartenza)
	{
		//inizializzo 
		soluzioneBest = new ArrayList<String>();
		nPassiBest = 0;
		
		List<String>parziale = new LinkedList<String>();
		parziale.add(statoPartenza);
		ricorsione(parziale);
	}
	
	//TODO va in loop comunque
	// livello rappresenta lo stato che sto studiando
	public void ricorsione(List<String>parziale)
	{
		List<String>candidati = this.getSuccessori(parziale.get(parziale.size()-1)); //prendo i successori dell'ultimo elemento della sol parz
		// non c'è terminazione
		for (String succ : candidati)
		{
			//se aggiunta valida
			if (!parziale.contains(succ))
			{
				parziale.add(succ);
				ricorsione(parziale);
				parziale.remove(parziale.size()-1); //backtrack
			}
			//la soluzione che sto studiando è migliore della best?
			if (parziale.size() > nPassiBest)
			{
				//aggiorno best
				soluzioneBest.clear();
				soluzioneBest.addAll(parziale);
				nPassiBest = parziale.size();
			}
		}
			
	}

}
