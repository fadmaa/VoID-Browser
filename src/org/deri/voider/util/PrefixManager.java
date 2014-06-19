package org.deri.voider.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

//TODO make this a singleton
public class PrefixManager {
	private Map<String, String> prefixMap = new HashMap<String, String>();

	public PrefixManager(InputStream in)  {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringTokenizer tokenizer;
		// Read File Line By Line
		try {
			while ((strLine = br.readLine()) != null) {
				tokenizer = new StringTokenizer(strLine, "\t");
				String prefix = tokenizer.nextToken();
				String uri = tokenizer.nextToken();
				if(!prefixMap.containsKey(uri)){
					prefixMap.put(uri,prefix);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("error reading prefixec file",e);
		}
	}

	public String getPrefix(String namespace){
		return prefixMap.get(namespace);
	}
	
	public String getCurie(String uri){
		int index = getNamespaceEndPosition(uri);
		String prefix =  getPrefix(uri.substring(0,index));
		if(prefix==null){
			//unknown prefix
			return uri;
		}
		return prefix + ":" + uri.substring(index);
	}
	
	private int getNamespaceEndPosition(String uri){
		if(uri.indexOf("#")!=-1){
			return uri.indexOf("#")+1;
		}else{
			return uri.lastIndexOf("/") + 1;
		}
	}
}
