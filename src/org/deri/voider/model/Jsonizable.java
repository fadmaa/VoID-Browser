package org.deri.voider.model;

import org.deri.voider.util.PrefixManager;
import org.json.JSONException;
import org.json.JSONWriter;

public interface Jsonizable {

	public void write(JSONWriter writer, PrefixManager prefixManager)throws JSONException;
}
