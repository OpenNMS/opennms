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
package org.opennms.features.topology.plugins.topo.bsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.simple.SimpleGraph;

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;

public class BusinessServiceGraph extends SimpleGraph {

    public BusinessServiceGraph() {
        super(BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE);
    }

    @Override
    public Vertex getVertex(VertexRef reference, Criteria... criteria) {
        Vertex theVertex = super.getVertex(reference, criteria);
        return filter(theVertex, criteria);
    }

    @Override
    public Vertex getVertex(String namespace, String id) {
        // Hack, as the id may not be prefixed with the type.
        // In these cases we assume a Business Service and update the id
        // We need to do this, otherwise linking from outside the Topology UI may not work
        if (!isValidVertexId(id)) {
            id = AbstractBusinessServiceVertex.Type.BusinessService + ":" + id;
        }
        return super.getVertex(namespace, id);
    }

    @Override
    public List<Vertex> getVertices(Criteria... criteria) {
        List<Vertex> vertices = super.getVertices(criteria);
        Collection<Vertex> filter = filter(vertices, criteria);
        return new ArrayList<>(filter);
    }

    @Override
    public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
        List<Vertex> vertices = super.getVertices(references, criteria);
        Collection<Vertex> filteredVertices = filter(vertices, criteria);
        return new ArrayList<>(filteredVertices);
    }

    private <T extends VertexRef> Collection<T> filter(List<T> references, final Criteria... criteria) {
        return Collections2.filter(references, input -> filter(input, criteria) != null);
    }

    private <T extends VertexRef> T filter(T refToFilter, Criteria... criteria) {
        // if we are supposed to hide leafs...
        if (hideLeafElement(Arrays.asList(criteria))) {
            AbstractBusinessServiceVertex vertex = (AbstractBusinessServiceVertex) refToFilter;
            //... and we are actually a leaf, but no business service
            if (vertex.isLeaf() && !(vertex instanceof BusinessServiceVertex)) {
                return null; // ... we filter
            }
            return refToFilter; // ... otherwise we do not
        }
        return refToFilter; // we are not supposed to filter
    }

    private boolean hideLeafElement(List<Criteria> criteria) {
        return criteria.stream().filter(e -> e instanceof BusinessServicesHideLeafsCriteria).findFirst().isPresent();
    }

    // Verify if the id starts with the type name
    private boolean isValidVertexId(String id) {
        if (Strings.isNullOrEmpty(id)) {
            return false;
        }
        for (AbstractBusinessServiceVertex.Type eachType : AbstractBusinessServiceVertex.Type.values()) {
            if (id.startsWith(eachType.name())) {
                return true;
            }
        }
        return false;
    }
}
