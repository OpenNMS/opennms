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

package org.opennms.netmgt.graph.simple;

import java.util.Objects;

import org.opennms.netmgt.graph.api.generic.GenericVertex;

/**
 * Acts as a domain specific view on a GenericVertex.
 * This is the most basic concrete subclass of {@link AbstractDomainVertex} and can be used as a reference for your own
 * domain vertex. It is a final class. If you need more functionality please extend AbstractDomainVertex.
 * Since it's delegate is immutable and this class holds no data of it's own it is immutable as well.
 */
public final class SimpleVertex extends AbstractDomainVertex {

    public SimpleVertex(GenericVertex genericVertex) {
        super(genericVertex);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleVertex that = (SimpleVertex) o;
        return Objects.equals(this.delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
    
    public static SimpleVertexBuilder builder() {
        return new SimpleVertexBuilder();
    }
    
    public static SimpleVertex from(GenericVertex genericVertex) {
        return new SimpleVertex(genericVertex);
    }
    
    public final static class SimpleVertexBuilder extends AbstractDomainVertexBuilder<SimpleVertexBuilder> {
                
        private SimpleVertexBuilder() {}
        
        public SimpleVertex build() {
            return new SimpleVertex(GenericVertex.builder().properties(properties).build());
        }
    }
}
