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

import java.util.regex.Pattern;

/**
 * This filter first splits value into comma separated values and
 * then applies the regex match against each csv. This is used to handle
 * the NODE_CATEGORIES case when a node has a csv list of categories. 
 * The returned result is an OR function. i.e. if any of the values matches
 * it returns true
 * @author cgallen
 *
 * @param <T>
 */
public class RegExCsvFilter<T> implements Filter<T> {

    private final Pattern regExp;

    public RegExCsvFilter(String regexp) {
        this.regExp = Pattern.compile(regexp);
    }

    @Override
    public boolean apply(T value) {
    	String[] values = value.toString().split(",");
    	for(String val:values){
    		if(regExp.matcher(val.toString()).matches()) {
    		    return true;
            }
    	}
        return false;
    }
}
