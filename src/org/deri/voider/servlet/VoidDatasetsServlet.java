package org.deri.voider.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.voider.sparql.tagcloud.VoidQuerier;
import org.deri.voider.sparql.tagcloud.model.Dataset;
import org.json.JSONException;
import org.json.JSONWriter;

@SuppressWarnings("serial")
public class VoidDatasetsServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String voidUrl = req.getParameter("voidUrl");
		VoidQuerier querier = new VoidQuerier();
		Set<Dataset> datasets = querier.datasets(voidUrl);

		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Content-Type", "application/json");
		Writer w = resp.getWriter();
		JSONWriter writer = new JSONWriter(w);
		try {
			writer.object();
			writer.key("code");
			writer.value("ok");
			writer.key("datasets");
			writer.array();
			for (Dataset dataset : datasets) {
				writer.object();
				writer.key("uri"); writer.value(dataset.getUri());
				writer.key("title"); writer.value(dataset.getTitle());
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

}
