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
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.VertexRef;

/**
 * Wrapper class to wrap a bunch of {@link VertexHopCriteria}.
 * There may be multiple {@link VertexHopCriteria} objects available.
 * However in the end it is easier to use this criteria object to wrap all available {@link VertexHopCriteria}
 * instead of iterating over all all the time and determine all vertices.
 */
public class WrappedVertexHopCriteria extends VertexHopCriteria {

    private final Set<VertexHopCriteria> criteriaList;

    public WrappedVertexHopCriteria(Set<VertexHopCriteria> vertexHopCriterias) {
        super("Wrapped Vertex Hop Criteria for all VertexHopCriteria in the currently selected GraphProvider");
        criteriaList = Objects.requireNonNull(vertexHopCriterias);
    }

    public void addCriteria(VertexHopCriteria criteria) {
        this.criteriaList.add(criteria);
    }

    @Override
    public Set<VertexRef> getVertices() {
        Set<VertexRef> vertices = criteriaList.stream()
                .flatMap(criteria -> criteria.getVertices().stream())
                .collect(Collectors.toSet());
        return vertices;
    }

    @Override
    public String getNamespace() {
        return "$wrapped$";
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteriaList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof WrappedVertexHopCriteria) {
            WrappedVertexHopCriteria other = (WrappedVertexHopCriteria) obj;
            return Objects.equals(criteriaList, other.criteriaList);
        }
        return false;
    }

    public boolean contains(VertexRef vertexRef) {
        return getVertices().contains(vertexRef);
    }
}
