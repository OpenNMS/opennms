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

package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class PingSweepRequestDTOTest extends XmlTestNoCastor<PingSweepRequestDTO> {

    public PingSweepRequestDTOTest(PingSweepRequestDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] { { getPingSweepRequest(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                        + "<ping-sweep-request location=\"MINION\" packet-size=\"64\" packets-per-second=\"9.5\">\n"
                        + "<ip-range begin=\"127.0.0.1\" end=\"127.0.0.5\" retries=\"2\" timeout=\"1000\"/>\n"
                        + "</ping-sweep-request>" } });
    }

    private static Object getPingSweepRequest() throws UnknownHostException {
        PingSweepRequestDTO requestDTO = new PingSweepRequestDTO();
        List<IPRangeDTO> ipRanges = new ArrayList<>();
        IPRangeDTO range = new IPRangeDTO();
        range.setRetries(2);
        range.setTimeout(1000);
        range.setBegin(InetAddress.getByName("127.0.0.1"));
        range.setEnd(InetAddress.getByName("127.0.0.5"));
        ipRanges.add(range);
        requestDTO.setIpRanges(ipRanges);
        requestDTO.setLocation("MINION");
        requestDTO.setPacketSize(64);
        requestDTO.setPacketsPerSecond(9.5);

        return requestDTO;
    }

}
