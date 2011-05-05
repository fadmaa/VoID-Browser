package org.deri.voider.model;

import java.util.HashSet;
import java.util.Set;

public abstract class Node implements Jsonizable{

	protected Set<Edge> neighbours;
	
	public Node(){
		neighbours = new HashSet<Edge>();
	}
	
	public Set<Edge> getNeighbours(){
		return neighbours;
	}
	public void addNeighbour(String p, Node target){
		this.neighbours.add(new Edge(p,target));
	}
	public void addNeighbour(Set<String> ps, Node target){
		this.neighbours.add(new Edge(ps,target));
	}
}
