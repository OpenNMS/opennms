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

import org.opennms.netmgt.bsm.service.model.ReadOnlyBusinessService;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyEdge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class GraphVertexImpl extends GraphElement implements GraphVertex {
    private final ReductionFunction m_reductionFunction;
    int m_level = -1;

    private final ReadOnlyEdge m_edge;
    private final String m_reductionKey;

    private final ReadOnlyBusinessService m_businessService;

    public GraphVertexImpl(ReductionFunction reductionFunction, String reductionKey, ReadOnlyEdge edge) {
        m_reductionFunction = reductionFunction;
        m_reductionKey = reductionKey;
        m_edge = edge;
        m_businessService = null;
    }

    public GraphVertexImpl(ReadOnlyBusinessService businessService) {
        m_reductionFunction = businessService.getReduceFunction();
        m_reductionKey = null;
        m_edge = null;
        m_businessService = businessService;
    }

    @Override
    public ReductionFunction getReductionFunction() {
        return m_reductionFunction;
    }

    @Override
    public String getReductionKey() {
        return m_reductionKey;
    }

    @Override
    public ReadOnlyEdge getEdge() {
        return m_edge;
    }

    @Override
    public ReadOnlyBusinessService getBusinessService() {
        return m_businessService;
    }

    public void setLevel(int level) {
        m_level = level;
    }

    @Override
    public int getLevel() {
        return m_level;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("edge", m_edge)
                .add("reductionKey", m_reductionKey)
                .add("businessService", m_businessService)
                .add("level", m_level)
                .add("reductionFunction", m_reductionFunction)
                .toString();
    }
}
