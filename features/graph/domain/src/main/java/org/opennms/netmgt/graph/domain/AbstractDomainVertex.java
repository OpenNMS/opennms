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
