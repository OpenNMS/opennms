/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.dns.client.rpc;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DnsLookupResponseDTOTest extends XmlTestNoCastor<DnsLookupResponseDTO> {

    public DnsLookupResponseDTOTest(DnsLookupResponseDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
            {   getDnsLookupResponse(),
                "<?xml version=\"1.0\"?>\n" +
                "<dns-lookup-response host-response=\"127.0.0.1\"/>"
            }
        });
    }

    private static Object getDnsLookupResponse() {
        DnsLookupResponseDTO dto = new DnsLookupResponseDTO();
        dto.setHostResponse("127.0.0.1");
        return dto;
    }

}
