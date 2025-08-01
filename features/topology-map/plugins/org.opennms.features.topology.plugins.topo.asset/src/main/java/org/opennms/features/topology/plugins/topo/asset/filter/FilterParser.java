/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.plugins.topo.asset.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

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

		// if null filter definition then return empty filter map
		if(filter==null) return filterMap;

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
					throw new  IllegalArgumentException("Cannot parse filter. Cannot have empty value '"+value+ "' in expression:"+s);
				filterStringMap.get(nodeParamLabel).add(value);
			}
		}

		for (String nodeParamLabel:filterStringMap.keySet()){
			//create filter for each filter string
			List<Filter> includeFilters=new ArrayList<Filter>();
			List<Filter> excludeFilters=new ArrayList<Filter>();
			for(String filterValueString: filterStringMap.get(nodeParamLabel)){
				String valStr=null;
				String regexStr=null;
				Filter f=null;
				if(filterValueString.startsWith("!")){
					if(filterValueString.startsWith("!~")){
						regexStr=filterValueString.substring(2);
						try {
							f = new RegExCsvFilter<String>(regexStr);
				        } catch (PatternSyntaxException syntaxException){
				        	throw new IllegalArgumentException("Cannot parse filter. Illegal Regex expression for '"+nodeParamLabel+ "' value in expression:"+filterValueString,syntaxException);
				        }
					} else {
						valStr=filterValueString.substring(1);
						f = new EqCsvFilter<String>(valStr);
					}
					excludeFilters.add(f);
				} else {
					if(filterValueString.startsWith("~")){
						regexStr=filterValueString.substring(1);
						try {
							f = new RegExCsvFilter<String>(regexStr);
				        } catch (PatternSyntaxException syntaxException){
				        	throw new IllegalArgumentException("Cannot parse filter. Illegal Regex expression for '"+nodeParamLabel+ "' value in expression:"+filterValueString,syntaxException);
				        }
					} else {
						valStr=filterValueString;
						f= new EqCsvFilter<String>(valStr);
					}
					includeFilters.add(f);
				}
				// check simple equals parameters for illegal characters
				if(valStr!=null){
					if (valStr.contains("~")) 
						throw new IllegalArgumentException("Cannot parse filter. Illegal '~' character for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
					if (valStr.contains("!")) 
						throw new IllegalArgumentException("Cannot parse filter. Illegal '!' character for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
					if (valStr.equals("")) 
						throw new IllegalArgumentException("Cannot parse filter. Illegal empty value for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
				}
				// check regex for illegal characters
				if(regexStr!=null){
					if (regexStr.contains("~")) 
						throw new IllegalArgumentException("Cannot parse filter. Illegal '~' character for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
					if (regexStr.equals("")) 
						throw new IllegalArgumentException("Cannot parse filter. Illegal empty value for '"+nodeParamLabel+ "' value in expression:"+filterValueString);
				}
			}

			// Note the node filter removes unwanted nodes rather than includes wanted nodes
			Filter topFilter;
			// if include filters empty then pass all nodes and only consider exclude filters
			if(includeFilters.isEmpty()){
				topFilter=new OrFilter(excludeFilters);
			} else {
				topFilter=new OrFilter(new NotFilter(new OrFilter(includeFilters)), new OrFilter(excludeFilters));
			}

			filterMap.put(nodeParamLabel, topFilter);

		}


		return filterMap;
	}
}
