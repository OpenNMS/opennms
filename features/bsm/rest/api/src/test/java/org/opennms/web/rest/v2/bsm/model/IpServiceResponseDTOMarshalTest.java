/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2.bsm.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;

public class IpServiceResponseDTOMarshalTest extends MarshalAndUnmarshalTest<IpServiceResponseDTO> {

    public IpServiceResponseDTOMarshalTest(Class<IpServiceResponseDTO> clazz, IpServiceResponseDTO sampleObject, String sampleJson, String sampleXml) {
        super(clazz, sampleObject, sampleJson, sampleXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        IpServiceResponseDTO ipService = new IpServiceResponseDTO();
        ipService.setId(17);
        ipService.setIpAddress("1.1.1.1");
        ipService.setNodeLabel("dummy");
        ipService.setServiceName("ICMP");
        ipService.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "ip-services", "17"));

        return Arrays.asList(new Object[][]{{
                IpServiceResponseDTO.class,
                ipService,
                "{" +
                "   \"id\" : 17," +
                "   \"location\" : \"/api/v2/business-services/ip-services/17\"," +
                "   \"ip-address\" : \"1.1.1.1\"," +
                "   \"node-label\" : \"dummy\"," +
                "   \"service-name\" : \"ICMP\"" +
                "}",
                "<ip-service>\n" +
                "   <id>17</id>\n" +
                "   <service-name>ICMP</service-name>\n" +
                "   <node-label>dummy</node-label>\n" +
                "   <ip-address>1.1.1.1</ip-address>\n" +
                "   <location>/api/v2/business-services/ip-services/17</location>\n" +
                "</ip-service>"
        }});
    }
}
