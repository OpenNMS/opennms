package org.opennms.web.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public class MultivaluedMapImpl extends HashMap<String, List<String>> implements MultivaluedMap<String, String> {

	private static final long serialVersionUID = 1L;

	public void add(String key, String value) {
		List<String> valueList=super.get(key);
		if(valueList==null) {
			valueList=new ArrayList<String>();
			super.put(key, valueList);
		}
		valueList.add(value);
	}

	public String getFirst(String key) {
		List<String> values=super.get(key);
		if(values.size()==0) {
			return null;
		}
		return values.get(0);
	}

	public void putSingle(String key, String value) {
		List<String> list=new ArrayList<String>();
		list.add(value);
		super.put(key, list);
	}
	
	public void put(String key, String...values){
	    put(key, new ArrayList<String>(Arrays.asList(values)));
	}

}
