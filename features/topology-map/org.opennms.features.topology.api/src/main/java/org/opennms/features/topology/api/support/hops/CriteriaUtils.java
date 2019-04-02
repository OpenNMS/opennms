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
