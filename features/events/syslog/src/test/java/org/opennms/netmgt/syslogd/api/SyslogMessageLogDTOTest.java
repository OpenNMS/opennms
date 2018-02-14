/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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
