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

package org.opennms.netmgt.bsm.mock;

import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MockBusinessService implements BusinessService {
    private final long m_id;
    private String m_name;
    private ReductionFunction m_reductionFunction = new HighestSeverity();
    private Map<String, String> attributes = Maps.newHashMap();
    private Set<Edge> m_edges = Sets.newHashSet();

    public MockBusinessService(long id) {
        m_id = id;
    }

    @Override
    public Long getId() {
        return m_id;
    }

    public void setName(String name) {
        m_name = name;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Set<BusinessService> getChildServices() {
        return Sets.newHashSet();
    }

    @Override
    public Set<BusinessService> getParentServices() {
        return Sets.newHashSet();
    }

    @Override
    public void save() {

    }

    @Override
    public void delete() {

    }

    @Override
    public Status getOperationalStatus() {
        return null;
    }

    @Override
    public String getName() {
        return m_name != null ? m_name : String.valueOf(m_id);
    }

    public void setReductionFunction(ReductionFunction reduce) {
        m_reductionFunction = reduce;
    }

    @Override
    public ReductionFunction getReduceFunction() {
        return m_reductionFunction;
    }

    @Override
    public void setReduceFunction(ReductionFunction reductionFunction) {
        m_reductionFunction = reductionFunction;
    }

    @Override
    public void setIpServiceEdges(Set<IpServiceEdge> ipServiceEdges) {

    }

    @Override
    public void addIpServiceEdge(IpService ipService, MapFunction mapFunction, int weight, String friendlyName) {

    }

    @Override
    public void setReductionKeyEdges(Set<ReductionKeyEdge> reductionKeyEdges) {

    }

    @Override
    public void addReductionKeyEdge(String reductionKey, MapFunction mapFunction, int weight, String friendlyName) {

    }

    @Override
    public void setChildEdges(Set<ChildEdge> childEdges) {

    }

    @Override
    public void addChildEdge(BusinessService child, MapFunction mapFunction, int weight) {

    }

    @Override
    public void removeEdge(Edge edge) {
        m_edges.remove(edge);
    }

    @Override
    public Set<ReductionKeyEdge> getReductionKeyEdges() {
        return Sets.newHashSet();
    }

    @Override
    public Set<IpServiceEdge> getIpServiceEdges() {
        return null;
    }

    @Override
    public Set<ChildEdge> getChildEdges() {
        return null;
    }

    public void setEdges(Set<Edge> edges) {
        m_edges = edges;
    }

    public void addEdge(Edge edge) {
        m_edges.add(edge);
    }

    @Override
    public Set<Edge> getEdges() {
        return m_edges;
    }

    @Override
    public String toString() {
        return String.format("MockBusinessService[id=%d]", m_id);
    }
}
