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
package org.opennms.netmgt.provision.detector.client.rpc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.provision.detector.client.rpc.DetectorRequestDTO;

public class DetectorRequestDTOTest extends XmlTestNoCastor<DetectorRequestDTO> {

    public DetectorRequestDTOTest(DetectorRequestDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
            {
                getDetectorRequest(),
                "<?xml version=\"1.0\"?>\n" +
                "<detector-request location=\"MINION\" class-name=\"org.opennms.netmgt.provision.detector.icmp.IcmpDetector\" address=\"127.0.0.1\">\n" +
                  "<detector-attribute key=\"port\">8980</detector-attribute>\n" +
                  "<runtime-attribute key=\"password\">foo</runtime-attribute>\n" +
                "</detector-request>"
            }
        });
    }

    public static DetectorRequestDTO getDetectorRequest() throws UnknownHostException {
        DetectorRequestDTO dto = new DetectorRequestDTO();
        dto.setLocation("MINION");
        dto.setClassName("org.opennms.netmgt.provision.detector.icmp.IcmpDetector");
        dto.setAddress(InetAddress.getByName("127.0.0.1"));
        dto.addDetectorAttribute("port", "8980");
        dto.addRuntimeAttribute("password", "foo");
        return dto;
    }
}
