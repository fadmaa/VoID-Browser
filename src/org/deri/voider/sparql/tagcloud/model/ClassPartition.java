package org.deri.voider.sparql.tagcloud.model;

public class ClassPartition {

	private final String classUri;
	private final long count;
	
	public ClassPartition(String classUri, long count) {
		this.classUri = classUri;
		this.count = count;
	}

	public String getClassUri() {
		return classUri;
	}

	public long getCount() {
		return count;
	}
	
}
