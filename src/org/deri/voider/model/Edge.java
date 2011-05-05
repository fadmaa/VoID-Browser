package org.deri.voider.model;

import java.util.HashSet;
import java.util.Set;

import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

public class Edge implements Jsonizable{
	private Set<String> propertyUris;
	private Node target;

	public Edge(String p, Node n){
		this.propertyUris = new HashSet<String>();
		this.propertyUris.add(p);
		this.target = n;
	}
	
	public Edge(Set<String> ps, Node n){
		this.propertyUris = ps;
		this.target = n;
	}
	
	public Set<String> getPropertyUris() {
		return propertyUris;
	}
	public Node getTarget() {
		return target;
	}

	public void write(JSONWriter writer, PrefixManager prefixManager) throws JSONException {
		writer.object();
		writer.key("properties");
		writer.array();
		for(String propertyUri:propertyUris){
			writer.object();
			writer.key("uri"); writer.value(propertyUri);
			writer.key("curie"); writer.value(prefixManager.getCurie(propertyUri));
			writer.endObject();
		}
		writer.endArray();
		writer.key("target"); target.write(writer,prefixManager);
		writer.endObject();
	}
	
}
