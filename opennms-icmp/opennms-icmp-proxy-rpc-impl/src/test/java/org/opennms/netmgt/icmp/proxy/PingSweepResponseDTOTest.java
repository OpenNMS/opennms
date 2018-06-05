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

public class PingSweepResponseDTOTest extends XmlTestNoCastor<PingSweepResponseDTO> {

    public PingSweepResponseDTOTest(PingSweepResponseDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] { { getPingSweepResponse(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<ping-sweep-response>\n"
                        + "<pinger-result>\n" + "<address>127.0.0.1</address>\n" + "<rtt>0.243</rtt>\n"
                        + "</pinger-result>\n" + "</ping-sweep-response>\n" } });
    }

    private static Object getPingSweepResponse() throws UnknownHostException {
        PingSweepResponseDTO responseDTO = new PingSweepResponseDTO();
        PingSweepResultDTO resultDTO = new PingSweepResultDTO();
        resultDTO.setAddress(InetAddress.getByName("127.0.0.1"));
        resultDTO.setRtt(0.243);
        List<PingSweepResultDTO> pingResult = new ArrayList<>();
        pingResult.add(resultDTO);
        responseDTO.setPingSweepResult(pingResult);
        return responseDTO;
    }

}
