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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;

public interface CriteriaUtils {
    static WrappedVertexHopCriteria getWrappedVertexHopCriteria(GraphContainer graphContainer) {
        final Set<VertexHopCriteria> vertexHopCriterias = Criteria.getCriteriaForGraphContainer(graphContainer, VertexHopCriteria.class);
        return new WrappedVertexHopCriteria(vertexHopCriterias);
    }

    static CollapsibleCriteria[] getCollapsedCriteriaForContainer(GraphContainer graphContainer) {
        return getCollapsedCriteria(graphContainer.getCriteria());
    }

    static CollapsibleCriteria[] getCollapsedCriteria(Criteria[] criteria) {
        return getCollapsibleCriteria(criteria, true);
    }

    static CollapsibleCriteria[] getCollapsibleCriteriaForContainer(GraphContainer graphContainer) {
        return getCollapsibleCriteria(graphContainer.getCriteria());
    }

    static CollapsibleCriteria[] getCollapsibleCriteria(Criteria[] criteria) {
        return getCollapsibleCriteria(criteria, false);
    }

    static CollapsibleCriteria[] getCollapsibleCriteria(Criteria[] criteria, boolean onlyCollapsed) {
        List<CollapsibleCriteria> retval = new ArrayList<CollapsibleCriteria>();
        if (criteria != null) {
            for (Criteria criterium : criteria) {
                try {
                    CollapsibleCriteria hopCriteria = (CollapsibleCriteria)criterium;
                    if (onlyCollapsed) {
                        if (hopCriteria.isCollapsed()) {
                            retval.add(hopCriteria);
                        }
                    } else {
                        retval.add(hopCriteria);
                    }
                } catch (ClassCastException e) {}
            }
        }
        return retval.toArray(new CollapsibleCriteria[0]);
    }
}
