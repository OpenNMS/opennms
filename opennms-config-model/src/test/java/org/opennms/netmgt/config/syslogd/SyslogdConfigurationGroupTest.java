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
