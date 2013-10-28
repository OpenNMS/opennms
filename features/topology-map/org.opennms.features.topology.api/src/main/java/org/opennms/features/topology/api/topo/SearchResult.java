/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

public class SearchResult{

    private final String m_id;
    private final String m_namespace;
    private final String m_label;

    public SearchResult(String id, String namespace, String label){
        m_id = id;
        m_namespace = namespace;
        m_label = label;
    }

    public SearchResult(VertexRef vertexRef) {
        this(vertexRef.getId(), vertexRef.getNamespace(), vertexRef.getLabel());
    }

    public String getId() {
        return m_id;
    }

    public String getNamespace() {
        return m_namespace;
    }

    public String getLabel() {
        return m_label;
    }

    @Override
    public boolean equals(Object obj) {
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
}
