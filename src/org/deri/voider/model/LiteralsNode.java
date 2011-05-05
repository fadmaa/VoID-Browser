package org.deri.voider.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

public class LiteralsNode extends Node{
	private Set<String> literals;
	
	public LiteralsNode(){
		super();
		literals = new HashSet<String>();
	}
	
	public LiteralsNode(String... ls){
		super();
		literals = new HashSet<String>(Arrays.asList(ls));
	}
	
	public LiteralsNode(Set<String> ls){
		super();
		literals = ls;
	}

	public void write(JSONWriter writer, PrefixManager prefixManager) throws JSONException {
		writer.object();
		writer.key("type"); writer.value("literals");
		writer.key("literals"); 
		writer.array();
		for (String l:literals){
			writer.value(l);
		}
		writer.endArray();
		writer.endObject();
	}

	public Set<String> getLiterals() {
		return literals;
	}

	@Override
	public int hashCode() {
		return literals.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(! obj.getClass().equals(this.getClass())) return false;
		
		return this.getLiterals().equals(((LiteralsNode)obj).getLiterals());
	}
	
	
	
}
