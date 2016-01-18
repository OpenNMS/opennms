/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v2.bsm.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;

public class IpServiceResponseDTOMarshalTest extends MarshalAndUnmarshalTest<IpServiceResponseDTO> {

    public IpServiceResponseDTOMarshalTest(Class<IpServiceResponseDTO> clazz, IpServiceResponseDTO sampleObject, String sampleJson, String sampleXml) {
        super(clazz, sampleObject, sampleJson, sampleXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        IpServiceResponseDTO ipService = new IpServiceResponseDTO();
        ipService.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "ip-services", "1"));
        ipService.setId(1);
        ipService.setIpAddress("1.1.1.1");
        ipService.setNodeLabel("dummy");
        ipService.setOperationalStatus(OnmsSeverity.WARNING);
        ipService.setServiceName("ICMP");

        return Arrays.asList(new Object[][]{{
                IpServiceResponseDTO.class,
                ipService,
                "{" +
                "  \"location\" : \"/api/v2/business-services/ip-services/1\"," +
                "  \"id\" : 1," +
                "  \"ipAddress\" : \"1.1.1.1\"," +
                "  \"nodeLabel\" : \"dummy\"," +
                "  \"operationalStatus\" : \"WARNING\"," +
                "  \"serviceName\" : \"ICMP\"" +
                "}",
                "<ip-service>" +
                        "<id>1</id>" +
                        "<service-name>ICMP</service-name>" +
                        "<node-label>dummy</node-label>" +
                        "<ip-address>1.1.1.1</ip-address>" +
                        "<operational-status>WARNING</operational-status>" +
                        "<location>/api/v2/business-services/ip-services/1</location>" +
                "</ip-service>"
        }});
    }
}
