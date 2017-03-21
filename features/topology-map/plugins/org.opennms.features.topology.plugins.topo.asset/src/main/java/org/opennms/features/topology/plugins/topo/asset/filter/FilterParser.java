/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.asset.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses filter queries to {@link Filter} objects.
 *  OR | key1=value1,value2 alternatively key1=value1&key1=value2 | asset-region=north,south
 *  AND | key1=val1&key2=val2 | asset-region=north&asset-site=23
 *  NOT | key1=!val1 | asset-site=!23
 *  Regex key1=~<regex expression> 
 *  NOT regex key1=!~<regex expression> 
 *
 * @author cgallen
 */
public class FilterParser {

	private static final Logger LOG = LoggerFactory.getLogger(FilterParser.class);

	public Map<String, Filter> parse(List<String> filter) {

		// filterMap key=NodeParamLabels value= Filter types 
		Map<String, Filter> filterMap= new LinkedHashMap<String, Filter>();

		// filterStringMap key=NodeParamLabels value=list of string values for filters 
		Map<String,List<String>> filterStringMap = new LinkedHashMap<String, List<String>>();

		for(String s:filter){
			String[] x = s.split("=");
			if(x.length<2) throw new IllegalArgumentException("Cannot parse filter. no '=' in expression:"+s);
			if(x.length>2) throw new IllegalArgumentException("Cannot parse filter. too many '=' in expression:"+s);
			String nodeParamLabel=x[0];
			String filterValue=x[1];
			if(!NodeParamLabels.ALL_KEYS.contains(nodeParamLabel))
				throw new IllegalArgumentException("Cannot parse filter. Unknown nodeParamLabel value '"+nodeParamLabel+ "' in expression:"+s);
			if (! filterStringMap.containsKey(nodeParamLabel)){
				filterStringMap.put(nodeParamLabel, new ArrayList<String>());
			}
			// split any comma separated values into separate value pairs
			String[] values = filterValue.split(",");
			for(String value:values){
				if("".equals(value))
					throw new  IllegalArgumentException("Cannot parse filter. cannot have empty value '"+filterValue+ "' in expression:"+s);
				filterStringMap.get(nodeParamLabel).add(value);
			}
		}

		//create filter for each filter string
		List<Filter> orFilters=new ArrayList<Filter>();
		List<Filter> andFilters=new ArrayList<Filter>();


		for (String nodeParamLabel:filterStringMap.keySet()){
			for(String filterValueString: filterStringMap.get(nodeParamLabel)){
				String valStr=null;
				Filter f=null;
				if(filterValueString.startsWith("!")){
					if(filterValueString.startsWith("!~")){
						valStr=filterValueString.substring(2);
						f = new NotFilter(new RegExFilter(valStr));
					} else {
						valStr=filterValueString.substring(1);
						f = new NotFilter(new EqFilter(valStr));
					}
					andFilters.add(f);
				} else {
					if(filterValueString.startsWith("~")){
						valStr=filterValueString.substring(1);
						f = new RegExFilter(valStr);
					} else {
						valStr=filterValueString;
						f= new EqFilter(valStr);
					}
					orFilters.add(f);
				}
				if (valStr.contains("~")) 
					throw new IllegalArgumentException("Cannot parse filter. Illegal '~' character for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
				if (valStr.contains("!")) 
					throw new IllegalArgumentException("Cannot parse filter. Illegal '!' character for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
				if (valStr.equals("")) 
					throw new IllegalArgumentException("Cannot parse filter. Illegal empty value for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
			}
			
			Filter topFilter=new NotFilter(new AndFilter(new OrFilter(orFilters), new AndFilter(andFilters)));
			filterMap.put(nodeParamLabel, topFilter);
			
		}


		return filterMap;
	}
}
