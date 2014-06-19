package org.deri.voider.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

public class ResourcesNode extends Node{

	private Set<String> resources;
	
	public ResourcesNode(){
		super();
		resources = new HashSet<String>();
	}
	
	public ResourcesNode(String... uris){
		super();
		resources= new HashSet<String>(Arrays.asList(uris));
	}
	
	public ResourcesNode(Set<String> uris){
		super();
		resources= uris;
	}
	
	public Set<String> getResources() {
		return resources;
	}
	
	public void write(JSONWriter writer, PrefixManager prefixManager) throws JSONException {
		writer.object();
		writer.key("type"); writer.value("resources");
		writer.key("uris"); 
		writer.array();
		for(String uri:resources){
			writer.object();
			writer.key("uri"); writer.value(uri);
			writer.key("curie"); writer.value(prefixManager.getCurie(uri));
			writer.endObject();
		}
		writer.endArray();
		//neighbours
		writer.key("neighbours");
		writer.array();
		for(Edge e:this.neighbours){
			e.write(writer, prefixManager);
		}
		writer.endArray();
		writer.endObject();
	}

	@Override
	public int hashCode() {
		return resources.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(! obj.getClass().equals(this.getClass())) return false;
		
		return this.getResources().equals(((ResourcesNode)obj).getResources());
	}
}
