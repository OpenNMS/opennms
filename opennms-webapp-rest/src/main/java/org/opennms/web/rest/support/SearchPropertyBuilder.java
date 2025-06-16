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
