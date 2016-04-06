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

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import org.opennms.features.topology.api.info.EdgeInfoPanelItem;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.plugins.topo.bsm.AbstractBusinessServiceVertex;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServiceEdge;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;
import org.opennms.netmgt.bsm.service.model.Status;

import static org.opennms.features.topology.plugins.topo.bsm.info.BusinessServiceVertexStatusInfoPanelItem.createStatusLabel;

public class BusinessServiceEdgeStatusInfoPanelItem implements EdgeInfoPanelItem {

    @Override
    public Component getComponent(EdgeRef ref) {
        FormLayout formLayout = new FormLayout();
        formLayout.setMargin(false);
        formLayout.setSpacing(false);
        formLayout.addStyleName("severity");

        final BusinessServiceEdge businessServiceEdge = ((BusinessServiceEdge) ref);

        final Status outgoingStatus = businessServiceEdge.getOperationalStatus().isLessThan(Status.NORMAL)
                                      ? Status.NORMAL
                                      : businessServiceEdge.getOperationalStatus();
        final Status incomingStatus = ((AbstractBusinessServiceVertex) businessServiceEdge.getTarget().getVertex()).getOperationalStatus();

        formLayout.addComponent(createStatusLabel("Outgoing Severity", outgoingStatus));
        formLayout.addComponent(createStatusLabel("Incoming Severity", incomingStatus));

        return formLayout;
    }

    @Override
    public boolean contributesTo(EdgeRef edgeRef) {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE.equals(edgeRef.getNamespace());
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getTitle(EdgeRef edgeRef) {
        return "Map Function Status";
    }
}


