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
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

// TODO MVR this is almost the same implementation as ApplicatinVertexProvider and maybe SimpleVertexProvider. Generalize please.
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
        for (BusinessServiceCriteria eachCriteria : getBusinessServiceCriteria(criteria)) {
            if (eachCriteria.apply(refToFilter)) {
                return refToFilter;
            }
        }
        return null;
    }

    private List<BusinessServiceCriteria> getBusinessServiceCriteria(Criteria... criteria) {
        List<BusinessServiceCriteria> returnList = new ArrayList<>();
        for (Criteria eachCriteria : criteria) {
            if (eachCriteria instanceof BusinessServiceCriteria) {
                returnList.add((BusinessServiceCriteria) eachCriteria);
            }
        }
        return returnList;
    }
}
