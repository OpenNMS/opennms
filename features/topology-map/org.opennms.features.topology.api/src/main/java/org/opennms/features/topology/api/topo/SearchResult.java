/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;

@XmlRootElement(name = "search")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class SearchResult {

	public static final boolean COLLAPSIBLE = true;
	public static final boolean COLLAPSED = true;

	@XmlElement(name = "id")
    private String m_id;
	@XmlElement(name = "namespace")
    private String m_namespace;
	@XmlElement(name = "label")
    private String m_label;
	@XmlElement(name = "query")
    private String m_query;
	@XmlElement(name = "collapsible")
    private boolean m_collapsible = false;
	@XmlElement(name = "collapsed")
    private boolean m_collapsed = false;

	// Constructor for JAXB
	public SearchResult() {
	}

	public SearchResult(String namespace, String id, String label, String query, boolean collapsible, boolean collapsed) {
        m_id = id;
        m_namespace = namespace;
        m_label = label;
        m_query = query;
        m_collapsible = collapsible;
        m_collapsed = collapsed;
    }

    public SearchResult(VertexRef vertexRef, boolean collapsible, boolean collapsed) {
        this(vertexRef.getNamespace(), vertexRef.getId(), vertexRef.getLabel(), null, collapsible, collapsed);
    }

	public SearchResult(SearchCriteria criteria) {
		this(criteria.getNamespace(), criteria.getId(), criteria.getLabel(), criteria.getSearchString(), SearchResult.COLLAPSIBLE, criteria.isCollapsed());
	}

	public final String getId() {
        return m_id;
    }

    public final String getNamespace() {
        return m_namespace;
    }

    public final String getLabel() {
        return m_label;
    }
    
    public final String getQuery() {
        return m_query;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        SearchResult ref = (SearchResult)obj;

        return getNamespace().equals(ref.getNamespace()) && getId().equals(ref.getId());

    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result
                + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        return result;
    }

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", m_id)
				.add("namespace", m_namespace)
				.add("label", m_label)
				.add("query", m_query)
				.add("collapsible", m_collapsible)
				.add("collapsed", m_collapsed)
				.toString();
	}

	public final boolean isCollapsible() {
		return m_collapsible;
	}

	public final void setCollapsible(boolean collapsible) {
		this.m_collapsible = collapsible;
	}

	public final boolean isCollapsed() {
		return m_collapsed;
	}

	public final void setCollapsed(boolean collapsed) {
		this.m_collapsed = collapsed;
	}
}
