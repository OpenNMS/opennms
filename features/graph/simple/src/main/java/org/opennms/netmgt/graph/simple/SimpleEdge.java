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

// TODO MVR fix package name opennsm vs opennms
package org.opennms.netmgt.graph.simple;

import java.util.Objects;

import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;

/**
 * Acts as a domain specific view on a GenericEdge.
 * This is the most basic concrete subclass of {@link AbstractDomainEdge} and can be used as a reference for your own
 * domain edge. It is a final class. If you need more functionality please extend AbstractDomainGraph.
 * Since it's delegate is immutable and this class holds no data of it's own it is immutable as well.
 */
public final class SimpleEdge extends AbstractDomainEdge {

    public SimpleEdge(GenericEdge genericEdge) {
        super(genericEdge);
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
        SimpleEdge that = (SimpleEdge) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
    
    public static SimpleEdgeBuilder builder() {
        return new SimpleEdgeBuilder();
    }
    
    public final static class SimpleEdgeBuilder extends AbstractDomainEdgeBuilder<SimpleEdgeBuilder> {
               
        private SimpleEdgeBuilder() {}
        
        public SimpleEdge build() {
            return new SimpleEdge(GenericEdge.builder().properties(properties).source(source).target(target).build());
        }
    }
}
