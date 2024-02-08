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

import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.domain.AbstractDomainGraphContainer;

/**
 * Acts as a domain specific view on a {@link GenericGraphContainer}.
 * This is the most basic concrete subclass of {@link AbstractDomainGraphContainer} and can be used as a reference for your own
 * domain graph container. It is a final class. If you need more functionality please extend {@link AbstractDomainGraphContainer}.
 */
public final class SimpleDomainGraphContainer extends AbstractDomainGraphContainer<SimpleDomainGraph> {
    
    private SimpleDomainGraphContainer(GenericGraphContainer genericGraphContainer){
        super(genericGraphContainer);
    }

    @Override
    protected SimpleDomainGraph convert(GenericGraph graph) {
        return new SimpleDomainGraph(graph);
    }
     
    public static SimpleDomainGraphContainerBuilder builder() {
        return new SimpleDomainGraphContainerBuilder();
    }
    
    public static SimpleDomainGraphContainer from(GenericGraphContainer genericGraphContainer) {
        return new SimpleDomainGraphContainer(genericGraphContainer);
    }

    public final static class SimpleDomainGraphContainerBuilder extends AbstractDomainGraphContainerBuilder<SimpleDomainGraphContainerBuilder, SimpleDomainGraph> {
        
        private SimpleDomainGraphContainerBuilder() {}
        
        public SimpleDomainGraphContainer build() {
            return new SimpleDomainGraphContainer(this.builder.build());
        }
    }
    
}
