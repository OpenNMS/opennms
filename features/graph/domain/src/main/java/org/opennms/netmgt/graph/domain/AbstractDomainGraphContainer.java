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
package org.opennms.netmgt.graph.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer.GenericGraphContainerBuilder;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;

/**
 * Acts as a domain specific view on a {@link GenericGraphContainer}.
 * Can be extended by a domain specific graph container class.
 * It contains no data of it's own but operates on the data of it's wrapped {@link GenericGraphContainer}.
 **/
public abstract class AbstractDomainGraphContainer<G extends AbstractDomainGraph<? extends AbstractDomainVertex, ? extends AbstractDomainEdge>>
    implements ImmutableGraphContainer<G> {
    
    private final GenericGraphContainer delegate;

    protected AbstractDomainGraphContainer(GenericGraphContainer delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    protected abstract G convert(GenericGraph graph);
    
    @Override
    public List<G> getGraphs() {
        return delegate.getGraphs().stream().map(graph -> convert(graph)).collect(Collectors.toList());
    }

    @Override
    public G getGraph(String namespace) {
        return getGraphs().stream().filter(g -> g.getNamespace().equals(namespace))
                .findFirst().orElse(null);
    }

    @Override
    public List<String> getNamespaces() {
        return delegate.getNamespaces();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getLabel() {
        return delegate.getLabel();
    }

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        Objects.requireNonNull(namespace);
        return delegate.getGraphInfo(namespace);
    }

    @Override
    public GraphInfo getPrimaryGraphInfo() {
        return delegate.getPrimaryGraphInfo();
    }

    @Override
    public List<GraphInfo> getGraphInfos() {
        return delegate.getGraphInfos();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public GenericGraphContainer asGenericGraphContainer() {
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDomainGraphContainer that = (AbstractDomainGraphContainer) o;
        return Objects.equals(this.delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }
    
    public static class AbstractDomainGraphContainerBuilder
        <T extends AbstractDomainGraphContainerBuilder,
         G extends AbstractDomainGraph<? extends AbstractDomainVertex, ? extends AbstractDomainEdge>> {
        
        protected final GenericGraphContainerBuilder builder = GenericGraphContainer.builder();
        
        protected AbstractDomainGraphContainerBuilder() {}
       
        public T id(String id) {
            builder.id(id);
            return (T)this;
        }
        
        public T label(String label) {
            builder.label(label);
            return (T)this;
        }
        
        public T description(String description) {
            builder.description(description);
            return (T)this;
        }
       
        public T property(String name, Object value){
            builder.property(name, value);
            return (T)this;
        }
        
        public T properties(Map<String, Object> properties){
            builder.properties(properties);
            return (T)this;
        }
        
        public T containerInfo(GraphContainerInfo containerInfo) {
            builder.applyContainerInfo(containerInfo);
            return (T)this;
        }
        
        public T addGraph(G graph) {
            builder.addGraph(graph.asGenericGraph());
            return (T)this;
        }
    } 
    
}
