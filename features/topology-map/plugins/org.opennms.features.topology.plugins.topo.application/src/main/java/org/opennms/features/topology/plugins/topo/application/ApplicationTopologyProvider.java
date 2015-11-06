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

package org.opennms.features.topology.plugins.topo.application;

import java.net.MalformedURLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE = "application";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationTopologyProvider.class);

    private ApplicationDao applicationDao;

    public ApplicationTopologyProvider(ApplicationDao applicationDao) {
        super(new ApplicationVertexProvider(TOPOLOGY_NAMESPACE), new SimpleEdgeProvider(TOPOLOGY_NAMESPACE));
        this.applicationDao = applicationDao;
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), TOPOLOGY_NAMESPACE);
    }

    @Override
    public void save() {
       // we do not support save at the moment
    }
    
    private void load() {
        resetContainer();
        List<OnmsApplication> applications = applicationDao.findAll();
        if (!applications.isEmpty()) {
            // TODO MVR consider all applications, not just the first one
            OnmsApplication application = applications.get(0);
//            for (OnmsApplication application : applications) {
                ApplicationVertex applicationVertex = new ApplicationVertex(String.valueOf(application.getId()));
                applicationVertex.setLabel(application.getName());
                applicationVertex.setTooltipText(String.format("Application '%s'", application.getName()));
                applicationVertex.setIconKey("application");
                addVertices(applicationVertex);

                for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                    final ApplicationVertex serviceVertex = new ApplicationVertex(String.valueOf(eachMonitoredService.getId()));
                    serviceVertex.setIpAddress(eachMonitoredService.getIpAddress().toString());
                    serviceVertex.setLabel(eachMonitoredService.getServiceName());
                    serviceVertex.setTooltipText(String.format("Service '%s', IP: %s", eachMonitoredService.getServiceName(), eachMonitoredService.getIpAddress().toString()));
                    serviceVertex.setNodeID(eachMonitoredService.getNodeId());
                    serviceVertex.setServiceType(eachMonitoredService.getServiceType());
                    applicationVertex.addChildren(serviceVertex);
                    addVertices(serviceVertex);

                    // connect with application
                    String id = String.format("connection:%s:%s", applicationVertex.getId(), serviceVertex.getId());
                    Edge edge = new AbstractEdge(getEdgeNamespace(), id, applicationVertex, serviceVertex);
                    edge.setTooltipText("LINK");
                    addEdges(edge);
                }
//            }
        }
    }

    @Override
    public void refresh() {
       load();
    }

    @Override
    public Criteria getDefaultCriteria() {
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
