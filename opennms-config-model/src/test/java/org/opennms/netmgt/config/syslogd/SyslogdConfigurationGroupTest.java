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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SyslogdConfigurationGroupTest extends XmlTestNoCastor<SyslogdConfigurationGroup> {

    public SyslogdConfigurationGroupTest(SyslogdConfigurationGroup sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/syslog.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfigGroup(),
                "<syslogd-configuration-group>\n" + 
                "    <ueiList>\n" + 
                "        <ueiMatch>\n" + 
                "            <severity>Notice</severity>\n" + 
                "            <process-match expression=\"^ifup$\" />\n" + 
                "            <match type=\"substr\" expression=\"Enabling Router Solicitations on loopback\" />\n" + 
                "            <uei>uei.opennms.org/vendor/openwrt/syslog/SystemStarting</uei>\n" + 
                "        </ueiMatch>\n" +
                "    </ueiList>\n" + 
                "    <hideMessage/>\n" +
                "</syslogd-configuration-group>"
            },
            {
                new SyslogdConfigurationGroup(),
                "<syslogd-configuration-group>\n" +
                "  <ueiList/>\n" +
                "  <hideMessage/>\n" +
                "</syslogd-configuration-group>\n"
            }
        });
    }

    private static SyslogdConfigurationGroup getConfigGroup() {
        SyslogdConfigurationGroup configGroup = new SyslogdConfigurationGroup();

        UeiMatch ueiMatch = new UeiMatch();
        ueiMatch.addSeverity("Notice");
        ueiMatch.setUei("uei.opennms.org/vendor/openwrt/syslog/SystemStarting");
        configGroup.addUeiMatch(ueiMatch);

        ProcessMatch processMatch = new ProcessMatch();
        processMatch.setExpression("^ifup$");
        ueiMatch.setProcessMatch(processMatch);

        Match match = new Match();
        match.setType("substr");
        match.setExpression("Enabling Router Solicitations on loopback");
        ueiMatch.setMatch(match);

        return configGroup;
    }
}
