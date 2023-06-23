/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.vmware;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

public class VmwareImporterTest {
    @Test
    public void testNMS14450() {
        final VmwareImporter vmwareImporter = new VmwareImporter(new VmwareImportRequest());
        final RequisitionNode requisitionNode = new RequisitionNode();
        final Set<RequisitionInterface> requisitionInterfaces = new HashSet<>();

        final RequisitionInterface requisitionInterface1 = new RequisitionInterface();
        requisitionInterface1.setIpAddr("192.168.42.1");
        requisitionInterfaces.add(requisitionInterface1);

        final RequisitionInterface requisitionInterface2 = new RequisitionInterface();
        requisitionInterface1.setIpAddr(null);
        requisitionInterfaces.add(requisitionInterface2);

        requisitionNode.setInterfaces(requisitionInterfaces);
        vmwareImporter.getRequisitionInterface(requisitionNode, null);
    }
}
