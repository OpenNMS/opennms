/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.syslogd;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.xml.JaxbUtils;

public class SyslogdConfigurationTest extends XmlTestNoCastor<SyslogdConfiguration> {

    public SyslogdConfigurationTest(SyslogdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/syslog.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<syslogd-configuration>\n" + 
                        "    <configuration\n" + 
                        "            syslog-port=\"10514\"\n" + 
                        "            new-suspect-on-message=\"false\"\n" + 
                        "            parser=\"org.opennms.netmgt.syslogd.CustomSyslogParser\"\n" + 
                        "            forwarding-regexp=\"^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)\"\n" + 
                        "            matching-group-host=\"6\"\n" + 
                        "            matching-group-message=\"8\"\n" + 
                        "            discard-uei=\"DISCARD-MATCHING-MESSAGES\"\n" + 
                        "            />\n" +
                        "    <ueiList/>" +
                        "    <hideMessage/>" +
                        "    <import-file>syslog/ApacheHTTPD.syslog.xml</import-file>" +
                        "</syslogd-configuration>"
            }
        });
    }

    private static SyslogdConfiguration getConfig() {
        SyslogdConfiguration daemonConfig = new SyslogdConfiguration();

        Configuration config = new Configuration();
        config.setSyslogPort(10514);
        config.setNewSuspectOnMessage(false);
        config.setParser("org.opennms.netmgt.syslogd.CustomSyslogParser");
        config.setForwardingRegexp("^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)");
        config.setMatchingGroupHost(6);
        config.setMatchingGroupMessage(8);
        config.setDiscardUei("DISCARD-MATCHING-MESSAGES");
        daemonConfig.setConfiguration(config);

        daemonConfig.addImportFile("syslog/ApacheHTTPD.syslog.xml");
        return daemonConfig;
    }

    @Test
    public void testOutOfOrderUeiMatch() {
        final String xml = "<ueiMatch>\n" + 
                "    <process-match expression=\"^HAL_ASE\\\\DbServer\" />\n" + 
                "    <match type=\"regex\" expression=\"^((.+?) (.*))\\r?\\n?$\"/>\n" + 
                "    <severity>Critical</severity>\n" + 
                "    <uei>mottmac.com/syslog/HAL_ASE/critical</uei>\n" + 
                "</ueiMatch>";

        final UeiMatch match = JaxbUtils.unmarshal(UeiMatch.class, xml);
        assertEquals("^HAL_ASE\\\\DbServer", match.getProcessMatch().get().getExpression());
        assertEquals("Critical", match.getSeverities().get(0));
    }
}
