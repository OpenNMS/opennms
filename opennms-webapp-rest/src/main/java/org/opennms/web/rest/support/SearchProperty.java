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

import java.util.Comparator;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.ImmutableMap;

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

    public static final Map<String, String> TRUE_OR_FALSE_VALUES = ImmutableMap.<String, String> builder()
            .put("1", "TRUE")
            .put("0", "FALSE")
            .build();

    public static enum SearchPropertyType {
        BOOLEAN,
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
