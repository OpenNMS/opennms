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

package org.opennms.web.rest.support;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Seth
 */
@XmlRootElement(name="property")
public class SearchProperty implements Comparable<SearchProperty> {

	public static final boolean DEFAULT_ORDER_BY = true;

	public static enum SearchPropertyType {
		FLOAT,
		INTEGER,
		IP_ADDRESS,
		LONG,
		STRING,
		TIMESTAMP
	}

	public SearchProperty() {}

	public SearchProperty(String id, String name, SearchPropertyType type) {
		this(id, name, type, null);
	}

	public SearchProperty(String id, String name, SearchPropertyType type, boolean orderBy) {
		this(id, name, type, orderBy, null);
	}

	public SearchProperty(String id, String name, SearchPropertyType type, Map<String,String> values) {
		this(id, name, type, DEFAULT_ORDER_BY, values);
	}

	public SearchProperty(String id, String name, SearchPropertyType type, boolean orderBy, Map<String,String> values) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.orderBy = orderBy;
		this.values = values;
	}

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String name;

	@XmlAttribute
	public SearchPropertyType type;

	@XmlAttribute
	public boolean orderBy;

	@XmlElementWrapper(name = "values")
	public Map<String,String> values;

	@Override
	public int compareTo(SearchProperty o) {
		return id.compareTo(o.id);
	}
}
