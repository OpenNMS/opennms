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
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.bsm.service.model.IpServiceDTO;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BusinessServicesTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "bsm";

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServicesTopologyProvider.class);

    private BusinessServiceManager businessServiceManager;

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
        List<BusinessServiceDTO> businessServices = businessServiceManager.findAll();
        for (BusinessServiceDTO businessService : businessServices) {
            BusinessServiceVertex businessServiceVertex = new BusinessServiceVertex(String.valueOf(businessService.getId()), businessService.getName());
            businessServiceVertex.setLabel(businessService.getName());
            businessServiceVertex.setTooltipText(String.format("BusinessService '%s'", businessService.getName()));
            businessServiceVertex.setIconKey("business-service");
            addVertices(businessServiceVertex);

            for (IpServiceDTO eachIpService : businessService.getIpServices()) {
                final BusinessServiceVertex serviceVertex = new BusinessServiceVertex(businessServiceVertex.getId() + ":" + String.valueOf(eachIpService.getId()), eachIpService.getServiceName());
                serviceVertex.setIpAddress(eachIpService.getIpAddress().toString());
                serviceVertex.setLabel(eachIpService.getServiceName());
                serviceVertex.setTooltipText(String.format("Service '%s', IP: %s", eachIpService.getServiceName(), eachIpService.getIpAddress().toString()));
//                    serviceVertex.setNodeID(eachIpService.getNodeId());
//                    serviceVertex.setServiceType(eachIpService.getServiceType());
                businessServiceVertex.addChildren(serviceVertex);
                addVertices(serviceVertex);

                // connect with businessService
                String id = String.format("connection:%s:%s", businessServiceVertex.getId(), serviceVertex.getId());
                Edge edge = new AbstractEdge(getEdgeNamespace(), id, businessServiceVertex, serviceVertex);
                edge.setTooltipText("LINK");
                addEdges(edge);
            }
        }
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        Objects.requireNonNull(businessServiceManager);
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }

    @Override
    public void refresh() {
       load();
    }

    @Override
    public Criteria getDefaultCriteria() {
        // Only show the first application by default
        List<BusinessServiceDTO> businessServices = businessServiceManager.findAll();
        if (!businessServices.isEmpty()) {
            return new BusinessServiceCriteria(String.valueOf(businessServices.get(0).getId()));
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
