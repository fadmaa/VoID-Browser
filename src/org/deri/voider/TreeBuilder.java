package org.deri.voider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.voider.model.AnnotatedSet;
import org.deri.voider.model.Edge;
import org.deri.voider.model.LiteralsNode;
import org.deri.voider.model.MixedNode;
import org.deri.voider.model.Node;
import org.deri.voider.model.ResourcesNode;
import org.deri.voider.sparql.SparqlEndpointProxy;

import com.ibm.icu.util.StringTokenizer;

public class TreeBuilder {
	private static final Logger logger = Logger.getLogger("org.deri.voider.TreeBuilder");
	private SparqlEndpointProxy sparqlEndpoint;
	
	public TreeBuilder(SparqlEndpointProxy sparqlProxy){
		sparqlEndpoint = sparqlProxy;
	}
	
	public ResourcesNode tree(String startingUris,int limit){
		long start = System.currentTimeMillis();
		//initiailization
		ResourcesNode startingNode = new ResourcesNode(expand(startingUris));
		Queue<WeightedNode> queue = new LinkedList<TreeBuilder.WeightedNode>();
		int depth = 1;
		queue.offer(new WeightedNode(startingNode,depth));
		
		int groupSize = 20;
		
		while(!queue.isEmpty()){
			WeightedNode wn = queue.poll();
			depth = wn.weight;
			if(depth==limit){
				break;
			}
			ResourcesNode n = wn.node;
			Map<String, AnnotatedSet> map = new HashMap<String, AnnotatedSet>();
			for(String uri:n.getResources()){
				Set<String> props = sparqlEndpoint.getAdjacentProperties(uri);
				
				//prepare structure for bulk-querying
				int i = 0;
				String[] propertiesGroup = new String[groupSize];
				for(String p:props){
					if(i<groupSize){
						propertiesGroup[i] = p;
						i +=1;
						continue;
					}
					//we have collected enough properties let's query now
					addAnnotatedSets(map,sparqlEndpoint.getValuesForSeveralProperties(n.getResources(), propertiesGroup,i));
					
					//reset counters
					i = 0;
					propertiesGroup = new String[groupSize];
				}
				//now, any left properties that haven't been queried yet
				if(i>0){
					addAnnotatedSets(map,sparqlEndpoint.getValuesForSeveralProperties(n.getResources(), propertiesGroup,i));
				}
			}
			for(Entry<String, AnnotatedSet> entry:map.entrySet()){
				Node o = getNode(entry.getValue());
				String prop = entry.getKey();
				if(o==null){
					continue;
				}
				n.addNeighbour(prop, o);
				//TODO ignoring MixedNode
				//FIXME ignoring MixedNode
				if(o instanceof ResourcesNode){
					queue.offer(new WeightedNode((ResourcesNode)o, depth+1));
				}
			}
		}
		long end = System.currentTimeMillis();
		logger.debug("Tree building took " + (end-start) + "milli seconds");
		return startingNode;
	}

	private Node getNode(AnnotatedSet set) {
		switch(set.getType()){
			case AnnotatedSet.RESOURCES:{
				return new ResourcesNode(set.getValues());
			}
			case AnnotatedSet.LITERALS:{
				return new LiteralsNode(set.getValues());				
			}
			case AnnotatedSet.MIXED:{
				return new MixedNode(set.getValues());				
			}
		}
		throw new RuntimeException("node has no type " + set.getValues().toString());
	}

	//TODO parameterize this method <T>
	public Node reduce(Node node){
		if(node instanceof LiteralsNode || node instanceof MixedNode){
			return node;
		}
		ResourcesNode root = (ResourcesNode) node;
		ResourcesNode newRoot = new ResourcesNode(root.getResources());
		Map<Node, Set<String>> map = new HashMap<Node, Set<String>>();
		
		for(Edge e:root.getNeighbours()){
			if(map.containsKey(e.getTarget())){
				map.get(e.getTarget()).addAll(e.getPropertyUris());
			}else{
				map.put(e.getTarget(), new HashSet<String>(e.getPropertyUris()));
			}
		}
		
		for(Entry<Node, Set<String>> entry:map.entrySet()){
			newRoot.addNeighbour(entry.getValue(), reduce(entry.getKey()));
		}
		return newRoot;
	}
	
	static class WeightedNode{
		ResourcesNode node;
		int weight;
		public WeightedNode(ResourcesNode n, int w) {
			this.node = n;
			this.weight = w;
		}
	}
	
	private void addAnnotatedSets(Map<String, AnnotatedSet> map, Map<String, AnnotatedSet> toAdd){
		for(Entry<String, AnnotatedSet> entry:toAdd.entrySet()){
			if(! entry.getValue().isEmpty()){
				if(map.containsKey(entry.getKey())){
					map.get(entry.getKey()).addAll(entry.getValue());
				}else{
					map.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	private Set<String> expand(String s){
		Set<String> uris = new HashSet<String>();
		StringTokenizer tokenizer = new StringTokenizer(s,"+");
		while(tokenizer.hasMoreTokens()){
			String uri = tokenizer.nextToken();
			if(uri.isEmpty()){
				continue;
			}
			uris.add(uri);
		}
		return uris;
	}
}
