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

public class BusinessServiceRequestDTOMarshalTest extends MarshalAndUnmarshalTest<BusinessServiceRequestDTO> {

    public BusinessServiceRequestDTOMarshalTest(Class<BusinessServiceRequestDTO> type, BusinessServiceRequestDTO sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        BusinessServiceRequestDTO requestDTO = new BusinessServiceRequestDTO();
        requestDTO.setName("Web Servers");
        requestDTO.addAttribute("dc", "RDU");
        requestDTO.addAttribute("some-key", "some-value");
        requestDTO.addChildService(2L);
        requestDTO.addChildService(3L);
        requestDTO.getReductionKeys().add("myReductionKeyA");
        requestDTO.getReductionKeys().add("myReductionKeyB");
        requestDTO.addIpService(1);

        return Arrays.asList(new Object[][]{{
            BusinessServiceRequestDTO.class,
            requestDTO,
            "{" +
            "  \"name\" : \"Web Servers\"," +
            "  \"attributes\" : {" +
            "    \"dc\" : \"RDU\"," +
            "    \"some-key\" : \"some-value\"" +
            "  }," +
            "  \"childServices\" : [ 2, 3 ]," +
            "  \"ipServices\" : [ 1 ]" +
            "}",
            "<business-service>" +
               "<name>Web Servers</name>" +
               "<attributes>" +
                 "<attribute>" +
                   "<key>dc</key>" +
                   "<value>RDU</value>" +
                 "</attribute>" +
                "<attribute>" +
                  "<key>some-key</key>" +
                  "<value>some-value</value>" +
                "</attribute>" +
               "</attributes>" +
               "<ip-services>" +
                   "<ip-service>1</ip-service>" +
               "</ip-services>" +
               "<child-services>" +
                    "<child-service>2</child-service>" +
                    "<child-service>3</child-service>" +
               "</child-services>" +
            "</business-service>"
        }});
    }
}
