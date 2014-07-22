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
import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

@SuppressWarnings("serial")
public class StructureServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try{
			String s = req.getParameter("sparql");
			String r = req.getParameter("resource");
			int depth = Integer.parseInt(req.getParameter("depth"));
			if(depth>4){
				throw new RuntimeException("Depth cannot be more than 4");
			}
			SparqlEndpointProxy proxy = new SparqlEndpointProxyImpl(s);
			TreeBuilder builder = new TreeBuilder(proxy);
			ResourcesNode tree = builder.tree(r,depth);
			tree = (ResourcesNode)builder.reduce(tree);
			//response
			resp.setCharacterEncoding("UTF-8");
			resp.setHeader("Content-Type", "application/json");

			Writer w = resp.getWriter();
			JSONWriter writer = new JSONWriter(w);

			try {
				InputStream in = this.getClass().getResourceAsStream("/files/prefixes");
				PrefixManager prefixManager = new PrefixManager(in);
				writer.object();
				writer.key("rootResources");
				writer.array();
				tree.write(writer,prefixManager);
				writer.endArray();
				writer.endObject();
			} catch (JSONException e) {
				throw new IOException(e);
			}
			w.flush();
			w.close();
		}catch(Exception e){
			Writer w = resp.getWriter();
			JSONWriter writer = new JSONWriter(w);
			try{
				writer.object();
				writer.key("code"); writer.value("error");
				writer.key("msg"); writer.value(e.getMessage());
				writer.endObject();
			} catch (JSONException ex) {
				throw new IOException(ex);
			}
		}
	}
	
}
