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
