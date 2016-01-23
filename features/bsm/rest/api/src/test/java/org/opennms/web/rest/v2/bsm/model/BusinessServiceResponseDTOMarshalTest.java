/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.web.rest.api.ApiVersion;
import org.opennms.web.rest.api.ResourceLocation;

public class BusinessServiceResponseDTOMarshalTest extends MarshalAndUnmarshalTest<BusinessServiceResponseDTO> {

    public BusinessServiceResponseDTOMarshalTest(Class<BusinessServiceResponseDTO> type, BusinessServiceResponseDTO sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        BusinessServiceResponseDTO bs = new BusinessServiceResponseDTO();
        bs.setId(1L);
        bs.setName("Web Servers");
        bs.addAttribute("dc", "RDU");
        bs.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "1"));
        bs.setOperationalStatus(Status.CRITICAL);

        // TODO MVR fix me
//        bs.addReductionKey("myReductionKeyA");
//        bs.addReductionKey("myReductionKeyB");
//
//        bs.addChildService(2L);
//        bs.addChildService(3L);
//        bs.addParentService(11L);
//        bs.addParentService(12L);
//
//        IpServiceEdgeResponseDTO ipService = new IpServiceEdgeResponseDTO();
//        ipService.setId(1);
//        ipService.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "ip-services", "1"));
//        ipService.setOperationalStatus(Status.WARNING);
//        bs.addIpService(ipService);

        return Arrays.asList(new Object[][]{{
            BusinessServiceResponseDTO.class,
            bs,
            "{\n" +
            "  \"location\" : \"/api/v2/business-services/1\",\n" +
            "  \"name\" : \"Web Servers\",\n" +
            "  \"id\" : 1,\n" +
            "  \"attributes\" : {\n" +
            "    \"dc\" : \"RDU\"\n" +
            "  },\n" +
            "  \"ipServices\" : [ {\n" +
            "    \"location\" : \"/api/v2/business-services/ip-services/1\",\n" +
            "    \"id\" : 1,\n" +
            "    \"serviceName\" : null,\n" +
            "    \"nodeLabel\" : null,\n" +
            "    \"ipAddress\" : null,\n" +
            "    \"operationalStatus\" : \"WARNING\"\n" +
            "  } ],\n" +
            "  \"childServices\" : [ 2, 3 ],\n" +
            "  \"parentServices\" : [ 11, 12 ],\n" +
            "  \"operationalStatus\" : CRITICAL\n" +
            "}",
            "<business-service>" +
               "<id>1</id>" +
               "<name>Web Servers</name>" +
               "<attributes>" +
                 "<attribute>" +
                   "<key>dc</key>" +
                   "<value>RDU</value>" +
                 "</attribute>" +
               "</attributes>" +
               "<ip-services>" +
                   "<ip-service>" +
                      "<id>1</id>" +
                      "<operational-status>WARNING</operational-status>" +
                      "<location>/api/v2/business-services/ip-services/1</location>" +
                   "</ip-service>" +
               "</ip-services>" +
               "<child-services>" +
                    "<child-service>2</child-service>" +
                    "<child-service>3</child-service>" +
               "</child-services>" +
               "<parent-services>" +
                    "<parent-service>11</parent-service>" +
                    "<parent-service>12</parent-service>" +
               "</parent-services>" +
               "<operational-status>CRITICAL</operational-status>" +
               "<location>/api/v2/business-services/1</location>" +
            "</business-service>"
        }});
    }
}
