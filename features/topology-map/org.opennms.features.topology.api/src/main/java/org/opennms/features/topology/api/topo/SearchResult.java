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

public class SearchResult {

    private final String m_id;
    private final String m_namespace;
    private final String m_label;
    private final String m_query;
    private boolean m_collapsible = false;
    private boolean m_collapsed = false;
    
    public SearchResult(String namespace, String id, String label, String query) {
        m_id = id;
        m_namespace = namespace;
        m_label = label;
        m_query = query;
    }

    public SearchResult(VertexRef vertexRef) {
        this(vertexRef.getNamespace(), vertexRef.getId(), vertexRef.getLabel(), null);
    }

    public SearchResult(SearchResult result) {
        this(result.getNamespace(), result.getId(), result.getLabel(), result.getQuery());
        setCollapsible(result.isCollapsible());
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
    	return "NameSpace:"+m_namespace+"; ID:"+m_id+"; Label:"+m_label;
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
