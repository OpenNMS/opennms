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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.EdgeEntityVisitor;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.SingleReductionKeyEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionDao;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteria;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.internal.edge.AbstractEdge;
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
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.internal.BusinessServiceGraphImpl;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class BusinessServiceManagerImpl implements BusinessServiceManager {
    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private BusinessServiceEdgeDao edgeDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private MapFunctionDao mapFunctionDao;

    @Autowired
    private ReductionFunctionDao reductionFunctionDao;

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
    public List<BusinessService> search(BusinessServiceSearchCriteria businessServiceSearchCriteria) {
        Objects.requireNonNull(businessServiceSearchCriteria);
        return businessServiceSearchCriteria.apply(this, getAllBusinessServices());
    }

    @Override
    public List<BusinessService> findMatching(Criteria criteria) {
        criteria = transform(criteria);
        List<BusinessServiceEntity> all = getDao().findMatching(criteria);
        if (all == null) {
            return null;
        }
        return all.stream().map(e -> new BusinessServiceImpl(this, e)).collect(Collectors.toList());
    }

    @Override
    public int countMatching(Criteria criteria) {
        criteria = transform(criteria);
                return getDao().countMatching(criteria);
            }

            @Override
            public BusinessService createBusinessService() {
        return new BusinessServiceImpl(this, new BusinessServiceEntity());
    }

    @SuppressWarnings("unchecked")
    private <T extends Edge> T createEdge(Class<T> type, BusinessService source, MapFunction mapFunction, int weight) {
        T edge = null;
        if (type == IpServiceEdge.class) {
            edge = (T) new IpServiceEdgeImpl(this, new IPServiceEdgeEntity());
        }
        if (type == ChildEdge.class) {
            edge = (T) new ChildEdgeImpl(this, new BusinessServiceChildEdgeEntity());
        }
        if (type == ReductionKeyEdge.class) {
            edge = (T) new ReductionKeyEdgeImpl(this, new SingleReductionKeyEdgeEntity());
        }
        if (edge != null) {
            edge.setSource(source);
            edge.setMapFunction(mapFunction);
            edge.setWeight(weight);
            return edge;
        }
        throw new IllegalArgumentException("Could not create edge for type " + type);
    }

    @Override
    public Edge getEdgeById(Long edgeId) {
        BusinessServiceEdgeEntity edgeEntity = getBusinessServiceEdgeEntity(edgeId);
        return edgeEntity.accept(new EdgeEntityVisitor<Edge>() {

            @Override
            public Edge visit(BusinessServiceChildEdgeEntity edgeEntity) {
                return new ChildEdgeImpl(BusinessServiceManagerImpl.this, edgeEntity);
            }

            @Override
            public Edge visit(SingleReductionKeyEdgeEntity edge) {
                return new ReductionKeyEdgeImpl(BusinessServiceManagerImpl.this, edge);
            }

            @Override
            public Edge visit(IPServiceEdgeEntity edge) {
                return new IpServiceEdgeImpl(BusinessServiceManagerImpl.this, edge);
            }
        });
    }

    @Override
    public boolean deleteEdge(BusinessService source, Edge edge) {
        BusinessServiceEdgeEntity edgeEntity = getBusinessServiceEdgeEntity(edge);
        BusinessServiceEntity businessServiceEntity = getBusinessServiceEntity(source);

        // does not exist, no update necessary
        if (!businessServiceEntity.getEdges().contains(edgeEntity)) {
            return false;
        }

        // remove and update
        businessServiceEntity.removeEdge(edgeEntity);
        return true;
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
            List<BusinessServiceChildEdgeEntity> collect = parent.getChildEdges().stream().filter(e -> entity.equals(e.getChild())).collect(Collectors.toList());
            collect.forEach(x -> {
                parent.removeEdge(x);
                edgeDao.delete(x); // we need to delete this edge manually as they cannot be deleted automatically
            });
        }
        // edges of the entity are deleted automatically by hibernate
        getDao().delete(entity);
    }

    @Override
    public void setReductionKeyEdges(BusinessService businessService, Set<ReductionKeyEdge> reductionKeyEdges) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(businessService);
        for (final SingleReductionKeyEdgeEntity e : parentEntity.getReductionKeyEdges()) {
            parentEntity.removeEdge(e);
        }
        reductionKeyEdges.forEach(e -> parentEntity.addEdge(((ReductionKeyEdgeImpl) e).getEntity()));
    }

    @Override
    public boolean addReductionKeyEdge(BusinessService businessService, String reductionKey, MapFunction mapFunction, int weight) {
        return addReductionKeyEdge(businessService, reductionKey, mapFunction, weight, null);
    }

    @Override
    public boolean addReductionKeyEdge(BusinessService businessService, String reductionKey, MapFunction mapFunction, int weight, String friendlyName) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(businessService);

        // Create the edge
        final ReductionKeyEdgeImpl edge = (ReductionKeyEdgeImpl) createEdge(ReductionKeyEdge.class, businessService, mapFunction, weight);
        edge.setReductionKey(reductionKey);
        edge.setFriendlyName(friendlyName);

        // if already exists, no update
        final SingleReductionKeyEdgeEntity edgeEntity = getBusinessServiceEdgeEntity(edge);
        long count = parentEntity.getReductionKeyEdges().stream().filter(e -> e.equalsDefinition(edgeEntity)).count();
        if (count > 0) {
            return false;
        }
        parentEntity.addEdge(edge.getEntity());
        return true;
    }

    @Override
    public void setIpServiceEdges(BusinessService businessService, Set<IpServiceEdge> ipServiceEdges) {
        final BusinessServiceEntity entity = getBusinessServiceEntity(businessService);
        for (final IPServiceEdgeEntity e : entity.getIpServiceEdges()) {
            entity.removeEdge(e);
        }
        ipServiceEdges.forEach(e -> entity.addEdge(((IpServiceEdgeImpl) e).getEntity()));
    }

    @Override
    public boolean addIpServiceEdge(BusinessService businessService, IpService ipService, MapFunction mapFunction, int weight) {
        return addIpServiceEdge(businessService, ipService, mapFunction, weight, null);
    }

    @Override
    public boolean addIpServiceEdge(BusinessService businessService, IpService ipService, MapFunction mapFunction, int weight, String friendlyName) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(businessService);

        // Create the edge
        final IpServiceEdge edge = createEdge(IpServiceEdge.class, businessService, mapFunction, weight);
        edge.setIpService(ipService);
        edge.setFriendlyName(friendlyName);

        // if already exists, no update
        final IPServiceEdgeEntity edgeEntity = getBusinessServiceEdgeEntity(edge);
        long count = parentEntity.getIpServiceEdges().stream().filter(e -> e.equalsDefinition(edgeEntity)).count();
        if (count > 0) {
            return false;
        }
        parentEntity.addEdge(((IpServiceEdgeImpl)edge).getEntity());
        return true;
    }

    @Override
    public void setChildEdges(BusinessService parentService, Set<ChildEdge> childEdges) {
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(parentService);
        for (final BusinessServiceChildEdgeEntity e : parentEntity.getChildEdges()) {
            parentEntity.removeEdge(e);
        }
        childEdges.forEach(e -> parentEntity.addEdge(((ChildEdgeImpl) e).getEntity()));
    }

    @Override
    public boolean addChildEdge(BusinessService parentService, BusinessService childService, MapFunction mapFunction, int weight) {
        // verify that exists
        final BusinessServiceEntity parentEntity = getBusinessServiceEntity(parentService);
        final BusinessServiceEntity childEntity = getBusinessServiceEntity(childService);

        // Create the edge
        ChildEdge childEdge = createEdge(ChildEdge.class, parentService, mapFunction, weight);
        childEdge.setChild(childService);

        // Verify no loop
        if (this.checkDescendantForLoop(parentEntity, childEntity)) {
            throw new IllegalArgumentException("Service will form a loop");
        }
        // if already exists, no update
        final BusinessServiceChildEdgeEntity edgeEntity = getBusinessServiceEdgeEntity(childEdge);
        long count = parentEntity.getChildEdges().stream().filter(e -> e.equalsDefinition(edgeEntity)).count();
        if (count > 0) {
            return false;
        }
        parentEntity.addEdge(((ChildEdgeImpl)childEdge).getEntity());
        return true;
    }

    @Override
    public void removeEdge(final BusinessService businessService, final Edge edge) {
        final BusinessServiceEntity businessServiceEntity = getBusinessServiceEntity(businessService);
        final BusinessServiceEdgeEntity edgeEntity = getBusinessServiceEdgeEntity(edge);

        businessServiceEntity.removeEdge(edgeEntity);
    }

    private boolean checkDescendantForLoop(final BusinessServiceEntity parent,
                                           final BusinessServiceEntity descendant) {
        if (parent.equals(descendant)) {
            return true;
        }

        for (BusinessServiceChildEdgeEntity eachChildEdge : descendant.getChildEdges()) {
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
    public Status getOperationalStatus(BusinessService service) {
        final Status status = businessServiceStateMachine.getOperationalStatus(service);
        return status != null ? status : Status.INDETERMINATE;
    }

    @Override
    public Status getOperationalStatus(IpService ipService) {
        final Status status = businessServiceStateMachine.getOperationalStatus(ipService);
        return status != null ? status : Status.INDETERMINATE;
    }

    @Override
    public Status getOperationalStatus(String reductionKey) {
        final Status status = businessServiceStateMachine.getOperationalStatus(reductionKey);
        return status != null ? status : Status.INDETERMINATE;
    }

    @Override
    public Status getOperationalStatus(Edge edge) {
        final Status status = businessServiceStateMachine.getOperationalStatus(edge);
        return status != null ? status : Status.INDETERMINATE;
    }

    @Override
    public List<IpService> getAllIpServices() {
        return monitoredServiceDao.findAllServices().stream()
                                  .map(s -> new IpServiceImpl(this, s))
                                  .collect(Collectors.toList());
    }

    @Override
    public IpService getIpServiceById(Integer id) {
        OnmsMonitoredService entity = getMonitoredServiceEntity(id);
        return new IpServiceImpl(this, entity);
    }

    @Override
    public void triggerDaemonReload() {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "BSM Master Page");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, "bsmd");
        eventForwarder.sendNow(eventBuilder.getEvent());
    }

    @Override
    public Node getNodeById(Integer nodeId) {
        return new NodeImpl(getNodeEntity(nodeId));
    }

    @Override
    public BusinessServiceGraph getGraph(List<BusinessService> businessServices) {
        return new BusinessServiceGraphImpl(businessServices);
    }

    @Override
    public BusinessServiceGraph getGraph() {
        // Do not instantiate a new instance, or the status is not set
        return businessServiceStateMachine.getGraph();
    }

    @Override
    public BusinessServiceStateMachine getStateMachine() {
        return businessServiceStateMachine;
    }

    @Override
    public void setMapFunction(final Edge edge, final MapFunction mapFunction) {
        // This is a workaround for a hibernate bug which does not remove
        // orphan elements if the element is replaced using the setter. See:
        // https://hibernate.atlassian.net/browse/HHH-6484
        final BusinessServiceEdgeEntity edgeEntity = this.getBusinessServiceEdgeEntity(edge);

        final AbstractMapFunctionEntity prevMapFunctionEntity = edgeEntity.getMapFunction();
        if (prevMapFunctionEntity != null && prevMapFunctionEntity.getId() != null) {
            this.mapFunctionDao.delete(prevMapFunctionEntity);
        }

        final AbstractMapFunctionEntity mapFunctionEntity = new MapFunctionMapper().toPersistenceFunction(mapFunction);
        edgeEntity.setMapFunction(mapFunctionEntity);
    }

    @Override
    public void setReduceFunction(final BusinessService businessService, final ReductionFunction reductionFunction) {
        // This is a workaround for a hibernate bug which does not remove
        // orphan elements if the element is replaced using the setter. See:
        // https://hibernate.atlassian.net/browse/HHH-6484
        final BusinessServiceEntity entity = this.getBusinessServiceEntity(businessService);

        final AbstractReductionFunctionEntity prevReduceFunctionEntity = entity.getReductionFunction();
        if (prevReduceFunctionEntity != null && prevReduceFunctionEntity.getId() != null) {
            this.reductionFunctionDao.delete(prevReduceFunctionEntity);
        }

        final AbstractReductionFunctionEntity reduceFunctionEntity = new ReduceFunctionMapper().toPersistenceFunction(reductionFunction);
        entity.setReductionFunction(reduceFunctionEntity);
    }

    protected BusinessServiceDao getDao() {
        return this.businessServiceDao;
    }

    private OnmsNode getNodeEntity(Integer nodeId) {
        Objects.requireNonNull(nodeId);
        final OnmsNode entity = nodeDao.get(nodeId);
        if (entity == null) {
            throw new NoSuchElementException("OnmsNode with id " + nodeId);
        }
        return entity;
    }

    private BusinessServiceEdgeEntity getBusinessServiceEdgeEntity(Edge edge) {
        return ((AbstractEdge<?>) edge).getEntity();
    }

    private BusinessServiceEdgeEntity getBusinessServiceEdgeEntity(Long edgeId) {
        Objects.requireNonNull(edgeId);
        BusinessServiceEdgeEntity edgeEntity = edgeDao.get(edgeId);
        if (edgeEntity == null) {
            throw new NoSuchElementException("BusinessServiceEdgeEntity with id " + edgeId);
        }
        return edgeEntity;
    }

    private IPServiceEdgeEntity getBusinessServiceEdgeEntity(IpServiceEdge ipServiceEdge) {
        return ((IpServiceEdgeImpl) ipServiceEdge).getEntity();
    }

    private BusinessServiceChildEdgeEntity getBusinessServiceEdgeEntity(ChildEdge childEdge) {
        return ((ChildEdgeImpl) childEdge).getEntity();
    }

    private SingleReductionKeyEdgeEntity getBusinessServiceEdgeEntity(ReductionKeyEdge reductionKeyEdge) {
        return ((ReductionKeyEdgeImpl) reductionKeyEdge).getEntity();
    }

    private BusinessServiceEntity getBusinessServiceEntity(BusinessService service) throws NoSuchElementException {
        return ((BusinessServiceImpl) service).getEntity();
    }

    private BusinessServiceEntity getBusinessServiceEntity(Long serviceId) throws NoSuchElementException {
        Objects.requireNonNull(serviceId);
        final BusinessServiceEntity entity = getDao().get(serviceId);
        if (entity == null) {
            throw new NoSuchElementException("BusinessServiceEntity with id " + serviceId);
        }
        return entity;
    }

    private OnmsMonitoredService getMonitoredServiceEntity(Integer serviceId) throws NoSuchElementException {
        Objects.requireNonNull(serviceId);
        final OnmsMonitoredService monitoredService = monitoredServiceDao.get(serviceId);
        if (monitoredService == null) {
            throw new NoSuchElementException("OnmsMonitoredService with id " + serviceId);
        }
        return monitoredService;
    }

    /**
     * The criteria is build on BusinessService classes.
     * However we want to use the dao to filter. Therefore we have to perform a mapping from BusinessService to BusinessServiceEntity.
     *
     * @param input
     * @return
     */
    private Criteria transform(Criteria input) {
        Criteria criteria = input.clone();
        criteria.setClass(BusinessServiceEntity.class);
        return criteria;
    }
}
