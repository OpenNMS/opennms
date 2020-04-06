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

package org.opennms.netmgt.graph.provider.application;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainGraph;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;

public final class ApplicationGraph extends AbstractDomainGraph<ApplicationVertex, SimpleDomainEdge> {

    public static final String NAMESPACE = "application";

    public ApplicationGraph(GenericGraph graph) {
        super(graph);
    }

    protected ApplicationVertex convert(GenericVertex vertex){
        return new ApplicationVertex(vertex);
    }

    protected SimpleDomainEdge convert(GenericEdge edge){
        return new SimpleDomainEdge(edge);
    }

    protected ImmutableGraph<ApplicationVertex, SimpleDomainEdge> convert(GenericGraph graph){
        return new ApplicationGraph(graph);
    }

    public static ApplicationGraphBuilder builder() {
        return new ApplicationGraphBuilder();
    }
    
    public static ApplicationGraph from(GenericGraph genericGraph) {
        return new ApplicationGraph(genericGraph);
    }
    
    public final static class ApplicationGraphBuilder extends AbstractDomainGraphBuilder<ApplicationGraphBuilder, ApplicationVertex, SimpleDomainEdge> {
               
        private ApplicationGraphBuilder() {
            namespace(NAMESPACE);
        }
        
        public ApplicationGraphBuilder description(String description) {
            delegate.property(GenericProperties.DESCRIPTION, description);
            return this;
        }
        
        public ApplicationGraph build() {
            namespace(NAMESPACE); // namespace is fixed, cannot be changed.
            return new ApplicationGraph(delegate.build());
        }
    }
}
