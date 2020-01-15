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

import java.util.Objects;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.domain.simple.SimpleDomainEdge;

/**
* Acts as a domain specific view on a {@link GenericEdge}.
* Can be extended by a domain specific edge class.
* It contains no data of it's own but operates on the data of it's wrapped {@link GenericEdge}.
**/
public abstract class AbstractDomainEdge implements Edge {

    protected final GenericEdge delegate;

    public AbstractDomainEdge(GenericEdge genericEdge) {
        this.delegate = genericEdge;
    }
    
    @Override
    public String getNamespace() {
        return delegate.getNamespace();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public VertexRef getSource() {
        return delegate.getSource();
    }

    @Override
    public VertexRef getTarget() {
        return delegate.getTarget();
    }

    @Override
    public GenericEdge asGenericEdge() {
        return delegate;
    }

    public String getLabel(){
        return delegate.getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleDomainEdge that = (SimpleDomainEdge) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
    
    public static class AbstractDomainEdgeBuilder<T extends AbstractDomainElementBuilder<?>> extends AbstractDomainElementBuilder<T> {
        
        protected VertexRef source;
        protected VertexRef target;
        
        protected AbstractDomainEdgeBuilder() {}
        
        public T source(VertexRef source) {
            this.source = source;
            return (T)this;
        }

        public T source(String namespace, String vertexId) {
            return source(new VertexRef(namespace, vertexId));
        }

        public T target(VertexRef target) {
            this.target = target;
            return (T)this;
        }

        public T target(String namespace, String vertexId) {
            return target(new VertexRef(namespace, vertexId));
        }
        
        public T id(String id) {
            this.properties.put(GenericProperties.ID, id);
            return (T)this;
        }
    }
}
