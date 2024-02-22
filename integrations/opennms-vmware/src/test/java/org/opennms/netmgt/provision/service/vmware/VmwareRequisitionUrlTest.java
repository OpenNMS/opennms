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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;
import org.opennms.netmgt.provision.service.requisition.RequisitionUrlConnection;

@RunWith(Parameterized.class)
public class VmwareRequisitionUrlTest {
    private final String vmwareUrl;
    private final String requisitionUrl;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private final SecureCredentialsVault secureCredentialsVault;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
             {
               "vmware://vcenter.mydomain.org?importHostPoweredOff=true&username=username&password=password",
               "requisition://vmware?host=vcenter.mydomain.org&importHostPoweredOff=true&username=username&password=password"
             },
             {
               "vmware://172.16.123.100/vCenterImport?key=shouldImport;value=1&username=username&password=password",
               "requisition://vmware/vCenterImport?host=172.16.123.100&key=shouldImport;value=1&username=username&password=password"
             },
             {
               "vmware://172.16.123.100/vCenterImport?_shouldImport=1&username=username&password=password",
               "requisition://vmware/vCenterImport?host=172.16.123.100&_shouldImport=1&username=username&password=password"
             },
             {
               "vmware://172.16.123.100/vCenterImport?_shouldImport=1;username=opennms;password=secret",
               "requisition://vmware/vCenterImport?host=172.16.123.100&_shouldImport=1&username=opennms&password=secret"
             },
             {
               "vmware://[2001:db8:0:8d3:0:8a2e:70:7344]?virtualMachineServices=VM-SERVICE1,VM-SERVICE2&username=username&password=password",
               "requisition://vmware?host=[2001:db8:0:8d3:0:8a2e:70:7344]&virtualMachineServices=VM-SERVICE1,VM-SERVICE2&username=username&password=password"
             },
             {
                "vmware://vcenter.mydomain.org?importHostPoweredOff=true&timeout=3050&cimTimeout=3100&username=username&password=password",
                "requisition://vmware?host=vcenter.mydomain.org&importHostPoweredOff=true&timeout=3050&cimTimeout=3100&username=username&password=password"
             }
       });
    }

    @BeforeClass
    public static void setUpClass() {
        GenericURLFactory.initialize();
    }

    public VmwareRequisitionUrlTest(String vmwareUrl, String requisitionUrl) throws IOException {
        tempFolder.create();

        this.vmwareUrl = vmwareUrl;
        this.requisitionUrl = requisitionUrl;

        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
    }

    @Test
    public void compareGeneratedRequests() throws MalformedURLException, RemoteException {
        final VmwareConfigDao vmwareConfigDao = mock(VmwareConfigDao.class);
        when(vmwareConfigDao.getServerMap()).thenReturn(new HashMap<String, VmwareServer>());

        VmwareRequisitionUrlConnection.setSecureCredentialsVault(secureCredentialsVault);
        VmwareRequisitionUrlConnection.setVmwareConfigDao(vmwareConfigDao);
        VmwareRequisitionUrlConnection vmwareUrlConnection = new VmwareRequisitionUrlConnection(new URL(vmwareUrl));
        VmwareImportRequest vmwareUrlImportRequest = vmwareUrlConnection.getImportRequest();

        Map<String, String> requisitionUrlParms = RequisitionUrlConnection.getParameters(new URL(requisitionUrl));
        VmwareImportRequest requisitionUrlImportRequest = new VmwareImportRequest(requisitionUrlParms);

        assertEquals(vmwareUrlImportRequest, requisitionUrlImportRequest);
    }
}
