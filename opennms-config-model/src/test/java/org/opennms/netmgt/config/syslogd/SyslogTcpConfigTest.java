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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SyslogTcpConfigTest {

    @Test
    public void tcpIsDisabledWithoutATcpElement() {
        // An install that has never been touched must keep its UDP-only behaviour.
        final Configuration config = new Configuration();
        config.setSyslogPort(514);

        assertNull(config.getTcpConfig());
    }

    @Test
    public void tcpIsEnabledOnceTheElementCarriesAPort() {
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(601);

        final Configuration config = new Configuration();
        config.setSyslogPort(514);
        config.setTcpConfig(tcpConfig);

        assertTrue(config.getTcpConfig().isEnabled());
        assertEquals(Integer.valueOf(601), config.getTcpConfig().getPort());
    }

    @Test
    public void aZeroPortLeavesTcpOffSoTheMinionCfgCanAlwaysCarryTheKey() {
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(0);

        assertFalse(tcpConfig.isEnabled());
    }

    @Test
    public void unsetListenAddressLeavesTheListenerToBindEverything() {
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(601);

        assertNull(tcpConfig.getListenAddress());
    }

    @Test
    public void defaultsAreAppliedForUnsetTcpAttributes() {
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(601);

        assertEquals(SyslogTcpFraming.AUTO, tcpConfig.resolveFraming());
        assertEquals(SyslogTcpConfig.DEFAULT_MAX_MESSAGE_SIZE, tcpConfig.getMaxMessageSize());
        assertEquals(SyslogTcpConfig.DEFAULT_MAX_CONNECTIONS, tcpConfig.getMaxConnections());
        assertEquals(SyslogTcpConfig.DEFAULT_IDLE_TIMEOUT_SECONDS, tcpConfig.getIdleTimeoutSeconds());
    }

    @Test
    public void tlsIsOffWithoutATlsElement() {
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(601);

        assertNull(tcpConfig.getTls());
        assertFalse(tcpConfig.isTlsEnabled());
    }

    @Test
    public void aTlsElementThatIsNotEnabledStillLeavesTlsOff() {
        // So that switching TLS off does not mean deleting the certificate paths.
        final SyslogTcpTlsConfig tls = new SyslogTcpTlsConfig();
        tls.setCertFilePath("/etc/syslog.crt");

        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setPort(601);
        tcpConfig.setTls(tls);

        assertFalse(tcpConfig.isTlsEnabled());
        assertEquals(SyslogTcpClientAuth.NONE, tls.resolveClientAuth());
    }

    @Test
    public void framingAcceptsTheHyphenatedAndUnderscoredSpellings() {
        assertEquals(SyslogTcpFraming.OCTET_COUNTING, SyslogTcpFraming.fromConfigValue("octet-counting"));
        assertEquals(SyslogTcpFraming.OCTET_COUNTING, SyslogTcpFraming.fromConfigValue("OCTET_COUNTING"));
        assertEquals(SyslogTcpFraming.NON_TRANSPARENT, SyslogTcpFraming.fromConfigValue("non-transparent"));
        assertEquals(SyslogTcpFraming.NON_TRANSPARENT, SyslogTcpFraming.fromConfigValue(" Non-Transparent "));
        assertEquals(SyslogTcpFraming.AUTO, SyslogTcpFraming.fromConfigValue(null));
        assertEquals(SyslogTcpFraming.AUTO, SyslogTcpFraming.fromConfigValue(""));
    }

    @Test
    public void clientAuthAcceptsTheDocumentedSpellings() {
        assertEquals(SyslogTcpClientAuth.NONE, SyslogTcpClientAuth.fromConfigValue(null));
        assertEquals(SyslogTcpClientAuth.OPTIONAL, SyslogTcpClientAuth.fromConfigValue("optional"));
        assertEquals(SyslogTcpClientAuth.REQUIRE, SyslogTcpClientAuth.fromConfigValue("REQUIRE"));
    }

    @Test
    public void badValuesAreRejectedAtWiringTimeRatherThanOnTheFirstMessage() {
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        final SyslogTcpTlsConfig tls = new SyslogTcpTlsConfig();

        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setFraming("octet counting"));
        assertThrows(IllegalArgumentException.class, () -> tls.setClientAuth("mutual"));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setPort(70000));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setMaxMessageSize(0));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setMaxConnections(0));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setIdleTimeoutSeconds(-1));
    }

    @Test
    public void blankPathsAreNormalizedToNull() {
        final SyslogTcpTlsConfig tls = new SyslogTcpTlsConfig();
        tls.setCertFilePath("   ");
        tls.setPrivateKeyFilePath("  /etc/key.pem  ");

        assertNull(tls.getCertFilePath());
        assertEquals("/etc/key.pem", tls.getPrivateKeyFilePath());
    }
}
