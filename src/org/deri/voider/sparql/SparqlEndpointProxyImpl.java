package org.deri.voider.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
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

	public Set<String> getAdjacentProperties(String resource) throws Exception {
		String sparql = "SELECT DISTINCT ?p WHERE{<" + resource + "> ?p ?o.}";
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		ResultSet res = execSelect(sparql);
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
	public Set<String> resourcesOfType(String typeUri, int limit)
			throws Exception {
		Set<String> resources = new HashSet<String>();
		String sparql = "SELECT DISTINCT ?s WHERE { ?s a <" + typeUri
				+ "> } LIMIT " + limit;
		ResultSet res = execSelect(sparql);
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			resources.add(sol.getResource("s").getURI());
		}
		return resources;
	}

	@Override
	public Set<String> resources(Set<ClassPartition> classes, int limit)
			throws Exception {
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
		ResultSet res = execSelect(buffer.toString());
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			resources.add(sol.getResource("s").getURI());
		}
		return limit < resources.size() ? sample(resources, limit)
				: new HashSet<String>(resources);
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

	public String getResource(String typeUri) throws Exception {
		String sparql = "SELECT ?s WHERE { ?s a <" + typeUri + ">} LIMIT 1";
		ResultSet res = execSelect(sparql);
		String resource = null;
		if (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			resource = sol.getResource("s").getURI();
		}
		return resource;
	}

	public Node getValues(Set<String> resources, String property)
			throws Exception {
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
		ResultSet res = execSelect(sparql);
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
	public Set<ClassPartition> classes(int limit) throws Exception {
		logger.debug("Executing \n" + TYPES_WITH_COUNTS_SPARQL);
		long start = System.currentTimeMillis();
		ResultSet res = execSelect(TYPES_WITH_COUNTS_SPARQL + limit);
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
			Set<String> resources, String[] properties, int num)
			throws Exception {
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
		ResultSet res = execSelect(sparql);
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

	public Map<String, AnnotatedSet> getNeighbours(Set<String> resources)
			throws Exception {
		// TODO remove this
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		StringBuilder builder = new StringBuilder(
				"SELECT ?p ?o WHERE  { ?s ?p ?o . FILTER (?s IN (");
		for (String s : resources) {
			builder.append("<").append(s).append(">, ");
		}
		builder.setLength(builder.length() - 2);
		builder.append("))}");
		String sparql = builder.toString();
		logger.debug("Executing \n" + sparql);
		long start = System.currentTimeMillis();
		ResultSet res = execSelect(sparql);
		long end = System.currentTimeMillis();
		logger.debug("It took " + (end - start) + " milli second");
		// collect properties values in a map. key is the property. value is a
		// pair of sets one for URIs and the other is for literals
		Map<String, AnnotatedSet> map = new HashMap<String, AnnotatedSet>();
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			RDFNode o = sol.get("o");
			String p = sol.getResource("p").getURI();
			if (o.canAs(Literal.class)) {
				if (map.containsKey(p)) {
					map.get(p).add(o.asLiteral().getString());
					map.get(p).setType(AnnotatedSet.LITERALS);
				} else {
					AnnotatedSet set = new AnnotatedSet(AnnotatedSet.LITERALS, NODE_SIZE_LIMIT);
					set.add(o.asLiteral().getString());
					map.put(p, set);
				}
			} else if (o.canAs(Resource.class)) {
				String rUri = o.asResource().getURI();
				// TODO I am ignoring blank nodes
				if (rUri != null) {
					if (map.containsKey(p)) {
						map.get(p).add(rUri);
						map.get(p).setType(AnnotatedSet.RESOURCES);
					} else {
						AnnotatedSet set = new AnnotatedSet(
								AnnotatedSet.RESOURCES, NODE_SIZE_LIMIT);
						set.add(rUri);
						map.put(p, set);
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

	private ResultSet execSelect(String sparql) throws MalformedURLException,
			UnsupportedEncodingException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(endpointUri + "?query="+URLEncoder.encode(sparql, "UTF-8"))
				.openConnection();
		//connection.setDoInput(true);
		//connection.setDoOutput(true);

		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept",
				"application/sparql-results+xml");
		InputStream response = connection.getInputStream();
		return ResultSetFactory.fromXML(response);
	}

	private final static String TYPES_WITH_COUNTS_SPARQL = "SELECT ?c (COUNT(?s) AS ?count) WHERE {?s a ?c } GROUP BY ?c ORDER BY DESC(?count) LIMIT ";
	private final static int NODE_SIZE_LIMIT = 12; 
}
