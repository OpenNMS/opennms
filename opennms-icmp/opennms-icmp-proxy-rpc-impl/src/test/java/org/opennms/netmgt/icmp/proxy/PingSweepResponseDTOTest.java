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
