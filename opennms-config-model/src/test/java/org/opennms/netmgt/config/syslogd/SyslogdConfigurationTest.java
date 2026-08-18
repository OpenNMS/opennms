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
            },
            {
                getTcpConfig(),
                "<syslogd-configuration>\n" +
                        "    <configuration\n" +
                        "            syslog-port=\"10514\"\n" +
                        "            syslog-tcp-port=\"10601\"\n" +
                        "            tcp-listen-address=\"127.0.0.1\"\n" +
                        "            tcp-framing=\"octet-counting\"\n" +
                        "            tcp-max-message-size=\"32768\"\n" +
                        "            tcp-max-connections=\"64\"\n" +
                        "            tcp-idle-timeout=\"300\"\n" +
                        "            tcp-tls-enabled=\"true\"\n" +
                        "            tcp-tls-cert-filepath=\"/opt/opennms/etc/syslog-tls.crt\"\n" +
                        "            tcp-tls-private-key-filepath=\"/opt/opennms/etc/syslog-tls.key\"\n" +
                        "            tcp-tls-trust-cert-filepath=\"/opt/opennms/etc/syslog-tls-ca.crt\"\n" +
                        "            tcp-tls-client-auth=\"require\"\n" +
                        "            />\n" +
                        "    <ueiList/>" +
                        "    <hideMessage/>" +
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

    private static SyslogdConfiguration getTcpConfig() {
        SyslogdConfiguration daemonConfig = new SyslogdConfiguration();

        Configuration config = new Configuration();
        config.setSyslogPort(10514);
        config.setSyslogTcpPort(10601);
        config.setTcpListenAddress("127.0.0.1");
        config.setTcpFraming("octet-counting");
        config.setTcpMaxMessageSize(32768);
        config.setTcpMaxConnections(64);
        config.setTcpIdleTimeout(300);
        config.setTcpTlsEnabled(true);
        config.setTcpTlsCertFilePath("/opt/opennms/etc/syslog-tls.crt");
        config.setTcpTlsPrivateKeyFilePath("/opt/opennms/etc/syslog-tls.key");
        config.setTcpTlsTrustCertFilePath("/opt/opennms/etc/syslog-tls-ca.crt");
        config.setTcpTlsClientAuth("require");
        daemonConfig.setConfiguration(config);

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
