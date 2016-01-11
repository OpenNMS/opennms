/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.VertexRef;

class BusinessServiceVertex extends AbstractVertex {

    private List<VertexRef> children = new ArrayList<>();

    /**
     * Creates a new {@link BusinessServiceVertex}.
     * @param id the unique id of this vertex. Must be unique overall the namespace.
     */
    public BusinessServiceVertex(String id, String label) {
        super(BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE, id, label);
        setIconKey(null);
        setLocked(false);
        setSelected(false);
    }

    public void addChildren(AbstractVertex vertex) {
        if (!children.contains(vertex)) {
            children.add(vertex);
            vertex.setParent(this);
        }
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public List<VertexRef> getChildren() {
        return children;
    }


    public boolean isPartOf(String serviceId) {
        return serviceId != null && serviceId.equals(getRoot().getId());
    }

    public BusinessServiceVertex getRoot() {
        if (isRoot()) {
            return this;
        }
        return ((BusinessServiceVertex)getParent()).getRoot();
    }
}
