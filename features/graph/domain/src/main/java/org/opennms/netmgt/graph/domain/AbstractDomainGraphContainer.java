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
