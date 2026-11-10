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
package org.opennms.netmgt.syslogd.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.StringUtils;

public class SyslogMessageLogDTOTest extends XmlTestNoCastor<SyslogMessageLogDTO> {

    public SyslogMessageLogDTOTest(SyslogMessageLogDTO sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException, UnknownHostException {
        SyslogMessageLogDTO messageLog = new SyslogMessageLogDTO();
        messageLog.setLocation("loc");
        messageLog.setSystemId("99");
        messageLog.setSourceAddress(InetAddress.getByName("127.0.0.1"));
        messageLog.setSourcePort(1514);

        SyslogMessageDTO message = new SyslogMessageDTO();
        message.setTimestamp(new Date(0));
        message.setBytes(ByteBuffer.wrap(new byte[] {0, 1, 2, 3}));
        messageLog.getMessages().add(message);

        return Arrays.asList(new Object[][] { { messageLog,
                "<syslog-message-log source-address=\"127.0.0.1\" source-port=\"1514\" system-id=\"99\" location=\"loc\">\n" + 
                "   <messages timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">AAECAw==</messages>\n" + 
                "</syslog-message-log>",
                null }
        });
    }
}
