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
package org.opennms.netmgt.poller.client.rpc;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.poller.PollStatus;

public class PollerResponseDTOTest extends XmlTestNoCastor<PollerResponseDTO> {

    public PollerResponseDTOTest(PollerResponseDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
            // NOTE: In order for the latency metrics to be persisted properly
            // it is important the the properties are unmarshaled in the same order
            // as they appear in the source PollStatus object
            {
                getPollerResponse(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-response>" +
                "   <poll-status code=\"1\" name=\"Up\" time=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" +
                "      <properties>\n" +
                "         <property key=\"ping1\">55</property>\n" +
                "         <property key=\"ping2\">61</property>\n" +
                "         <property key=\"ping3\">67</property>\n" +
                "         <property key=\"median\">98</property>\n" +
                "      </properties>\n" +
                "   </poll-status>\n" +
                "</poller-response>"
            },
            {
                getPollerResponseNoProperties(),
                "<?xml version=\"1.0\"?>\n" +
                "<poller-response>" +
                "   <poll-status reason=\"don't ask me\" code=\"0\" name=\"Unknown\" time=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" +
                "     <properties/>\n" +
                "   </poll-status>\n" +
                "</poller-response>"
            }
        });
    }

    public static PollerResponseDTO getPollerResponseNoProperties() throws UnknownHostException {
        PollerResponseDTO dto = new PollerResponseDTO();
        PollStatus pollStatus = PollStatus.unknown("don't ask me");
        pollStatus.setTimestamp(new Date(0));
        dto.setPollStatus(pollStatus);
        return dto;
    }

    public static PollerResponseDTO getPollerResponse() throws UnknownHostException {
        PollerResponseDTO dto = new PollerResponseDTO();
        PollStatus pollStatus = PollStatus.available();
        pollStatus.setTimestamp(new Date(0));
        pollStatus.setProperty("ping1", BigDecimal.valueOf(55));
        pollStatus.setProperty("ping2", BigDecimal.valueOf(61));
        pollStatus.setProperty("ping3", BigDecimal.valueOf(67));
        pollStatus.setProperty("median", BigDecimal.valueOf(98));
        dto.setPollStatus(pollStatus);
        return dto;
    }
}
