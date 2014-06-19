package org.deri.voider.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.voider.TreeBuilder;
import org.deri.voider.model.ResourcesNode;
import org.deri.voider.sparql.SparqlEndpointProxy;
import org.deri.voider.sparql.SparqlEndpointProxyImpl;
import org.deri.voider.sparql.tagcloud.VoidQuerier;
import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

@SuppressWarnings("serial")
public class InstanceOfTypeServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String voidUrl = req.getParameter("voidUrl");
		String typeUri = req.getParameter("typeUri");
		String dataset = req.getParameter("dataset");
		// TODO read as parameter
		int depth = 3;
		VoidQuerier querier = new VoidQuerier();
		String sparqlEndpoint = querier.getSparqlEndpointUrl(voidUrl, dataset);
		//TODO
		if(sparqlEndpoint == null){
			sparqlEndpoint = "http://worldbank.270a.info/sparql";
		}
		try {
			SparqlEndpointProxy proxy = new SparqlEndpointProxyImpl(
					sparqlEndpoint);
			String uri = proxy.getResource(typeUri);
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
			ResourcesNode tree = builder.tree(uri, depth);
			tree = (ResourcesNode) builder.reduce(tree);
			// response
			tree.write(writer, prefixManager);
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
