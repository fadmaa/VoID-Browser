package org.deri.voider.model;

import java.util.HashSet;
import java.util.Set;

public class AnnotatedSet {
	private int type;
	private final Set<String> values;
	private final int sizeLimit;
	public AnnotatedSet(Set<String> value) {
		this.type = UNKNOWN;
		this.values = value;
		this.sizeLimit = -1;
	}
	public AnnotatedSet(int t){
		this(t,-1);
	}
	public AnnotatedSet(int t, int sizeLimit){
		this.values = new HashSet<String>();
		this.type = t;
		this.sizeLimit = sizeLimit;
	}
	public int getType() {
		return type;
	}
	public Set<String> getValues() {
		return values;
	}
	public void setType(int t){
		if(type==UNKNOWN || type==t){
			this.type = t;
		}else{
			this.type = MIXED;
		}
	}
	public void add(String s){
		if(this.sizeLimit >0 & this.values.size() < sizeLimit){
			this.values.add(s);
		}
	}
	public boolean isEmpty(){
		return values.isEmpty();
	}
	public void addAll(AnnotatedSet set){
		this.values.addAll(set.getValues());
		if(this.type!=set.getType()){
			this.type = MIXED;
		}
	}
	private static final int UNKNOWN = 0;
	public static final int RESOURCES = 1;
	public static final int LITERALS = 2;
	public static final int MIXED = 3;
}
