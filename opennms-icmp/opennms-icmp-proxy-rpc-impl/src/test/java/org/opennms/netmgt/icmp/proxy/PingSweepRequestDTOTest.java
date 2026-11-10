/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
