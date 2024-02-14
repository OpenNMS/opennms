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

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.provision.service.requisition.RequisitionUrlConnection;

@RunWith(Parameterized.class)
public class VmwareRequisitionUrlTest {
    private final String vmwareUrl;
    private final String requisitionUrl;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
             {
               "vmware://vcenter.mydomain.org?importHostPoweredOff=true",
               "requisition://vmware?host=vcenter.mydomain.org&importHostPoweredOff=true"
             },
             {
               "vmware://172.16.123.100/vCenterImport?key=shouldImport;value=1",
               "requisition://vmware/vCenterImport?host=172.16.123.100&key=shouldImport;value=1"
             },
             {
               "vmware://172.16.123.100/vCenterImport?_shouldImport=1",
               "requisition://vmware/vCenterImport?host=172.16.123.100&_shouldImport=1"
             },
             {
               "vmware://172.16.123.100/vCenterImport?_shouldImport=1;username=opennms;password=secret",
               "requisition://vmware/vCenterImport?host=172.16.123.100&_shouldImport=1&username=opennms&password=secret"
             },
             {
               "vmware://[2001:db8:0:8d3:0:8a2e:70:7344]?virtualMachineServices=VM-SERVICE1,VM-SERVICE2",
               "requisition://vmware?host=[2001:db8:0:8d3:0:8a2e:70:7344]&virtualMachineServices=VM-SERVICE1,VM-SERVICE2"
             },
             {
                "vmware://vcenter.mydomain.org?importHostPoweredOff=true&timeout=3050&cimTimeout=3100",
                "requisition://vmware?host=vcenter.mydomain.org&importHostPoweredOff=true&timeout=3050&cimTimeout=3100"
             }
       });
    }

    @BeforeClass
    public static void setUpClass() {
        GenericURLFactory.initialize();
    }

    public VmwareRequisitionUrlTest(String vmwareUrl, String requisitionUrl) {
        this.vmwareUrl = vmwareUrl;
        this.requisitionUrl = requisitionUrl;
    }

    @Test
    public void compareGeneratedRequests() throws MalformedURLException, RemoteException {
        VmwareRequisitionUrlConnection vmwareUrlConnection = new VmwareRequisitionUrlConnection(new URL(vmwareUrl));
        VmwareImportRequest vmwareUrlImportRequest = vmwareUrlConnection.getImportRequest();

        Map<String, String> requisitionUrlParms = RequisitionUrlConnection.getParameters(new URL(requisitionUrl));
        VmwareImportRequest requisitionUrlImportRequest = new VmwareImportRequest(requisitionUrlParms);

        assertEquals(vmwareUrlImportRequest, requisitionUrlImportRequest);
    }
}
