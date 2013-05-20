/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * <p>MultivaluedMapImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MultivaluedMapImpl extends HashMap<String, List<String>> implements MultivaluedMap<String, String> {

    private static final long serialVersionUID = 8520828454808579795L;

    public MultivaluedMapImpl() {
        super();
    }

    /** 
	 * This constructor can be used as a convenience method to create populated
	 * {@link MultivaluedMapImpl} instances.
	 */
	public MultivaluedMapImpl(String[][] keyValuePairs) {
	    super();
	    for (String[] keyValuePair : keyValuePairs) {
	        this.add(keyValuePair[0], keyValuePair[1]);
	    }
	}

	/**
	 * <p>add</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
    @Override
	public void add(String key, String value) {
		List<String> valueList=super.get(key);
		if(valueList==null) {
			valueList=new ArrayList<String>();
			super.put(key, valueList);
		}
		valueList.add(value);
	}

	/**
	 * <p>getFirst</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
    @Override
	public String getFirst(String key) {
		List<String> values=super.get(key);
		if(values == null || values.size()==0) {
			return null;
		}
		return values.get(0);
	}

	/**
	 * <p>putSingle</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
    @Override
	public void putSingle(String key, String value) {
		List<String> list=new ArrayList<String>();
		list.add(value);
		super.put(key, list);
	}
	
	/**
	 * <p>put</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param values a {@link java.lang.String} object.
	 */
	public void put(String key, String...values){
	    put(key, new ArrayList<String>(Arrays.asList(values)));
	}

}
