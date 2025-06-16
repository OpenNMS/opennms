/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.graph.domain.simple;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainGraph;

/**
 * Acts as a domain specific view on a {@link GenericGraph}.
 * This is the most basic concrete subclass of {@link AbstractDomainGraph} and can be used as a reference for your own
 * domain graph. It is a final class. If you need more functionality please extend {@link AbstractDomainGraph}.
 */
public final class SimpleDomainGraph extends AbstractDomainGraph<SimpleDomainVertex, SimpleDomainEdge> {

    public SimpleDomainGraph(GenericGraph graph) {
        super(graph);
    }

    @Override
    public SimpleDomainVertex convert(GenericVertex vertex) {
        return new SimpleDomainVertex(vertex);
    }

    @Override
    public SimpleDomainEdge convert(GenericEdge edge) {
        return new SimpleDomainEdge(edge);
    }

    @Override
    protected ImmutableGraph<SimpleDomainVertex, SimpleDomainEdge> convert(GenericGraph graph) {
        return new SimpleDomainGraph(graph);
    }

    public static SimpleDomainGraphBuilder builder() {
        return new SimpleDomainGraphBuilder();
    }
    
    public static SimpleDomainGraph from(GenericGraph genericGraph) {
        return new SimpleDomainGraph(genericGraph);
    }
    
    public final static class SimpleDomainGraphBuilder extends AbstractDomainGraphBuilder<SimpleDomainGraphBuilder, SimpleDomainVertex, SimpleDomainEdge> {
        
        private SimpleDomainGraphBuilder() {}

        public SimpleDomainGraph build() {
            return new SimpleDomainGraph(delegate.build());
        }
    }
}
