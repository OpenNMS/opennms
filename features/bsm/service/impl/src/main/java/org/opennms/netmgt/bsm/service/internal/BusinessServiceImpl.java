/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.internal;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.edge.ChildEdgeImpl;
import org.opennms.netmgt.bsm.service.internal.edge.IpServiceEdgeImpl;
import org.opennms.netmgt.bsm.service.internal.edge.ReductionKeyEdgeImpl;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;

import com.google.common.collect.Sets;

public class BusinessServiceImpl implements BusinessService {

    private final BusinessServiceManager m_manager;

    private final BusinessServiceEntity m_entity;

    public BusinessServiceImpl(final BusinessServiceManager manager,
                               final BusinessServiceEntity entity) {
        this.m_manager = manager;
        this.m_entity = entity;
    }

    public BusinessServiceEntity getEntity() {
        return m_entity;
    }

    @Override
    public Long getId() {
        return m_entity.getId();
    }

    @Override
    public String getName() {
        return m_entity.getName();
    }

    @Override
    public void setName(String name) {
        m_entity.setName(name);
    }

    @Override
    public Map<String, String> getAttributes() {
        return m_entity.getAttributes();
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        m_entity.setAttributes(attributes);
    }

    @Override
    public Set<BusinessService> getChildServices() {
        return getChildEdges().stream().map(ChildEdge::getChild).collect(Collectors.toSet());
    }

    @Override
    public Set<BusinessService> getParentServices() {
        return m_manager.getParentServices(getId());
    }

    @Override
    public void save() {
        this.m_manager.saveBusinessService(this);
    }

    @Override
    public void delete() {
        this.m_manager.deleteBusinessService(this);
    }

    @Override
    public Status getOperationalStatus() {
        return m_manager.getOperationalStatus(this);
    }

    @Override
    public ReductionFunction getReduceFunction() {
        return new ReduceFunctionMapper().toServiceFunction(getEntity().getReductionFunction());
    }

    @Override
    public void setReduceFunction(ReductionFunction reductionFunction) {
        m_manager.setReduceFunction(this, reductionFunction);
    }

    @Override
    public Set<Edge> getEdges() {
        Set<org.opennms.netmgt.bsm.service.model.edge.Edge> edges = Sets.newHashSet();
        edges.addAll(getIpServiceEdges());
        edges.addAll(getReductionKeyEdges());
        edges.addAll(getChildEdges());
        return edges;
    }

    @Override
    public Set<org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge> getIpServiceEdges() {
        return m_entity.getIpServiceEdges()
                .stream()
                .map(edge -> new IpServiceEdgeImpl(m_manager, edge))
                .collect(Collectors.toSet());
    }

    @Override
    public void setIpServiceEdges(Set<IpServiceEdge> ipServiceEdges) {
        m_manager.setIpServiceEdges(this, ipServiceEdges);
    }

    @Override
    public void addIpServiceEdge(IpService ipService, MapFunction mapFunction, int weight, String friendlyName) {
        m_manager.addIpServiceEdge(this, ipService, mapFunction, weight, friendlyName);
    }

    @Override
    public Set<ReductionKeyEdge> getReductionKeyEdges() {
        return m_entity.getReductionKeyEdges()
                .stream()
                .map(edge -> new ReductionKeyEdgeImpl(m_manager, edge))
                .collect(Collectors.toSet());
    }

    @Override
    public void setReductionKeyEdges(Set<ReductionKeyEdge> reductionKeyEdges) {
        m_manager.setReductionKeyEdges(this, reductionKeyEdges);
    }

    @Override
    public void addReductionKeyEdge(String reductionKey, MapFunction mapFunction, int weight, String friendlyName) {
        m_manager.addReductionKeyEdge(this, reductionKey, mapFunction, weight, friendlyName);
    }

    @Override
    public Set<ChildEdge> getChildEdges() {
        return m_entity.getChildEdges()
                .stream()
                .map(edge -> new ChildEdgeImpl(m_manager, edge))
                .collect(Collectors.toSet());
    }

    @Override
    public void setChildEdges(Set<ChildEdge> childEdges) {
        m_manager.setChildEdges(this, childEdges);
    }

    @Override
    public void addChildEdge(BusinessService child, MapFunction mapFunction, int weight) {
        m_manager.addChildEdge(this, child, mapFunction, weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        final BusinessServiceImpl other = (BusinessServiceImpl) obj;
        return Objects.equals(getEntity(), other.getEntity());
    }

    @Override
    public int hashCode() {
        return getEntity().hashCode();
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", this.getId())
                .add("name", this.getName())
                .add("attributes", this.getAttributes())
                .add("edges", this.getEdges())
                .add("operationalStatus", this.getOperationalStatus())
                .toString();
    }

    @Override
    public void removeEdge(final Edge edge) {
        m_manager.removeEdge(this, edge);
    }
}
