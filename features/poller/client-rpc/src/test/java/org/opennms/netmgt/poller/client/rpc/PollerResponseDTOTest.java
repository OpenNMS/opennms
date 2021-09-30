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
