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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.web.rest.support.ApiVersion;
import org.opennms.web.rest.support.ResourceLocation;

public class BusinessServiceDTOJaxbTest extends XmlTestNoCastor<BusinessServiceDTO> {

    public BusinessServiceDTOJaxbTest(BusinessServiceDTO sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        BusinessServiceDTO bs = new BusinessServiceDTO();
        bs.setId(1L);
        bs.setName("Web Servers");
        bs.setAttribute("dc", "RDU");
        bs.setLocation(new ResourceLocation(ApiVersion.Version2, "business-services", "1"));

        IpServiceDTO ipService = new IpServiceDTO();
        ipService.setId("1");
        ipService.setLocation(new ResourceLocation(ApiVersion.Version1, "ifservices", "1"));
        bs.addIpService(ipService);

        return Arrays.asList(new Object[][]{{
            bs,
            "<business-service>" +
               "<id>1</id>" +
               "<name>Web Servers</name>" +
               "<location>/api/v2/business-services/1</location>" +
               "<attributes>" +
                 "<attribute>" +
                   "<key>dc</key>" +
                   "<value>RDU</value>" +
                 "</attribute>" +
               "</attributes>" +
               "<ip-services>" +
                   "<ip-service>" +
                      "<id>1</id>" +
                      "<location>/rest/ifservices/1</location>" +
                   "</ip-service>" +
               "</ip-services>" +
            "</business-service>",
            null
        }});
    }
}
