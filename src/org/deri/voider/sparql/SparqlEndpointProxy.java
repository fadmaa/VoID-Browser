package org.deri.voider.sparql;

import java.util.Map;
import java.util.Set;

import org.deri.voider.model.AnnotatedSet;
import org.deri.voider.model.Node;
import org.deri.voider.sparql.tagcloud.model.ClassPartition;

public interface SparqlEndpointProxy {

	public Set<String> getAdjacentProperties(String resource) throws Exception;
	public Node getValues(Set<String> resources, String property)throws Exception;
	public Map<String, AnnotatedSet> getValuesForSeveralProperties(Set<String> resources, String[] properties,int num)throws Exception;
	public Map<String, AnnotatedSet> getNeighbours(Set<String> resources)throws Exception;
	public String getResource(String typeUri)throws Exception;
	public Set<ClassPartition> classes(int limit)throws Exception;
	public Set<String> resources(Set<ClassPartition> classes, int limit)throws Exception;
	public Set<String> resourcesOfType(String typeUri, int limit)throws Exception;
}
