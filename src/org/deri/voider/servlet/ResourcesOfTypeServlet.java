package org.deri.voider.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.voider.sparql.SparqlEndpointProxy;
import org.deri.voider.sparql.SparqlEndpointProxyImpl;
import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

@SuppressWarnings("serial")
public class ResourcesOfTypeServlet extends HttpServlet{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String sparqlEndpoint = req.getParameter("sparql");
		String typeUri = req.getParameter("type");
		SparqlEndpointProxy endpoint = new SparqlEndpointProxyImpl(sparqlEndpoint);
		
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Content-Type", "application/json");
		Writer w = resp.getWriter();
		JSONWriter writer = new JSONWriter(w);
		try {
			Set<String> resources = endpoint.resourcesOfType(typeUri, LIMIT_RESOURCES);
			
			InputStream in = this.getClass().getResourceAsStream("/files/prefixes");
			PrefixManager prefixManager = new PrefixManager(in);
			
			writer.object();
			writer.key("code");
			writer.value("ok");
			writer.key("resources");
			writer.array();
			for(String r:resources){
				writer.object();
				writer.key("uri"); writer.value(r);
				writer.key("curie"); writer.value(prefixManager.getCurie(r));
				writer.endObject();
			}
			writer.endArray();
			writer.endObject();
			w.close();
		} catch (Exception e) {
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

	private static final int LIMIT_RESOURCES = 10;

}
