package org.opennms.features.topology.plugins.topo.asset.repo;

import java.util.ArrayList;
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
		boolean matches = false;

		// check filter correctness and split comma separated values
		List<String>newFilter=new ArrayList<String>();
		for (String filterElement : filter) {
			String[] keyVal = filterElement.split("=");
			if (keyVal.length < 2)
				throw new RuntimeException(
						"filter element incorrectly formatted. (no '=' in filterElement '"
								+ filterElement + "')");
			if (keyVal.length != 2)
				throw new RuntimeException(
						"filter element incorrectly formatted. (too many '=' in  filterElement '"
								+ filterElement + "')");
			String key = keyVal[0];
			String value = keyVal[1];
			if (key.contains(","))
				throw new RuntimeException(
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
		for (String filterElement : newFilter) {
			String[] keyVal = filterElement.split("=");
			String key = keyVal[0];
			String value = keyVal[1];
			String paramValue = peramaterMap.get(key);
			if(value.equals("!")) throw new RuntimeException("filter element incorrectly formatted. (only'!' in  filterElement '"
					+ filterElement + "')");
			if (paramValue != null) {
				// deal with negation
				if (value.startsWith("!")) {
					if (value.substring(1).equals(paramValue)) return false;
				} else {
					if (value.equals(paramValue)) matches= true;
				}
			}
		}

		return matches;
	}

}
