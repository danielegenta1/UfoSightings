package it.polito.tdp.ufo.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.polito.tdp.ufo.db.SightingsDAO;

public class Model 
{
	private SimpleDirectedGraph<String, DefaultEdge> grafo;
	
	private List<Sighting> avvistamenti;

	
	private SightingsDAO dao;
	
	public Model()
	{
		avvistamenti = new ArrayList<Sighting>();
		dao = new SightingsDAO();
		avvistamenti = dao.getSightings();
		
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

	public void creaGrafo(int anno) 
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
		System.out.println(grafo.edgeSet().size());
	}

}
