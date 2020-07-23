/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.bsm;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainGraph;


public final class BusinessServiceGraph extends AbstractDomainGraph<BusinessServiceVertex, BusinessServiceEdge> {

    public static final String NAMESPACE = "bsm";

    public BusinessServiceGraph(GenericGraph graph) {
        super(graph);
    }
    
    @Override
    protected ImmutableGraph<BusinessServiceVertex, BusinessServiceEdge> convert(GenericGraph graph) {
        return new BusinessServiceGraph(graph);
    }

    @Override
    protected BusinessServiceVertex convert(GenericVertex vertex) {
        return BusinessServiceVertex.from(vertex);
    }

    @Override
    protected BusinessServiceEdge convert(GenericEdge edge) {
        return new BusinessServiceEdge(edge);
    }

    public static BusinessServiceGraphBuilder builder() {
        return new BusinessServiceGraphBuilder();
    }
    
    public static BusinessServiceGraph from(GenericGraph genericGraph) {
        return new BusinessServiceGraph(genericGraph);
    }

    public final static class BusinessServiceGraphBuilder extends AbstractDomainGraphBuilder<BusinessServiceGraphBuilder, BusinessServiceVertex, BusinessServiceEdge> {
        
        private BusinessServiceGraphBuilder() {
            namespace(NAMESPACE);
        }
 
        public BusinessServiceGraph build() {
            return new BusinessServiceGraph(delegate.build());
        }
    }
    
}
