/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLevelAwareVertex extends AbstractVertex implements LevelAware {

    private VertexRef m_parent;
    private List<VertexRef> m_children = new ArrayList<>();

    public AbstractLevelAwareVertex(String namespace, String id, String label) {
        super(namespace, id, label);
    }

    public final VertexRef getParent() {
        return m_parent;
    }

    public final void setParent(VertexRef parent) {
        if (this.equals(parent)) return;
        m_parent = parent;
    }

    public void addChildren(AbstractLevelAwareVertex vertex) {
        if (!m_children.contains(vertex)) {
            m_children.add(vertex);
            vertex.setParent(this);
        }
    }

    public List<VertexRef> getChildren() {
        return m_children;
    }
}
