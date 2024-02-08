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
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.VertexRef;

/**
 * Wrapper class to wrap a bunch of {@link VertexHopCriteria}.
 * There may be multiple {@link VertexHopCriteria} objects available.
 * However in the end it is easier to use this criteria object to wrap all available {@link VertexHopCriteria}
 * instead of iterating over all all the time and determine all vertices.
 */
public class WrappedVertexHopCriteria extends VertexHopCriteria {

    private final Set<VertexHopCriteria> criteriaList;

    public WrappedVertexHopCriteria(Set<VertexHopCriteria> vertexHopCriterias) {
        super("Wrapped Vertex Hop Criteria for all VertexHopCriteria in the currently selected GraphProvider");
        criteriaList = Objects.requireNonNull(vertexHopCriterias);
    }

    public void addCriteria(VertexHopCriteria criteria) {
        this.criteriaList.add(criteria);
    }

    @Override
    public Set<VertexRef> getVertices() {
        Set<VertexRef> vertices = criteriaList.stream()
                .flatMap(criteria -> criteria.getVertices().stream())
                .collect(Collectors.toSet());
        return vertices;
    }

    @Override
    public String getNamespace() {
        return "$wrapped$";
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteriaList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof WrappedVertexHopCriteria) {
            WrappedVertexHopCriteria other = (WrappedVertexHopCriteria) obj;
            return Objects.equals(criteriaList, other.criteriaList);
        }
        return false;
    }

    public boolean contains(VertexRef vertexRef) {
        return getVertices().contains(vertexRef);
    }
}
