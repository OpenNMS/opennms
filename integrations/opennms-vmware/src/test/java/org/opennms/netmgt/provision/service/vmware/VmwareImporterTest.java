/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
