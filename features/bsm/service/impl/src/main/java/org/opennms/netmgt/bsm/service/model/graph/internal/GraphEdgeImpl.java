/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.model.graph.internal;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;

public class GraphEdgeImpl extends GraphElement implements GraphEdge {

    private final MapFunction m_mapFunction;
    private final int m_weight;
    private final String m_friendlyName;

    public GraphEdgeImpl(MapFunction mapFunction) {
        m_mapFunction = mapFunction;
        m_weight = 1;
        m_friendlyName = null;
    }

    public GraphEdgeImpl(ReadOnlyEdge edge) {
        m_mapFunction = edge.getMapFunction();
        m_weight = edge.getWeight();
        m_friendlyName = edge.getFriendlyName();
    }

    public Status getStatus() {
        return m_status;
    }

    private GraphEdgeImpl(MapFunction mapFunction, int weight) {
        m_mapFunction = mapFunction;
        m_weight = weight;
        // We set the default status to INDETERMINATE as the map function may not be IDENTITY
        // This allows to distinguish between "NO STATUS" and a "DEFINED STATUS"
        setStatus(Status.INDETERMINATE);
    }

    public MapFunction getMapFunction() {
        return m_mapFunction;
    }

    public int getWeight() {
        return m_weight;
    }

    @Override
    public String getFriendlyName() {
        return m_friendlyName;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("mapFunction", m_mapFunction)
                .add("weight", m_weight)
                .toString();
    }
}
