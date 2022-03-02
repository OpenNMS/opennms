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

import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.aware.NodeRefAware;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.NodeInfo;

/**
* Acts as a domain specific view on a {@link GenericVertex}.
* Can be extended by a domain specific vertex class.
* It contains no data of it's own but operates on the data of it's wrapped {@link GenericVertex}.
**/
public class AbstractDomainVertex implements Vertex, NodeRefAware {
    
    protected final GenericVertex delegate;

    public AbstractDomainVertex(GenericVertex genericVertex) {
        this.delegate = genericVertex;
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
    public VertexRef getVertexRef(){
        return delegate.getVertexRef();
    }

    public String getLabel() {
        return delegate.getProperty(GenericProperties.LABEL);
    }

    @Override
    public final GenericVertex asGenericVertex() {
        return delegate;
    }

    @Override
    public NodeRef getNodeRef() {
        return delegate.getNodeRef();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDomainVertex that = (AbstractDomainVertex) o;
        return Objects.equals(this.delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
    
    public abstract static class AbstractDomainVertexBuilder<T extends AbstractDomainElementBuilder> extends AbstractDomainElementBuilder<T> { 
        
        protected AbstractDomainVertexBuilder() {}
        
        public T nodeInfo(NodeInfo nodeInfo) {
            this.properties.put(GenericProperties.NODE_INFO, nodeInfo);
            return (T) this;
        }
        
        public T nodeRef(String nodeRefString) {
            this.properties.put(GenericProperties.NODE_CRITERIA, nodeRefString);
            return (T) this;
        }

        public T nodeRef(int nodeId) {
            this.properties.put(GenericProperties.NODE_CRITERIA, Integer.toString(nodeId));
            return (T) this;
        }
    }
}
