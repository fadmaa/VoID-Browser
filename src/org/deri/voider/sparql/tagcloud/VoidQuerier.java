package org.deri.voider.sparql.tagcloud;

import java.util.HashSet;
import java.util.Set;

import org.deri.voider.sparql.tagcloud.model.ClassPartition;
import org.deri.voider.sparql.tagcloud.model.Dataset;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class VoidQuerier {

	public Set<Dataset> datasets(String voidUrl){
		Set<Dataset> datasets = new HashSet<Dataset>();
		
		Model model = ModelFactory.createDefaultModel();
		model.read(voidUrl);
		QueryExecution qExec = QueryExecutionFactory.create(VOID_DATASETS_QUERY, model);
		ResultSet res = qExec.execSelect();
		while(res.hasNext()){
			QuerySolution sol = res.nextSolution();
			Literal title = sol.getLiteral("t");
			String uri = sol.getResource("d").getURI();
			if(title==null){
				datasets.add(new Dataset(uri, uri));
			} else {
				datasets.add(new Dataset(uri, title.getString()));
			}
		}
		return datasets;
	}
	/**
	 * @param voidUrl
	 * @return a map from classes to their counts
	 */
	public Set<ClassPartition> classes(String voidUrl, String datasetUri){
		Set<ClassPartition> classes = new HashSet<ClassPartition>();
		Model model = ModelFactory.createDefaultModel();
		model.read(voidUrl);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("d", model.getResource(datasetUri));
		QueryExecution qExec = QueryExecutionFactory.create(VOID_CLASS_COUNT_QUERY, model, initialBindings);
		ResultSet res = qExec.execSelect();
		while(res.hasNext()){
			QuerySolution sol = res.nextSolution();
			classes.add(new ClassPartition(sol.getResource("class").getURI(), sol.getLiteral("count").getLong()));
		}
		return classes;
	}
	
	public String getSparqlEndpointUrl(String voidUrl, String datasetUri){
		Model model = ModelFactory.createDefaultModel();
		model.read(voidUrl);
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		initialBindings.add("d", model.getResource(datasetUri));
		QueryExecution qExec = QueryExecutionFactory.create(VOID_SPARQL_ENDPOINT_QUERY, model, initialBindings);
		ResultSet res = qExec.execSelect();
		String endpoint = null;
		if(res.hasNext()){
			QuerySolution sol = res.nextSolution();
			endpoint = sol.getResource("ep").getURI();
		}
		return endpoint;
	}
	
	private static final String VOID_CLASS_COUNT_QUERY = 
			"PREFIX void:<http://rdfs.org/ns/void#> " +
			"SELECT ?class ?count " +
			"WHERE{ " +
			   "?d void:classPartition ?part . " +
			   "?part void:class ?class; void:entities ?count" +
			"}"
			;
	
	private static final String VOID_DATASETS_QUERY = 
			"PREFIX void:<http://rdfs.org/ns/void#> " +
			"PREFIX dct:<http://purl.org/dc/terms/> " +
			"SELECT ?d ?t " +
			"WHERE{ " +
			   "?d a void:Dataset . OPTIONAL {?d dct:title ?t}" +
			"}"
			;

	private static final String VOID_SPARQL_ENDPOINT_QUERY = 
			"PREFIX void:<http://rdfs.org/ns/void#> " +
					"SELECT ?class ?count " +
					"WHERE{ " +
					   "?d void:sparqlEndpoint ?ep . " +
					"}"
					;
}
