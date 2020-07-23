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

package org.opennms.features.topology.api.support.hops;

import java.util.Objects;
import java.util.Set;

import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Sets;

/**
 * Helper criteria class to reference to existing VertexRefs.
 * This should be used anytime you want to add a vertex to the current focus (e.g. from the mouse context menu).
 */
public class DefaultVertexHopCriteria extends VertexHopCriteria {

    private final VertexRef vertexRef;

    public DefaultVertexHopCriteria(VertexRef vertexRef) {
        super(vertexRef.getId(), vertexRef.getLabel());
        this.vertexRef = vertexRef;
    }

    @Override
    public Set<VertexRef> getVertices() {
        return Sets.newHashSet(vertexRef);
    }

    @Override
    public String getNamespace() {
        return vertexRef.getNamespace();
    }

    @Override
    public int hashCode() {
        return vertexRef.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof DefaultVertexHopCriteria) {
            return Objects.equals(vertexRef, ((DefaultVertexHopCriteria) obj).vertexRef);
        }
        return false;
    }
}
