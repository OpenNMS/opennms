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
package org.opennms.smoketest.utils;

import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;

import com.spotify.docker.client.messages.ContainerInfo;

/**
 * Helper class used to simplify creating requisitions
 * for containers.
 *
 * @author jwhite
 */
public class RequisitionBuilder {

    private final TestEnvironment minionSystem;
    
    private final Requisition requisition = new Requisition();

    public RequisitionBuilder(TestEnvironment minionSystem) {
        this.minionSystem = minionSystem;
    }

    public RequisitionBuilder withForeignSourceName(String fsName) {
        requisition.setForeignSource(fsName);
        return this;
    }

    public RequisitionBuilder withContainer(final ContainerAlias alias, final String... services) {
        // We're assuming that the Minion container is on the same
        // host as the service containers
        final ContainerInfo containerInfo = minionSystem.getContainerInfo(alias);
        final String containerIpAddr = containerInfo.networkSettings().ipAddress();

        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel(alias.toString());
        node.setForeignId(alias.toString());

        RequisitionInterface iface = new RequisitionInterface();
        iface.setSnmpPrimary(PrimaryType.PRIMARY);
        iface.setIpAddr(containerIpAddr);

        for (String svcName : services) {
            RequisitionMonitoredService svc = new RequisitionMonitoredService();
            svc.setServiceName(svcName);
            iface.putMonitoredService(svc);
        }

        node.putInterface(iface);
        requisition.putNode(node);

        return this;
    }

    public Requisition build() {
        return requisition;
    }
}
