package org.deri.voider.sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.voider.model.AnnotatedSet;
import org.deri.voider.model.LiteralsNode;
import org.deri.voider.model.Node;
import org.deri.voider.model.ResourcesNode;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SparqlEndpointProxyImpl implements SparqlEndpointProxy{

	private static final Logger logger = Logger.getLogger("org.deri.voider.sparql.SparqlEndpointProxyImpl");
	private final String endpointUri;
	
	public SparqlEndpointProxyImpl(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	public Set<String> getAdjacentProperties(String resource) {
		String sparql = "SELECT DISTINCT ?p WHERE{<" +  resource + "> ?p ?o.}";
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		QueryExecution qExec= QueryExecutionFactory.sparqlService(endpointUri, sparql);
		ResultSet res = qExec.execSelect();
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end-start) + " milli second");
		Set<String> ps = new HashSet<String>();
		while(res.hasNext()){
			QuerySolution sol = res.nextSolution();
			ps.add(sol.getResource("p").getURI());
		}
		return ps;
	}

	public String getResource(String typeUri){
		String sparql = "SELECT ?s WHERE { ?s a <" + typeUri + ">} LIMIT 1";
		QueryExecution qExec= QueryExecutionFactory.sparqlService(endpointUri, sparql);
		ResultSet res = qExec.execSelect();
		String resource = null;
		if(res.hasNext()){
			QuerySolution sol = res.nextSolution();
			resource = sol.getResource("s").getURI();
		}
		return resource;
	}
	
	public Node getValues(Set<String> resources, String property) {
		//TODO remove this
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		

		String sparql = "SELECT DISTINCT ?o WHERE {?r <" + property  + "> ?o. FILTER (" + getOrClause("r",resources) + ") }";
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri, sparql);
		ResultSet res= qExec.execSelect();
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end-start) + " milli second");
		Set<String> uris = new HashSet<String>();
		Set<String> literals = new HashSet<String>();
		while(res.hasNext()){
			QuerySolution sol= res.nextSolution();
			RDFNode o= sol.get("o");
			if(o.canAs(Literal.class)){
				literals.add(o.asLiteral().getString());
			}else if(o.canAs(Resource.class)){
				String rUri = o.asResource().getURI();
				//TODO I am ignoring blank nodes
				if(rUri!=null) uris.add(rUri);
			}
		  }
		  if(! literals.isEmpty()){
		 	  return new LiteralsNode(literals);
		  }else if(! uris.isEmpty()){
		 	  return new ResourcesNode(uris);
		  }else{
			  return null;
		  }
	}

	
	public Map<String, AnnotatedSet> getValuesForSeveralProperties(Set<String> resources, String[] properties,int num) {
		//TODO remove this
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		String objectVarname = "o";
		String subjectVarname = "r";
		String selectClause = getSelectClause(objectVarname, num);
		StringBuilder builder = new StringBuilder(selectClause);
		builder.append(" WHERE {");
		for(int i=0;i<num;i++){
			String p = properties[i];
			builder.append(getPropertyPattern(subjectVarname, p, objectVarname, i));
		}
		builder.append(" FILTER (").append(getOrClause(subjectVarname, resources)).append(") }");
		
		String sparql = builder.toString();
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri, sparql);
		ResultSet res= qExec.execSelect();
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end-start) + " milli second");
		//collect properties values in a map. key is the property. value is a pair of sets one for URIs and the other is for literals
		Map<String, AnnotatedSet> map = new HashMap<String, AnnotatedSet>();
		while(res.hasNext()){
			QuerySolution sol= res.nextSolution();
			for(int i=0;i<num;i++){
				RDFNode o= sol.get(objectVarname+String.valueOf(i));
				if(o.canAs(Literal.class)){
					if(map.containsKey(properties[i])){
						map.get(properties[i]).add(o.asLiteral().getString());
						map.get(properties[i]).setType(AnnotatedSet.LITERALS);
					}else{
						AnnotatedSet set = new AnnotatedSet(AnnotatedSet.LITERALS);
						set.add(o.asLiteral().getString());
						map.put(properties[i], set);
					}
				}else if(o.canAs(Resource.class)){
					String rUri = o.asResource().getURI();
					//TODO I am ignoring blank nodes
					if(rUri!=null){
						if(map.containsKey(properties[i])){
							map.get(properties[i]).add(rUri);
							map.get(properties[i]).setType(AnnotatedSet.RESOURCES);
						}else{
							AnnotatedSet set = new AnnotatedSet(AnnotatedSet.RESOURCES);
							set.add(rUri);
							map.put(properties[i], set);
						}
					}
				}
			}
		  }
		return map;
	}

	private Object getPropertyPattern(String subjectVarname, String p, String objectVarname, int i) {
		return "?" + subjectVarname + " <" + p + "> ?" + objectVarname + String.valueOf(i) + ". ";
	}

	private String getOrClause(String varname, Set<String> resources) {
		StringBuilder builder = new StringBuilder();
		for(String r:resources){
			builder.append("?").append(varname).append(" = <").append(r).append("> || ");
		}
		//get rid of the last ||
		return builder.substring(0,builder.length()-4);
	}
	
	private String getSelectClause(String varname, int num){
		StringBuilder builder = new StringBuilder("SELECT ");
		String v = " ?" + varname;
		for(int i=0;i<num;i++){
			builder.append(v).append(String.valueOf(i));
		}
		return builder.toString();
	}
}
