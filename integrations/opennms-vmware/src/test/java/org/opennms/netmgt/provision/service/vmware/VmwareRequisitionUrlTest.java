/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
