/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class BusinessServiceVertexProvider extends SimpleVertexProvider {
    public BusinessServiceVertexProvider(String namespace) {
        super(namespace);
    }

    @Override
    public Vertex getVertex(VertexRef reference, Criteria... criteria) {
        Vertex theVertex = super.getVertex(reference, criteria);
        return filter(theVertex, criteria);
    }

    @Override
    public List<Vertex> getVertices(Criteria... criteria) {
        List<Vertex> vertices = super.getVertices(criteria);
        Collection<Vertex> filter = filter(vertices, criteria);
        return new ArrayList<>(filter);
    }

    @Override
    public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
        List<Vertex> vertices = super.getVertices(references, criteria);
        Collection<Vertex> filteredVertices = filter(vertices, criteria);
        return new ArrayList<>(filteredVertices);
    }

    private <T extends VertexRef> Collection<T> filter(List<T> references, final Criteria... criteria) {
        return Collections2.filter(references, new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return filter(input, criteria) != null;
            }
        });
    }

    private <T extends VertexRef> T filter(T refToFilter, Criteria... criteria) {
        // The BusinessServiceCriteria objects are used to indicate focal points
        // and aren't actually used to filter the result set.
        // At this point in time we don't support any additional Criteria types so
        // we just return the original set of vertices.
        return refToFilter;
    }
}
