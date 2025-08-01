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

import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.domain.AbstractDomainEdge;

/**
 * Acts as a domain specific view on a {@link GenericEdge}.
 * This is the most basic concrete subclass of {@link AbstractDomainEdge} and can be used as a reference for your own
 * domain edge. It is a final class. If you need more functionality please extend {@link AbstractDomainEdge}.
 * Since it's delegate is immutable and this class holds no data of it's own it is immutable as well.
 */
public final class SimpleDomainEdge extends AbstractDomainEdge {

    public SimpleDomainEdge(GenericEdge genericEdge) {
        super(genericEdge);
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
    
    public static SimpleDomainEdgeBuilder builder() {
        return new SimpleDomainEdgeBuilder();
    }
    
    public static SimpleDomainEdge from(GenericEdge genericEdge) {
        return new SimpleDomainEdge(genericEdge);
    }
    
    public final static class SimpleDomainEdgeBuilder extends AbstractDomainEdgeBuilder<SimpleDomainEdgeBuilder> {
               
        private SimpleDomainEdgeBuilder() {}
        
        public SimpleDomainEdge build() {
            return new SimpleDomainEdge(GenericEdge.builder().properties(properties).source(source).target(target).build());
        }
    }
}
