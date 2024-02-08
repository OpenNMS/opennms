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
package org.opennms.features.topology.api.support.hops;

import java.util.Objects;
import java.util.Set;

import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Sets;

/**
 * Helper criteria class to reference to existing VertexRefs.
 * This should be used anytime you want to add a vertex to the current focus (e.g. from the mouse context menu).
 */
public class DefaultVertexHopCriteria extends VertexHopCriteria {

    private final VertexRef vertexRef;

    public DefaultVertexHopCriteria(VertexRef vertexRef) {
        super(vertexRef.getId(), vertexRef.getLabel());
        this.vertexRef = vertexRef;
    }

    @Override
    public Set<VertexRef> getVertices() {
        return Sets.newHashSet(vertexRef);
    }

    @Override
    public String getNamespace() {
        return vertexRef.getNamespace();
    }

    @Override
    public int hashCode() {
        return vertexRef.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof DefaultVertexHopCriteria) {
            return Objects.equals(vertexRef, ((DefaultVertexHopCriteria) obj).vertexRef);
        }
        return false;
    }
}
