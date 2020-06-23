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
