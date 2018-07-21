/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import org.opennms.features.topology.api.topo.Vertex;

public abstract class LinkdDetail<K,L> {

    private final String m_id;
    private final Vertex m_source;
    private final K m_sourceLink;
    private final Vertex m_target;
    private final L m_targetLink;

    public LinkdDetail(String id, Vertex source, K sourceLink, Vertex target, L targetLink){
        m_id = id;
        m_source = source;
        m_sourceLink = sourceLink;
        m_target = target;
        m_targetLink = targetLink;
    }

    public abstract int hashCode();

    public abstract boolean equals(Object obj);

    public abstract Integer getSourceIfIndex();

    public abstract Integer getTargetIfIndex();

    public abstract String getType();

    public String getId() {
        return m_id;
    }

    public Vertex getSource() {
        return m_source;
    }

    public Vertex getTarget() {
        return m_target;
    }

    public K getSourceLink() {
        return m_sourceLink;
    }

    public L getTargetLink() {
        return m_targetLink;
    }

}
