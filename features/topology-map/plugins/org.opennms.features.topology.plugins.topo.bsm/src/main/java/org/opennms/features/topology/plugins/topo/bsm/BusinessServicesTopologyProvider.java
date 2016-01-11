/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;


public class BusinessServicesTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "bsm";

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServicesTopologyProvider.class);

    private BusinessServiceManager businessServiceManager;
    // TODO MVR use BusinessServiceManager and the BusinessService model from the service layer
    private BusinessServiceDao businessServiceDao;

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServicesTopologyProvider(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        super(new BusinessServiceVertexProvider(TOPOLOGY_NAMESPACE), new SimpleEdgeProvider(TOPOLOGY_NAMESPACE));
        this.transactionAwareBeanProxyFactory = Objects.requireNonNull(transactionAwareBeanProxyFactory);
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), TOPOLOGY_NAMESPACE);
    }

    @Override
    public void save() {
       // we do not support save at the moment
    }
    
    private void load() {
        resetContainer();
        // we only consider root business services to build the graph
        Collection<BusinessService> businessServices = Collections2.filter(businessServiceDao.findAll(), new Predicate<BusinessService>() {
            @Override
            public boolean apply(BusinessService input) {
                return input.getParentServices().isEmpty();
            }
        });
        addBusinessServices(null, businessServices);
    }

    private void addBusinessServices(BusinessServiceVertex parentVertex, Collection<BusinessService> businessServices) {
        for (BusinessService eachBusinessService : businessServices) {
            addBusinessService(parentVertex, eachBusinessService);
        }
    }

    private void addBusinessService(BusinessServiceVertex parentVertex, BusinessService businessService) {
        // create the vertex itself
        BusinessServiceVertex businessServiceVertex = createVertex(businessService);
        addVertices(businessServiceVertex);

        // if we have a parent, connect the parent at the current business service vertex as well
        if (parentVertex != null) {
            parentVertex.addChildren(businessServiceVertex);
            addEdges(createConnection(parentVertex, businessServiceVertex));
        }

        // add ip services
        for (OnmsMonitoredService eachIpService : businessService.getIpServices()) {
            BusinessServiceVertex serviceVertex = createVertex(businessService, eachIpService);
            businessServiceVertex.addChildren(serviceVertex);
            addVertices(serviceVertex);

            // connect with businessService
            Edge edge = createConnection(businessServiceVertex, serviceVertex);
            addEdges(edge);
        }

        // add children to the hierarchy as well
        addBusinessServices(businessServiceVertex, businessService.getChildServices());
    }

    private Edge createConnection(BusinessServiceVertex v1, BusinessServiceVertex v2) {
        String id = String.format("connection:%s:%s", v1.getId(), v2.getId());
        Edge edge = new AbstractEdge(getEdgeNamespace(), id, v1, v2);
        edge.setTooltipText("LINK");
        return edge;
    }

    private BusinessServiceVertex createVertex(BusinessService parentBusinessService, OnmsMonitoredService monitoredService) {
        final BusinessServiceVertex serviceVertex = new BusinessServiceVertex(parentBusinessService.getId() + ":" + String.valueOf(monitoredService.getId()), monitoredService.getServiceName());
        serviceVertex.setIpAddress(monitoredService.getIpAddress().toString());
        serviceVertex.setLabel(monitoredService.getServiceName());
        serviceVertex.setTooltipText(String.format("Service '%s', IP: %s", monitoredService.getServiceName(), monitoredService.getIpAddress().toString()));
//      serviceVertex.setNodeID(eachIpService.getNodeId());
//      serviceVertex.setServiceType(eachIpService.getServiceType());
        return serviceVertex;
    }

    private BusinessServiceVertex createVertex(BusinessService businessService) {
        BusinessServiceVertex businessServiceVertex = new BusinessServiceVertex(String.valueOf(businessService.getId()), businessService.getName());
        businessServiceVertex.setLabel(businessService.getName());
        businessServiceVertex.setTooltipText(String.format("BusinessService '%s'", businessService.getName()));
        businessServiceVertex.setIconKey("business-service");
        return businessServiceVertex;
    }

    public void setBusinessServiceDao(BusinessServiceDao businessServiceDao) {
        Objects.requireNonNull(businessServiceDao);
        this.businessServiceDao = transactionAwareBeanProxyFactory.createProxy(businessServiceDao);
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager =  Objects.requireNonNull(businessServiceManager);
    }

    @Override
    public void refresh() {
       load();
    }

    @Override
    public Criteria getDefaultCriteria() {
        // Only show the first application by default
        List<BusinessService> businessServices = businessServiceDao.findAll();
        if (!businessServices.isEmpty()) {
            BusinessService businessService = businessServices.iterator().next();
            return new BusinessServiceCriteria(String.valueOf(businessService.getId()), businessService.getName(), businessServiceManager);
        }
        return null;
    }

    @Override
    public void load(String filename) throws MalformedURLException, JAXBException {
      load();
    }

    @Override
    public void resetContainer() {
        super.resetContainer();
    }
}
