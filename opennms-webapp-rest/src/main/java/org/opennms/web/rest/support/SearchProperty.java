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

import java.util.Comparator;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Seth
 */
@XmlRootElement(name="property")
public class SearchProperty implements Comparable<SearchProperty> {

	public static final boolean DEFAULT_ORDER_BY = true;

	public static final boolean DEFAULT_IPLIKE = false;

	private static final Comparator<SearchProperty> COMPARATOR = Comparator
		// Compare the entity class name
		.<SearchProperty,String>comparing(t -> t.entityClass.getName())
		// Compare the property ID
		.thenComparing(SearchProperty::getId);

	public static enum SearchPropertyType {
		FLOAT,
		INTEGER,
		IP_ADDRESS,
		LONG,
		STRING,
		TIMESTAMP
	}

	public SearchProperty() {}

	public SearchProperty(Class<?> entityClass, String id, String name, SearchPropertyType type) {
		this(entityClass, id, name, type, null);
	}

	public SearchProperty(Class<?> entityClass, String id, String name, SearchPropertyType type, Map<String,String> values) {
		this(entityClass, null, id, null, name, type, DEFAULT_ORDER_BY, DEFAULT_IPLIKE, values);
	}

	public SearchProperty(
		Class<?> entityClass,
		String idPrefix,
		String id,
		String namePrefix,
		String name,
		SearchPropertyType type,
		boolean orderBy,
		boolean iplike,
		Map<String, String> values
	) {
		this.entityClass = entityClass;
		this.idPrefix = idPrefix;
		this.id = id;
		this.namePrefix = namePrefix;
		this.name = name;
		this.type = type;
		this.orderBy = orderBy;
		this.iplike = iplike;
		this.values = values;
	}

	@XmlTransient
	public Class<?> entityClass;

	@XmlTransient
	String idPrefix;

	@XmlTransient
	public String id;

	@XmlAttribute
	public String getId() {
		return (idPrefix == null ? id : idPrefix + "." + id);
	}

	@XmlTransient
	String namePrefix;

	@XmlTransient
	public String name;

	@XmlAttribute
	public String getName() {
		return (namePrefix == null ? name : namePrefix + ": " + name);
	}

	@XmlAttribute
	public SearchPropertyType type;

	@XmlAttribute
	public boolean orderBy;

	@XmlAttribute
	public boolean iplike;

	@XmlElementWrapper(name = "values")
	public Map<String,String> values;

	@Override
	public int compareTo(SearchProperty o) {
		return COMPARATOR.compare(this, o);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("idPrefix", idPrefix)
			.append("id", id)
			.append("namePrefix", namePrefix)
			.append("name", name)
			.append("type", type.toString())
			.append("iplike", iplike)
			.append("orderBy", orderBy)
			.build();
	}
}
