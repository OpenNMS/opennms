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
                "<vmware-requisition-request import-vm-powered-on=\"true\" import-vm-powered-off=\"false\" import-vm-suspended=\"false\" import-host-powered-on=\"true\" import-host-powered-off=\"false\" import-host-standby=\"false\" import-host-unknown=\"false\" persist-ipv4=\"true\" persist-ipv6=\"true\" persist-vms=\"true\" persist-hosts=\"true\" topology-port-groups=\"false\" topology-networks=\"true\" topology-datastores=\"true\">\n" +
                "   <custom-attribute key=\"k\">v</custom-attribute>\n" +
                "</vmware-requisition-request>"
            }
        });
    }

}
