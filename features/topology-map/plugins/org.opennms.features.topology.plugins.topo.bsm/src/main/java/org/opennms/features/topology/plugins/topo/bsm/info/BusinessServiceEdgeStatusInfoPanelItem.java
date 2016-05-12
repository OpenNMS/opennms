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

package org.opennms.features.topology.plugins.topo.bsm.info;

import static org.opennms.features.topology.plugins.topo.bsm.info.BusinessServiceVertexStatusInfoPanelItem.createStatusLabel;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.EdgeInfoPanelItem;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceEdge;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesStatusProvider;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;
import org.opennms.features.topology.plugins.topo.bsm.simulate.SimulationAwareStateMachineFactory;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;

public class BusinessServiceEdgeStatusInfoPanelItem extends EdgeInfoPanelItem {

    private BusinessServiceManager businessServiceManager;

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;

    public BusinessServiceEdgeStatusInfoPanelItem(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.transactionAwareBeanProxyFactory = transactionAwareBeanProxyFactory;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(businessServiceManager);
    }

    @Override
    protected Component getComponent(EdgeRef ref, GraphContainer container) {
        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(false);
        formLayout.setSpacing(false);
        formLayout.addStyleName("severity");

        final BusinessServiceEdge businessServiceEdge = ((BusinessServiceEdge) ref);

        final BusinessServiceStateMachine stateMachine = SimulationAwareStateMachineFactory.createStateMachine(businessServiceManager, container.getCriteria());
        final Status outgoingStatus = BusinessServicesStatusProvider.getStatus(stateMachine, businessServiceEdge);
        final Status incomingStatus = BusinessServicesStatusProvider.getStatus(stateMachine, ((AbstractBusinessServiceVertex) businessServiceEdge.getTarget().getVertex()));

        formLayout.addComponent(createStatusLabel("Outgoing Severity", outgoingStatus));
        formLayout.addComponent(createStatusLabel("Incoming Severity", incomingStatus));

        return formLayout;
    }

    @Override
    protected boolean contributesTo(EdgeRef edgeRef, GraphContainer container) {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE.equals(edgeRef.getNamespace());
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected String getTitle(EdgeRef edgeRef) {
        return "Map Function Status";
    }
}


