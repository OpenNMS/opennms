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

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EventLogResponseDTOTest extends XmlTestNoCastor<EventLogResponseDTO> {

    public EventLogResponseDTOTest(EventLogResponseDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        final EventLogResponseDTO dto = new EventLogResponseDTO();
        final EventLogBatchDTO batch = new EventLogBatchDTO("System");
        batch.setTruncated(true);
        final EventLogRecordDTO record = new EventLogRecordDTO();
        record.setLogfile("System");
        record.setRecordNumber(7);
        record.setEventCode(6008);
        record.setEventType(1);
        record.setSourceName("EventLog");
        record.setTimeGenerated("20260918120000.000000+000");
        record.setComputerName("WIN-12");
        record.setMessage("The previous system shutdown was unexpected.");
        record.setInsertionStrings(List.of("12:00:00", "9/18/2026"));
        batch.getRecords().add(record);
        dto.addBatch(batch);
        return Arrays.asList(new Object[][] {
            { dto,
              "<?xml version=\"1.0\"?>\n"
              + "<wsman-eventlog-response>\n"
              + "  <batch logfile=\"System\" truncated=\"true\">\n"
              + "    <record logfile=\"System\" record-number=\"7\" event-code=\"6008\" event-type=\"1\" source-name=\"EventLog\" time-generated=\"20260918120000.000000+000\" computer-name=\"WIN-12\">\n"
              + "      <message>The previous system shutdown was unexpected.</message>\n"
              + "      <insertion-string>12:00:00</insertion-string>\n"
              + "      <insertion-string>9/18/2026</insertion-string>\n"
              + "    </record>\n"
              + "  </batch>\n"
              + "</wsman-eventlog-response>" }
        });
    }
}
