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
    public void tcpIsDisabledUntilAPortIsConfigured() {
        // An install that has never been touched must keep its UDP-only behaviour.
        final Configuration config = new Configuration();
        config.setSyslogPort(514);

        assertFalse(config.getTcpConfig().isEnabled());
    }

    @Test
    public void tcpIsEnabledOnceAPortIsConfigured() {
        final Configuration config = new Configuration();
        config.setSyslogPort(514);
        config.setSyslogTcpPort(601);

        final SyslogTcpConfig tcpConfig = config.getTcpConfig();
        assertTrue(tcpConfig.isEnabled());
        assertEquals(Integer.valueOf(601), tcpConfig.getPort());
    }

    @Test
    public void tcpListenAddressFallsBackToTheUdpListenAddress() {
        final Configuration config = new Configuration();
        config.setSyslogPort(514);
        config.setSyslogTcpPort(601);
        config.setListenAddress("10.0.0.1");

        assertEquals("10.0.0.1", config.getTcpConfig().getListenAddress());
    }

    @Test
    public void explicitTcpListenAddressWinsOverTheUdpOne() {
        final Configuration config = new Configuration();
        config.setSyslogPort(514);
        config.setSyslogTcpPort(601);
        config.setListenAddress("10.0.0.1");
        config.setTcpListenAddress("10.0.0.2");

        assertEquals("10.0.0.2", config.getTcpConfig().getListenAddress());
    }

    @Test
    public void unsetListenAddressesLeaveTheListenerToBindEverything() {
        final Configuration config = new Configuration();
        config.setSyslogPort(514);
        config.setSyslogTcpPort(601);

        assertNull(config.getTcpConfig().getListenAddress());
    }

    @Test
    public void defaultsAreAppliedForUnsetTcpAttributes() {
        final Configuration config = new Configuration();
        config.setSyslogPort(514);
        config.setSyslogTcpPort(601);

        final SyslogTcpConfig tcpConfig = config.getTcpConfig();
        assertEquals(SyslogTcpFraming.AUTO, tcpConfig.resolveFraming());
        assertEquals(SyslogTcpConfig.DEFAULT_MAX_MESSAGE_SIZE, tcpConfig.getMaxMessageSize());
        assertEquals(SyslogTcpConfig.DEFAULT_MAX_CONNECTIONS, tcpConfig.getMaxConnections());
        assertEquals(SyslogTcpConfig.DEFAULT_IDLE_TIMEOUT_SECONDS, tcpConfig.getIdleTimeoutSeconds());
        assertFalse(tcpConfig.isTlsEnabled());
        assertEquals(SyslogTcpClientAuth.NONE, tcpConfig.resolveTlsClientAuth());
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

        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setFraming("octet counting"));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setTlsClientAuth("mutual"));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setPort(70000));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setMaxMessageSize(0));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setMaxConnections(0));
        assertThrows(IllegalArgumentException.class, () -> tcpConfig.setIdleTimeoutSeconds(-1));
    }

    @Test
    public void blankPathsAreNormalizedToNull() {
        final SyslogTcpConfig tcpConfig = new SyslogTcpConfig();
        tcpConfig.setTlsCertFilePath("   ");
        tcpConfig.setTlsPrivateKeyFilePath("  /etc/key.pem  ");

        assertNull(tcpConfig.getTlsCertFilePath());
        assertEquals("/etc/key.pem", tcpConfig.getTlsPrivateKeyFilePath());
    }
}
