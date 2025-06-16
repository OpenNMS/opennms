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
