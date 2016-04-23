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

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

public class GraphVertexImpl extends GraphElement implements GraphVertex, Comparable<GraphVertexImpl> {
    private final BusinessService m_businessService;
    private final IpService m_ipService;
    private final String m_reductionKey;
    private ReductionFunction m_reductionFunction;
    int m_level = -1;

    protected GraphVertexImpl(ReductionFunction reduceFunction, BusinessService businessService) {
        this(reduceFunction, businessService, null, null);
    }

    protected GraphVertexImpl(ReductionFunction reduceFunction, IpService ipService) {
        this(reduceFunction, null, ipService, null);
    }

    protected GraphVertexImpl(ReductionFunction reduceFunction, String reductionKey) {
        this(reduceFunction, null, null, reductionKey);
    }

    public GraphVertexImpl(ReductionFunction reduceFunction, BusinessService businessService, IpService ipService, String reductionKey) {
        m_businessService = businessService;
        m_ipService = ipService;
        m_reductionKey = reductionKey;
        m_reductionFunction = reduceFunction;
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
    public BusinessService getBusinessService() {
        return m_businessService;
    }

    @Override
    public IpService getIpService() {
        return m_ipService;
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
                .add("businessService", m_businessService)
                .add("ipService", m_ipService)
                .add("reductionKey", m_reductionKey)
                .add("level", m_level)
                .add("reductionFunction", m_reductionFunction)
                .toString();
    }

    /**
     * This is used to ensure that list of vertices returned by
     * the RCA and IA algorithms are in consistent order.
     */
    @Override
    public int compareTo(GraphVertexImpl other) {
        int i = getBusinessService() == null ?
                (other.getBusinessService() == null ? 0 : -1) :
                (other.getBusinessService() == null ? 1 : getBusinessService().getId().compareTo(other.getBusinessService().getId()));
        if (i != 0) return i;

        i = getIpService() == null ?
                (other.getIpService() == null ? 0 : -1) :
                (other.getIpService() == null ? 1 : Integer.compare(getIpService().getId(), other.getIpService().getId()));
        if (i != 0) return i;

        i = getReductionKey() == null ?
                (other.getReductionKey() == null ? 0 : -1) :
                (other.getReductionKey() == null ? 1 : getReductionKey().compareTo(other.getReductionKey()));
        return i;
    }
}
