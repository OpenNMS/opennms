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

package org.opennms.netmgt.provision.service.dns;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DnsRequisitionRequestTest extends XmlTestNoCastor<DnsRequisitionRequest> {

    public DnsRequisitionRequestTest(DnsRequisitionRequest sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        DnsRequisitionRequest request = new DnsRequisitionRequest();
        request.setHost("my-dns-server");
        request.setPort(5353);
        request.setZone("some-zone");
        request.setFallback(false);
        request.setForeignSource("f*s*");
        request.setForeignIdHashSource(ForeignIdHashSource.IP_ADDRESS);

        return Arrays.asList(new Object[][] {
            {
                request,
                "<dns-requisition-request host=\"my-dns-server\" port=\"5353\" zone=\"some-zone\" foreign-source=\"f*s*\" fallback=\"false\" foreign-id-hash-source=\"IP_ADDRESS\"/>"
            }
        });
    }

}