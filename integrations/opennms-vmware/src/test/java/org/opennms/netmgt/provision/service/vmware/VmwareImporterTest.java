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
