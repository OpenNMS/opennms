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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.protocols.vmware.VmwareViJavaAccess;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.VirtualMachine;

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

    @Test
    public void testNMS16320() throws Exception {
        Assert.assertNull(createRequisitionNodeInLocation(null).getLocation());
        Assert.assertEquals("Pittsboro", createRequisitionNodeInLocation("Pittsboro").getLocation());
        Assert.assertEquals("Fulda", createRequisitionNodeInLocation("Fulda").getLocation());
    }

    private RequisitionNode createRequisitionNodeInLocation(final String location) throws Exception {
        final VmwareImportRequest vmwareImportRequest = new VmwareImportRequest();
        vmwareImportRequest.setLocation(location);
        vmwareImportRequest.setHostname("1.2.3.4");
        final VmwareImporter vmwareImporter = new VmwareImporter(vmwareImportRequest);
        final VirtualMachine virtualMachine = Mockito.mock(VirtualMachine.class);
        final VmwareViJavaAccess vmwareViJavaAccess = Mockito.mock(VmwareViJavaAccess.class);
        final ManagedObjectReference managedObjectReference = Mockito.mock(ManagedObjectReference.class);
        final VirtualMachineRuntimeInfo virtualMachineRuntimeInfo = Mockito.mock(VirtualMachineRuntimeInfo.class);
        final ManagedObjectReference host = Mockito.mock(ManagedObjectReference.class);
        Mockito.when(managedObjectReference.getVal()).thenReturn("vm-1234");
        Mockito.when(virtualMachine.getMOR()).thenReturn(managedObjectReference);
        Mockito.when(virtualMachine.getDatastores()).thenReturn(new Datastore[]{});
        Mockito.when(virtualMachine.getNetworks()).thenReturn(new Network[]{});
        Mockito.when(virtualMachine.getRuntime()).thenReturn(virtualMachineRuntimeInfo);
        Mockito.when(virtualMachineRuntimeInfo.getHost()).thenReturn(host);
        Mockito.when(host.getVal()).thenReturn("host-1234");
        return vmwareImporter.createRequisitionNode(Set.of("11.12.13.14"), virtualMachine, 7, vmwareViJavaAccess);
    }
}
