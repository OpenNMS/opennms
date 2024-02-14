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
