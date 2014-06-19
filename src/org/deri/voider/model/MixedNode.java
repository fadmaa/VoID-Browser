package org.deri.voider.model;

import java.util.Set;

import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

public class MixedNode extends Node{
	private Set<String> values;

	public MixedNode(Set<String> vs){
		super();
		values = vs;
	}
	
	public void write(JSONWriter writer, PrefixManager prefixManager)
			throws JSONException {
		writer.object();
		writer.key("type"); writer.value("mixed");
		writer.key("values");
		writer.array();
		for (String v:values){
			writer.value(v);
		}
		writer.endArray();
		writer.endObject();
	}
	
}
