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

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class VmwareImportRequestTest extends XmlTestNoCastor<VmwareImportRequest> {

    public VmwareImportRequestTest(VmwareImportRequest sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        VmwareImportRequest request = new VmwareImportRequest();
        request.getCustomAttributes().add(new VmwareImportRequestAttribute("k", "v"));

        // This causes problems due to the date formatting
        //Requisition existingRequisition = new Requisition();
        //existingRequisition.setDate(new Date(0));
        //request.setExistingRequisition(existingRequisition);

        return Arrays.asList(new Object[][] {
            {
                request,
                "<vmware-requisition-request import-vm-powered-on=\"true\" import-vm-powered-off=\"false\" import-vm-suspended=\"false\" import-host-powered-on=\"true\" import-host-powered-off=\"false\" import-host-standby=\"false\" import-host-unknown=\"false\" persist-ipv4=\"true\" persist-ipv6=\"true\" persist-vms=\"true\" persist-hosts=\"true\" topology-port-groups=\"false\" topology-networks=\"true\" topology-datastores=\"true\" timeout=\"3000\" cim-timeout=\"3000\">\n" +
                "   <custom-attribute key=\"k\">v</custom-attribute>\n" +
                "</vmware-requisition-request>"
            }
        });
    }

}
