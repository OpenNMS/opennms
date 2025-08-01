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

import java.util.Objects;

import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainVertex;

/**
 * Acts as a domain specific view on a {@link GenericVertex}.
 * This is the most basic concrete subclass of {@link AbstractDomainVertex} and can be used as a reference for your own
 * domain vertex. It is a final class. If you need more functionality please extend {@link AbstractDomainVertex}.
 * Since it's delegate is immutable and this class holds no data of it's own it is immutable as well.
 */
public final class SimpleDomainVertex extends AbstractDomainVertex {

    public SimpleDomainVertex(GenericVertex genericVertex) {
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
        SimpleDomainVertex that = (SimpleDomainVertex) o;
        return Objects.equals(this.delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
    
    public static SimpleDomainVertexBuilder builder() {
        return new SimpleDomainVertexBuilder();
    }
    
    public static SimpleDomainVertex from(GenericVertex genericVertex) {
        return new SimpleDomainVertex(genericVertex);
    }
    
    public final static class SimpleDomainVertexBuilder extends AbstractDomainVertexBuilder<SimpleDomainVertexBuilder> {
                
        private SimpleDomainVertexBuilder() {}
        
        public SimpleDomainVertex build() {
            return new SimpleDomainVertex(GenericVertex.builder().properties(properties).build());
        }
    }
}
