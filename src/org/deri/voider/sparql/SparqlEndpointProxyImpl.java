package org.deri.voider.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.voider.model.AnnotatedSet;
import org.deri.voider.model.LiteralsNode;
import org.deri.voider.model.Node;
import org.deri.voider.model.ResourcesNode;
import org.deri.voider.sparql.tagcloud.model.ClassPartition;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SparqlEndpointProxyImpl implements SparqlEndpointProxy {

	private static final Logger logger = Logger
			.getLogger("org.deri.voider.sparql.SparqlEndpointProxyImpl");
	private final String endpointUri;

	public SparqlEndpointProxyImpl(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	public Set<String> getAdjacentProperties(String resource) {
		String sparql = "SELECT DISTINCT ?p WHERE{<" + resource + "> ?p ?o.}";
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri,
				sparql);
		ResultSet res = qExec.execSelect();
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end - start) + " milli second");
		Set<String> ps = new HashSet<String>();
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			ps.add(sol.getResource("p").getURI());
		}
		return ps;
	}

	
	@Override
	public Set<String> resourcesOfType(String typeUri, int limit) {
		Set<String> resources = new HashSet<String>();
		String sparql = "SELECT DISTINCT ?s WHERE { ?s a <" + typeUri + "> } LIMIT " + limit;
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri, sparql);
		ResultSet res = qExec.execSelect();
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			resources.add(sol.getResource("s").getURI());
		}
		return resources;
	}

	@Override
	public Set<String> resources(Set<ClassPartition> classes, int limit) {
		List<String> resources = new ArrayList<String>(limit + 10);
		StringBuilder buffer = new StringBuilder();
		buffer.append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n SELECT ?s WHERE{ ");
		for (ClassPartition cp : classes) {
			buffer.append("{ SELECT ?s WHERE{ ?s rdf:type <")
					.append(cp.getClassUri()).append("> } LIMIT ")
					.append(limit).append(" OFFSET ").append(cp.getCount() / 3)
					.append(" } UNION ");
		}
		// get rid of the last union
		buffer.setLength(buffer.length() - 6);
		buffer.append("}");
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri,
				buffer.toString());
		ResultSet res = qExec.execSelect();
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			resources.add(sol.getResource("s").getURI());
		}
		return limit<resources.size()?sample(resources,limit):new HashSet<String>(resources);
	}

	private Set<String> sample(List<String> input, int subsetSize) {
		Random r = new Random();
		int inputSize = input.size();
		for (int i = 0; i < subsetSize; i++) {
			int indexToSwap = i + r.nextInt(inputSize - i);
			String temp = input.get(i);
			input.set(i, input.get(indexToSwap));
			input.set(indexToSwap, temp);
		}
		return new HashSet<String>(input.subList(0, subsetSize));
	}

	public String getResource(String typeUri) {
		String sparql = "SELECT ?s WHERE { ?s a <" + typeUri + ">} LIMIT 1";
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri,
				sparql);
		ResultSet res = qExec.execSelect();
		String resource = null;
		if (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			resource = sol.getResource("s").getURI();
		}
		return resource;
	}

	public Node getValues(Set<String> resources, String property) {
		// TODO remove this
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		String sparql = "SELECT DISTINCT ?o WHERE {?r <" + property
				+ "> ?o. FILTER (" + getOrClause("r", resources) + ") }";
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri,
				sparql);
		ResultSet res = qExec.execSelect();
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end - start) + " milli second");
		Set<String> uris = new HashSet<String>();
		Set<String> literals = new HashSet<String>();
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			RDFNode o = sol.get("o");
			if (o.canAs(Literal.class)) {
				literals.add(o.asLiteral().getString());
			} else if (o.canAs(Resource.class)) {
				String rUri = o.asResource().getURI();
				// TODO I am ignoring blank nodes
				if (rUri != null)
					uris.add(rUri);
			}
		}
		if (!literals.isEmpty()) {
			return new LiteralsNode(literals);
		} else if (!uris.isEmpty()) {
			return new ResourcesNode(uris);
		} else {
			return null;
		}
	}

	@Override
	public Set<ClassPartition> classes(int limit) {
		logger.debug("Executing \n" + TYPES_WITH_COUNTS_SPARQL);
		long start = System.currentTimeMillis();
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri,
				TYPES_WITH_COUNTS_SPARQL + limit);
		ResultSet res = qExec.execSelect();
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end - start) + " milli second");
		Set<ClassPartition> classes = new HashSet<ClassPartition>();
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			classes.add(new ClassPartition(sol.getResource("c").getURI(), sol
					.getLiteral("count").getLong()));
		}
		return classes;
	}

	public Map<String, AnnotatedSet> getValuesForSeveralProperties(
			Set<String> resources, String[] properties, int num) {
		// TODO remove this
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
		for (int i = 0; i < num; i++) {
			String p = properties[i];
			builder.append(getPropertyPattern(subjectVarname, p, objectVarname,
					i));
		}
		builder.append(" FILTER (")
				.append(getOrClause(subjectVarname, resources)).append(") }");

		String sparql = builder.toString();
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointUri,
				sparql);
		ResultSet res = qExec.execSelect();
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end - start) + " milli second");
		// collect properties values in a map. key is the property. value is a
		// pair of sets one for URIs and the other is for literals
		Map<String, AnnotatedSet> map = new HashMap<String, AnnotatedSet>();
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			for (int i = 0; i < num; i++) {
				RDFNode o = sol.get(objectVarname + String.valueOf(i));
				if (o.canAs(Literal.class)) {
					if (map.containsKey(properties[i])) {
						map.get(properties[i]).add(o.asLiteral().getString());
						map.get(properties[i]).setType(AnnotatedSet.LITERALS);
					} else {
						AnnotatedSet set = new AnnotatedSet(
								AnnotatedSet.LITERALS);
						set.add(o.asLiteral().getString());
						map.put(properties[i], set);
					}
				} else if (o.canAs(Resource.class)) {
					String rUri = o.asResource().getURI();
					// TODO I am ignoring blank nodes
					if (rUri != null) {
						if (map.containsKey(properties[i])) {
							map.get(properties[i]).add(rUri);
							map.get(properties[i]).setType(
									AnnotatedSet.RESOURCES);
						} else {
							AnnotatedSet set = new AnnotatedSet(
									AnnotatedSet.RESOURCES);
							set.add(rUri);
							map.put(properties[i], set);
						}
					}
				}
			}
		}
		return map;
	}

	private Object getPropertyPattern(String subjectVarname, String p,
			String objectVarname, int i) {
		return "?" + subjectVarname + " <" + p + "> ?" + objectVarname
				+ String.valueOf(i) + ". ";
	}

	private String getOrClause(String varname, Set<String> resources) {
		StringBuilder builder = new StringBuilder();
		for (String r : resources) {
			builder.append("?").append(varname).append(" = <").append(r)
					.append("> || ");
		}
		// get rid of the last ||
		return builder.substring(0, builder.length() - 4);
	}

	private String getSelectClause(String varname, int num) {
		StringBuilder builder = new StringBuilder("SELECT ");
		String v = " ?" + varname;
		for (int i = 0; i < num; i++) {
			builder.append(v).append(String.valueOf(i));
		}
		return builder.toString();
	}

	private final String TYPES_WITH_COUNTS_SPARQL = "SELECT ?c (COUNT(?s) AS ?count) WHERE {?s a ?c } GROUP BY ?c ORDER BY DESC(?count) LIMIT ";
}
