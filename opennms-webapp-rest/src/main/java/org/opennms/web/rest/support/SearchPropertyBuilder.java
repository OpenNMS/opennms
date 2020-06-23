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

import org.opennms.web.rest.support.SearchProperty.SearchPropertyType;

/**
 * @author Seth
 */
public class SearchPropertyBuilder {
	private Class<?> entityClass;
	private String idPrefix;
	private String id;
	private String namePrefix;
	private String name;
	private SearchPropertyType type;
	private boolean orderBy;
	private boolean iplike;
	private Map<String,String> values;

	public SearchPropertyBuilder() {
	}

	public SearchPropertyBuilder entityClass(Class<?> clazz) {
		entityClass = clazz;
		return this;
	}

	public SearchPropertyBuilder idPrefix(String value) {
		idPrefix = value;
		return this;
	}

	public SearchPropertyBuilder id(String value) {
		id = value;
		return this;
	}

	public SearchPropertyBuilder namePrefix(String value) {
		namePrefix = value;
		return this;
	}

	public SearchPropertyBuilder name(String value) {
		name = value;
		return this;
	}

	public SearchPropertyBuilder type(SearchPropertyType value) {
		type = value;
		return this;
	}

	public SearchPropertyBuilder orderBy(boolean value) {
		orderBy = value;
		return this;
	}

	public SearchPropertyBuilder iplike(boolean value) {
		iplike = value;
		return this;
	}

	public SearchPropertyBuilder values(Map<String,String> value) {
		values = value;
		return this;
	}

	public SearchProperty build() {
		return new SearchProperty(entityClass, idPrefix, id, namePrefix, name, type, orderBy, iplike, values);
	}
}
