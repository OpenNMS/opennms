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
package org.opennms.netmgt.wsman.eventlog.rpc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EventLogRequestDTOTest extends XmlTestNoCastor<EventLogRequestDTO> {

    public EventLogRequestDTOTest(EventLogRequestDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        final EventLogRequestDTO dto = new EventLogRequestDTO();
        dto.setLocation("MINION");
        dto.setRetries(2);
        dto.setEndpointAttributes(Map.of("url", "http://10.0.0.5:5985/wsman"));
        final EventLogQueryDTO query = new EventLogQueryDTO("System");
        query.setAfterRecordNumber(42L);
        query.setMaxRecords(100);
        query.setEventTypes(List.of(1, 2));
        dto.addQuery(query);
        return Arrays.asList(new Object[][] {
            { dto,
              "<?xml version=\"1.0\"?>\n"
              + "<wsman-eventlog-request location=\"MINION\" resource-uri=\"" + EventLogRequestDTO.DEFAULT_RESOURCE_URI + "\" retries=\"2\">\n"
              + "  <endpoint-attribute key=\"url\">http://10.0.0.5:5985/wsman</endpoint-attribute>\n"
              + "  <query logfile=\"System\" after-record-number=\"42\" max-records=\"100\">\n"
              + "    <event-type>1</event-type>\n"
              + "    <event-type>2</event-type>\n"
              + "  </query>\n"
              + "</wsman-eventlog-request>" }
        });
    }
}
