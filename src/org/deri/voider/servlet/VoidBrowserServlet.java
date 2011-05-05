package org.deri.voider.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.management.RuntimeErrorException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.voider.TreeBuilder;
import org.deri.voider.model.ResourcesNode;
import org.deri.voider.sparql.SparqlEndpointProxy;
import org.deri.voider.sparql.SparqlEndpointProxyImpl;
import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class VoidBrowserServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			int depth;
			try {
				depth = Integer.parseInt(req.getParameter("depth"));
			} catch (NumberFormatException ne) {
				depth = 3;
			}
			if (depth > 4) {
				throw new RuntimeException("Depth cannot be more than 4");
			}
			String voidUri = req.getParameter("void");
			Model model = ModelFactory.createDefaultModel();
			model.read(voidUri);

			Resource ds = model.getResource(voidUri);
			Set<String> uris = new HashSet<String>();
			// get SPARQL endpoint
			Property p = model
					.getProperty("http://rdfs.org/ns/void#sparqlEndpoint");
			if(p==null){
				throw new RuntimeException(voidUri  + " does not have a SPARQL endpoint ");
			}
			String sparql = ds.getProperty(p).getObject().asResource().getURI();
			// get all root resources
			p = model.getProperty("http://rdfs.org/ns/void#rootResource");
			if (p != null) {
				StmtIterator iter = model.listStatements(ds, p, (RDFNode) null);
				while (iter.hasNext()) {
					uris.add(iter.nextStatement().getObject().asResource()
							.getURI());
				}
			}
			// get all example resources
			p = model.getProperty("http://rdfs.org/ns/void#exampleResource");
			if (p != null) {
				StmtIterator iter = model.listStatements(ds, p, (RDFNode) null);
				while (iter.hasNext()) {
					uris.add(iter.nextStatement().getObject().asResource()
							.getURI());
				}
			}
			// group by type
			// TODO
			// draw
			SparqlEndpointProxy proxy = new SparqlEndpointProxyImpl(sparql);
			TreeBuilder builder = new TreeBuilder(proxy);
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Content-Type", "application/json");
			Writer w = resp.getWriter();
			JSONWriter writer = new JSONWriter(w);
			InputStream in = this.getClass().getResourceAsStream(
					"/files/prefixes");
			PrefixManager prefixManager = new PrefixManager(in);
			writer.object();
			writer.key("rootResources");
			writer.array();
			for (String uri : uris) {
				ResourcesNode tree = builder.tree(uri, depth);
				tree = (ResourcesNode) builder.reduce(tree);
				// response
				tree.write(writer, prefixManager);
			}
			writer.endArray();
			writer.endObject();
			w.flush();
			w.close();
		} catch (Exception e) {
			Writer w = resp.getWriter();
			JSONWriter writer = new JSONWriter(w);
			try {
				writer.object();
				writer.key("code");
				writer.value("error");
				writer.key("msg");
				writer.value(e.getMessage());
				writer.endObject();
			} catch (JSONException ex) {
				throw new IOException(ex);
			}
		}
	}

}
