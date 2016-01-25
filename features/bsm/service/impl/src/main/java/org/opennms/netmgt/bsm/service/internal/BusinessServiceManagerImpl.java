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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdge;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdge;
import org.opennms.netmgt.bsm.persistence.api.SingleReductionKeyEdge;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteria;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.internal.edge.ChildEdgeImpl;
import org.opennms.netmgt.bsm.service.internal.edge.IpServiceEdgeImpl;
import org.opennms.netmgt.bsm.service.internal.edge.ReductionKeyEdgeImpl;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Node;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;
import org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.bsm.service.model.mapreduce.MapFunction;
import org.opennms.netmgt.bsm.service.model.mapreduce.ReductionFunction;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Transactional
public class BusinessServiceManagerImpl implements BusinessServiceManager {
    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private ReductionFunctionDao reductionFunctionDao;

    @Autowired
    private BusinessServiceEdgeDao edgeDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private BusinessServiceStateMachine businessServiceStateMachine;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private EventForwarder eventForwarder;

    @Override
    public List<BusinessService> getAllBusinessServices() {
        return getDao().findAll().stream()
                .map(s -> new BusinessServiceImpl(this, s))
                .collect(Collectors.toList());
    }

    @Override
    public BusinessService createBusinessService() {
        return new BusinessServiceImpl(this, new BusinessServiceEntity());
    }

    @Override
    public <T extends Edge> T createEdge(Class<T> type, BusinessService source, MapFunction mapFunction) {
        T edge = null;
        if (type == IpServiceEdge.class) {
            edge = (T) new IpServiceEdgeImpl(this, new org.opennms.netmgt.bsm.persistence.api.IPServiceEdge());
        }
        if (type == ChildEdge.class) {
            edge = (T) new ChildEdgeImpl(this, new BusinessServiceChildEdge());
        }
        if (type == ReductionKeyEdge.class) {
            edge = (T) new ReductionKeyEdgeImpl(this, new SingleReductionKeyEdge());
        }
        if (edge != null) {
            edge.setSource(source);
            edge.setMapFunction(mapFunction);
            return edge;
        }
        throw new IllegalArgumentException("Could not create edge for type " + type);
    }

    @Override
    public void saveBusinessService(BusinessService service) {
        BusinessServiceEntity entity = getBusinessServiceEntity(service);
        getDao().saveOrUpdate(entity);
    }

    @Override
    public Set<BusinessService> getParentServices(Long id) {
        BusinessServiceEntity entity = getBusinessServiceEntity(id);
        return businessServiceDao.findParents(entity)
            .stream()
            .map(bs -> new BusinessServiceImpl(this, bs))
            .collect(Collectors.toSet());
    }

    @Override
    public BusinessService getBusinessServiceById(Long id) {
        BusinessServiceEntity entity = getBusinessServiceEntity(id);
        return new BusinessServiceImpl(this, entity);
    }

    @Override
    public void deleteBusinessService(BusinessService businessService) {
        BusinessServiceEntity entity = getBusinessServiceEntity(businessService);
        // remove all parent -> child associations
        for(BusinessServiceEntity parent : getDao().findParents(entity)) {
            List<BusinessServiceChildEdge> collect = parent.getChildEdges().stream().filter(e -> entity.equals(e.getChild())).collect(Collectors.toList());
            collect.forEach(x -> {
                parent.removeEdge(x);
                edgeDao.delete(x); // we need to delete this edge manually as they cannot be deleted automatically
            });
        }
        // edges need not to be deleted manually, deletes will be cascaded
        getDao().delete(entity);
    }

    @Override
    public void setReductionKeyEdges(BusinessService businessService, Set<ReductionKeyEdge> reductionKeyEdges) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(businessService);
        for (final SingleReductionKeyEdge e : parentEntity.getReductionKeyEdges()) {
            parentEntity.removeEdge(e);
        }
        reductionKeyEdges.forEach(e -> parentEntity.addEdge(((ReductionKeyEdgeImpl) e).getEntity()));
    }

    @Override
    public boolean addReductionKeyEdge(BusinessService businessService, String reductionKey, MapFunction mapFunction) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(businessService);

        // Create the edge
        ReductionKeyEdgeImpl edge = (ReductionKeyEdgeImpl) createEdge(ReductionKeyEdge.class, businessService, mapFunction);
        edge.setReductionKey(reductionKey);

        // if already exists, no update
        if (parentEntity.getIpServiceEdges().contains(edge.getEntity())) {
            return false;
        }

        parentEntity.addEdge(edge.getEntity());
        return true;
    }

    @Override
    public void setIpServiceEdges(BusinessService businessService, Set<IpServiceEdge> ipServiceEdges) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(businessService);
        for (final IPServiceEdge e : entity.getIpServiceEdges()) {
            entity.removeEdge(e);
        }
        ipServiceEdges.forEach(e -> entity.addEdge(((IpServiceEdgeImpl) e).getEntity()));
    }

    @Override
    public boolean addIpServiceEdge(BusinessService businessService, IpService ipService, MapFunction mapFunction) {
        // verify that exists
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(businessService);
        final OnmsMonitoredService monitoredService = getMonitoredService(ipService);

        // Create the edge
        IpServiceEdge edge = createEdge(IpServiceEdge.class, businessService, mapFunction);
        edge.setIpService(ipService);

        // if already exists, no update
        if (parentEntity.getIpServiceEdges().contains(((IpServiceEdgeImpl)edge).getEntity())) {
            return false;
        }
        parentEntity.addEdge(((IpServiceEdgeImpl)edge).getEntity());
        return true;
    }

    @Override
    public void setChildEdges(BusinessService parentService, Set<ChildEdge> childEdges) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(parentService);
        for (final BusinessServiceChildEdge e : parentEntity.getChildEdges()) {
            parentEntity.removeEdge(e);
        }
        childEdges.forEach(e -> parentEntity.addEdge(((ChildEdgeImpl) e).getEntity()));
    }

    @Override
    public boolean addChildEdge(BusinessService parentService, BusinessService childService, MapFunction mapFunction) {
        // verify that exists
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(parentService);
        final BusinessServiceEntity childEntity = getBusinessServiceEntity(childService);

        // Create the edge
        ChildEdge childEdge = createEdge(ChildEdge.class, parentService, mapFunction);
        childEdge.setChild(childService);

        // Verify no loop
        if (this.checkDescendantForLoop(parentEntity, childEntity)) {
            throw new IllegalArgumentException("Service will form a loop");
        }
        // if already exists, no update
        if (parentEntity.getEdges().contains(((ChildEdgeImpl)childEdge).getEntity())) {
            return false;
        }
        parentEntity.addEdge(((ChildEdgeImpl)childEdge).getEntity());
        return true;
    }

    private boolean checkDescendantForLoop(final BusinessServiceEntity parent,
                                           final BusinessServiceEntity descendant) {
        if (parent.equals(descendant)) {
            return true;
        }

        for (BusinessServiceChildEdge eachChildEdge : descendant.getChildEdges()) {
            return this.checkDescendantForLoop(parent, eachChildEdge.getChild());
        }

        return false;
    }

    @Override
    public Set<BusinessService> getFeasibleChildServices(final BusinessService service) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(service);
        return getDao().findAll()
                       .stream()
                       .filter(s -> !this.checkDescendantForLoop(entity, s))
                       .map(s -> new BusinessServiceImpl(this, s))
                       .collect(Collectors.<BusinessService>toSet());
    }

    @Override
    public Status getOperationalStatusForBusinessService(BusinessService service) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(service); // verify the entity exists
        final Status status = businessServiceStateMachine.getOperationalStatus(service);
        return status != null ? status : Status.INDETERMINATE;
    }

    @Override
    public Status getOperationalStatusForIPService(IpService ipService) {
        final OnmsMonitoredService monitoredService = getMonitoredService(ipService); // verify the entity exists
        final Status status = businessServiceStateMachine.getOperationalStatus(ipService);
        return status != null ? status : Status.INDETERMINATE;
    }

    @Override
    public List<IpService> getAllIpServices() {
        return monitoredServiceDao.findAll().stream()
                                  .map(s -> new IpServiceImpl(this, s))
                                  .collect(Collectors.toList());
    }

    @Override
    public IpService getIpServiceById(Integer id) {
        OnmsMonitoredService entity = getMonitoredService(id);
        return new IpServiceImpl(this, entity);
    }

    @Override
    public void triggerDaemonReload() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "BSM Master Page");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, "bsmd");
        eventForwarder.sendNow(eventBuilder.getEvent());
    }

    @Override
    public Status getOperationalStatusForReductionKey(String reductionKey) {
        return Status.INDETERMINATE; // TODO MVR implement... probably getOperationalStatus(Edge edge...)
    }

    @Override
    public List<MapFunction> listMapFunctions() {
        return Lists.newArrayList(new Identity(), new Increase(), new Decrease(), new SetTo(), new Ignore());
    }

    @Override
    public List<ReductionFunction> listReduceFunctions() {
        return Lists.newArrayList(new MostCritical(), new Threshold());
    }

    protected BusinessServiceDao getDao() {
        return this.businessServiceDao;
    }

    private BusinessServiceEntity getBusinessServiceEntity(BusinessService service) throws NoSuchElementException {
        return ((BusinessServiceImpl) service).getEntity();
    }

    private BusinessServiceEntity getBusinessServiceEntity(Long serviceId) throws NoSuchElementException {
        final BusinessServiceEntity entity = getDao().get(serviceId);
        if (entity == null) {
            throw new NoSuchElementException();
        }
        return entity;
    }

    private OnmsMonitoredService getMonitoredService(IpService ipService) throws NoSuchElementException {
        return ((IpServiceImpl) ipService).getEntity();
    }

    private OnmsMonitoredService getMonitoredService(Integer serviceId) throws NoSuchElementException {
        final OnmsMonitoredService monitoredService = monitoredServiceDao.get(serviceId);
        if (monitoredService == null) {
            throw new NoSuchElementException();
        }
        return monitoredService;
    }

    @Override
    public Node getNodeById(int nodeId) {
        return new NodeImpl(this, nodeDao.get(nodeId));
    }
}
