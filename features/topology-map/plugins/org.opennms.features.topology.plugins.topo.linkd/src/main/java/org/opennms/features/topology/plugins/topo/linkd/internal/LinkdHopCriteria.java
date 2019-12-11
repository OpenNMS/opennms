/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * @author <a href=mailto:thedesloge@opennms.org>Donald Desloge</a>
 * @author <a href=mailto:seth@opennms.org>Seth Leger</a>
 */
public class LinkdHopCriteria extends VertexHopCriteria {

    public static VertexHopCriteria createCriteria(String namespace, String nodeId, String nodeLabel) {
        VertexHopCriteria criterion = new LinkdHopCriteria(namespace, nodeId, nodeLabel);
        return criterion;
    }

    private final String m_nodeId;
    private final String m_namespace;

    private LinkdHopCriteria(String namespace, String nodeId,
            String nodeLabel) {
        super(nodeId, nodeLabel);
        m_nodeId = nodeId;
        m_namespace = namespace;
    }

    @Override
    public String getNamespace() {
        return m_namespace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId, m_namespace);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof LinkdHopCriteria) {
            LinkdHopCriteria ref = (LinkdHopCriteria) obj;
            return Objects.equals(m_nodeId, ref.m_nodeId) && Objects.equals(m_namespace, ref.m_namespace);
        }
        return false;
    }

    @Override
    public Set<VertexRef> getVertices() {
        Set<VertexRef> vertices = new TreeSet<VertexRef>(new RefComparator());
        vertices.add(new DefaultVertexRef(getNamespace(), m_nodeId,getLabel()));
        return vertices;
    }

}
