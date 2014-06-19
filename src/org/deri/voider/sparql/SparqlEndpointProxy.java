package org.deri.voider.sparql;

import java.util.Map;
import java.util.Set;

import org.deri.voider.model.AnnotatedSet;
import org.deri.voider.model.Node;

public interface SparqlEndpointProxy {

	public Set<String> getAdjacentProperties(String resource);
	public Node getValues(Set<String> resources, String property);
	public Map<String, AnnotatedSet> getValuesForSeveralProperties(Set<String> resources, String[] properties,int num);
	public String getResource(String typeUri);
}
