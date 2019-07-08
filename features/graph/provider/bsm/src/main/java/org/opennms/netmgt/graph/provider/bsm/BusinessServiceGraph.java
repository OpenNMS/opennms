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
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.simple.AbstractDomainGraph;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleVertex;
import org.opennms.netmgt.graph.simple.AbstractDomainGraph.AbstractDomainGraphBuilder;
import org.opennms.netmgt.graph.simple.SimpleGraph.SimpleGraphBuilder;


// TODO: Patrick: I am not sure we need such a complicated object structure since we only build the objects and then give them
// as AbstractDomain* to the outside. Evaluate if the builders wouldn't be enough?
public final class BusinessServiceGraph extends AbstractDomainGraph<AbstractBusinessServiceVertex, BusinessServiceEdge> {
    
    public BusinessServiceGraph(GenericGraph graph) {
        super(graph);
    }
    
    @Override
    protected ImmutableGraph<AbstractBusinessServiceVertex, BusinessServiceEdge> convert(GenericGraph graph) {
        return new BusinessServiceGraph(graph);
    }

    @Override
    protected AbstractBusinessServiceVertex convert(GenericVertex vertex) {
        return AbstractBusinessServiceVertex.from(vertex);
    }

    @Override
    protected BusinessServiceEdge convert(GenericEdge edge) {
        return new BusinessServiceEdge(edge);
    }

    @Override
    public Class getVertexType() {
        return AbstractBusinessServiceVertex.class;
    }
    
    public static BusinessServiceGraphBuilder builder() {
        return new BusinessServiceGraphBuilder();
    }

    public final static class BusinessServiceGraphBuilder extends AbstractDomainGraphBuilder<BusinessServiceGraphBuilder,
        AbstractBusinessServiceVertex, BusinessServiceEdge> {
        
        private BusinessServiceGraphBuilder() {}
 
        public BusinessServiceGraph build() {
            return new BusinessServiceGraph(delegate.build());
        }
    }
    
}
