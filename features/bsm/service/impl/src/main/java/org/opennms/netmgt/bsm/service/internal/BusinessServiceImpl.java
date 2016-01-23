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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdge;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdge;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.edge.AbstractEdge;
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
import org.opennms.netmgt.bsm.service.model.mapreduce.MapFunction;
import org.opennms.netmgt.bsm.service.model.mapreduce.ReductionFunction;


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
        return getChildEdges().stream().map(edge -> edge.getChild()).collect(Collectors.toSet());
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
        return m_manager.getOperationalStatusForBusinessService(this);
    }

    @Override
    public void setLevel(int level) {
        getEntity().setLevel(level);
    }

    public int getLevel() {
        return getEntity().getLevel();
    }

    @Override
    public ReductionFunction getReduceFunction() {
        return new ReduceFunctionMapper().toServiceFunction(getEntity().getReductionFunction());
    }

    @Override
    public void setReduceFunction(ReductionFunction reductionFunction) {
        AbstractReductionFunctionEntity reductionFunctionEntity = new ReduceFunctionMapper().toPersistenceFunction(reductionFunction);
        getEntity().setReductionFunction(reductionFunctionEntity);
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
        Set<BusinessServiceEdge> allEdges = m_entity.getEdges();
        allEdges.removeAll(m_entity.getIpServiceEdges());
        allEdges.addAll(ipServiceEdges.stream().map(edge -> ((IpServiceEdgeImpl) edge).getEntity()).collect(Collectors.toSet()));
        m_entity.setEdges(allEdges);
    }

    @Override
    public void addIpServiceEdge(IpService ipService, MapFunction mapFunction) {
        IpServiceEdge edge = addEdge(IpServiceEdge.class, mapFunction);
        edge.setIpService(ipService);
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
        Set<BusinessServiceEdge> allEdges = m_entity.getEdges();
        allEdges.removeAll(m_entity.getReductionKeyEdges());
        allEdges.addAll(reductionKeyEdges.stream().map(edge -> ((ReductionKeyEdgeImpl) edge).getEntity()).collect(Collectors.toSet()));
        m_entity.setEdges(allEdges);
    }

    @Override
    public void addReductionKeyEdge(String reductionKey, MapFunction mapFunction) {
        ReductionKeyEdge edge = addEdge(ReductionKeyEdge.class, mapFunction);
        edge.setReductionKey(reductionKey);
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
        Set<BusinessServiceEdge> allEdges = m_entity.getEdges();
        allEdges.removeAll(m_entity.getChildEdges());
        allEdges.addAll(childEdges.stream().map(edge -> ((ChildEdgeImpl) edge).getEntity()).collect(Collectors.toSet()));
        m_entity.setEdges(allEdges);
    }

    @Override
    public void addChildEdge(BusinessService child, MapFunction mapFunction) {
        ChildEdge childEdge = addEdge(ChildEdge.class, mapFunction);
        childEdge.setChild(child);
    }

    @Override
    public void removeChildEdge(BusinessService childService) {
        Set<BusinessServiceChildEdge> childEdges = getEntity().getChildEdges();
        List<BusinessServiceChildEdge> abc = childEdges
                .stream()
                .filter(edge -> edge.getChild().equals(((BusinessServiceImpl) childService).getEntity()))
                .collect(Collectors.toList());
        for (BusinessServiceChildEdge e : abc) {
            getEntity().getEdges().remove(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final BusinessService other = (BusinessService) obj;

        if (getId() != null) {
            return getId().equals(other.getId());
        } else {
            return super.equals(other);
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        } else {
            return super.hashCode();
        }
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

    private <T extends Edge> T addEdge(Class<T> type, MapFunction mapFunction) {
        T edge = m_manager.createEdge(type, this, mapFunction);
        m_entity.getEdges().add(((AbstractEdge)edge).getEntity());
        return edge;
    }
}
