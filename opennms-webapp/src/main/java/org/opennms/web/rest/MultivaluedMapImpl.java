/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

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
