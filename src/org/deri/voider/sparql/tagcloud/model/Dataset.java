package org.deri.voider.sparql.tagcloud.model;

public class Dataset {
	private final String uri;
	private final String title;
	
	public Dataset(String uri, String title) {
		this.uri = uri;
		this.title = title;
	}
	public String getUri() {
		return uri;
	}
	public String getTitle() {
		return title;
	}
}
