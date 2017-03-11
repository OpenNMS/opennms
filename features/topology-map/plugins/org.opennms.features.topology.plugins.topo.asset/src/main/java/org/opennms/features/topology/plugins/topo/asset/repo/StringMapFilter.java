package org.opennms.features.topology.plugins.topo.asset.repo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StringMapFilter {

	private volatile Map<String, Map<String, String>> map = null;

	public StringMapFilter(Map<String, Map<String, String>> map) {
		this.map = Objects.requireNonNull(map);
	}

	/**
	 * returns filtered node info repository filter name value pair filter =
	 * null do not filter results key1=value1&key1=value2 (additive i.e. OR of
	 * key 1 values key1=value1,value2,value3 (additive i.e. OR of key 1 values)
	 * alternative) key1=value1,key2=value2,value3 etc (AND of keys - both must
	 * apply) key1=!value1 (negation - key NOT value1) key1=~regex (regex match
	 * of key) or key1=!~regex
	 * 
	 * @param filter
	 *            filter can be derived from url query
	 * @return filter NodeInfo
	 */
	public Map<String, Map<String, String>> filter(List<String> filter) {
		Map<String, Map<String, String>> newMap = new LinkedHashMap<String, Map<String, String>>();

		if (filter == null)
			return map;

		for (String key : map.keySet()) {
			if (matches(map.get(key), filter))
				newMap.put(key, map.get(key));
		}
		return newMap;

	}

	private boolean matches(Map<String, String> peramaterMap,List<String> filter) {

		// check filter correctness and split comma separated values
		if(filter.isEmpty()) return false;

		List<String>newFilter=new ArrayList<String>();
		for (String filterElement : filter) {
			String[] keyVal = filterElement.split("=");
			if (keyVal.length < 2)
				throw new IllegalArgumentException(
						"filter element incorrectly formatted. (no '=' in filterElement '"
								+ filterElement + "')");
			if (keyVal.length != 2)
				throw new IllegalArgumentException(
						"filter element incorrectly formatted. (too many '=' in  filterElement '"
								+ filterElement + "')");
			String key = keyVal[0];
			String value = keyVal[1];
			if (key.contains(","))
				throw new IllegalArgumentException(
						"filter element incorrectly formatted. (',' in  key in filterElement '"
								+ filterElement + "')");
			// deal with case parameter=a,b,c
			// change to parameter=a,parameter=b,parameter=c
			String[] vals = value.split(",");
			for (String val : vals) {
				newFilter.add(key+"="+val);
			}
		}
		// deal with new filter without comma separated values

		// separate all values for each key
		Map<String,List<String>> keys = new LinkedHashMap<String,List<String>>();

		for (String filterElement : newFilter) {
			String[] keyVal = filterElement.split("=");
			String key = keyVal[0];
			String value = keyVal[1];
			if(value.equals("!")) throw new IllegalArgumentException("filter element incorrectly formatted. (only'!' in  filterElement '"
					+ filterElement + "')");

			if(!keys.containsKey(key)){
				keys.put(key, new ArrayList<String>());
			}
			keys.get(key).add(value);
		}

		// for each key if no matching parameter key return false
		// for each key if no matching parameter values return false

		for(String key:keys.keySet()){
			String paramValue = peramaterMap.get(key);
			boolean innerMatch=false;
			List<String> values = keys.get(key);

			for(String value:values){
				// deal with negation
				if (value.startsWith("!")) {
					// break and return if we ever find a NOT value
					if (value.substring(1).equals(paramValue)) return false;
					innerMatch= true;
				} else {
					if (value.equals(paramValue)) {
						// break and return innerMatch true if paramValue matches value
						innerMatch= true;
						break;
					}
				}
			}
			if(!innerMatch) return false;
		}

		return true;
	}

}
