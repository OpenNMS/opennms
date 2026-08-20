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
                        "            >\n" +
                        "        <tcp port=\"10601\"\n" +
                        "             listen-address=\"127.0.0.1\"\n" +
                        "             framing=\"octet-counting\"\n" +
                        "             max-message-size=\"32768\"\n" +
                        "             max-connections=\"64\"\n" +
                        "             idle-timeout=\"300\">\n" +
                        "            <tls enabled=\"true\"\n" +
                        "                 cert-filepath=\"/opt/opennms/etc/syslog-tls.crt\"\n" +
                        "                 private-key-filepath=\"/opt/opennms/etc/syslog-tls.key\"\n" +
                        "                 trust-cert-filepath=\"/opt/opennms/etc/syslog-tls-ca.crt\"\n" +
                        "                 client-auth=\"require\"/>\n" +
                        "        </tcp>\n" +
                        "    </configuration>\n" +
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

        SyslogTcpTlsConfig tls = new SyslogTcpTlsConfig();
        tls.setEnabled(true);
        tls.setCertFilePath("/opt/opennms/etc/syslog-tls.crt");
        tls.setPrivateKeyFilePath("/opt/opennms/etc/syslog-tls.key");
        tls.setTrustCertFilePath("/opt/opennms/etc/syslog-tls-ca.crt");
        tls.setClientAuth("require");

        SyslogTcpConfig tcp = new SyslogTcpConfig();
        tcp.setPort(10601);
        tcp.setListenAddress("127.0.0.1");
        tcp.setFraming("octet-counting");
        tcp.setMaxMessageSize(32768);
        tcp.setMaxConnections(64);
        tcp.setIdleTimeoutSeconds(300);
        tcp.setTls(tls);
        config.setTcpConfig(tcp);
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
