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
package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.config.syslogd.SyslogTcpConfig;
import org.opennms.netmgt.config.syslogd.SyslogTcpTlsConfig;

import io.netty.handler.ssl.util.SelfSignedCertificate;

public class SyslogTcpSslContextFactoryTest {

    private static SelfSignedCertificate s_certificate;

    @BeforeClass
    public static void generateCertificate() throws Exception {
        s_certificate = new SelfSignedCertificate();
    }

    @AfterClass
    public static void deleteCertificate() {
        if (s_certificate != null) {
            s_certificate.delete();
        }
    }

    @Test
    public void buildsAContextFromACertificateAndKey() throws Exception {
        assertNotNull(SyslogTcpSslContextFactory.create(tlsConfig()));
    }

    @Test
    public void buildsAContextForMutualTls() throws Exception {
        final SyslogTcpConfig config = tlsConfig();
        tlsOf(config).setClientAuth("require");
        tlsOf(config).setTrustCertFilePath(s_certificate.certificate().getAbsolutePath());

        assertNotNull(SyslogTcpSslContextFactory.create(config));
    }

    @Test
    public void refusesToBuildWithoutACertificate() {
        final SyslogTcpConfig config = new SyslogTcpConfig();
        config.setPort(6514);
        tlsOf(config).setEnabled(true);

        final Exception e = assertThrows(IllegalStateException.class, () -> SyslogTcpSslContextFactory.create(config));
        assertTrue(e.getMessage(), e.getMessage().contains("cert-filepath"));
    }

    @Test
    public void refusesToBuildWithoutAPrivateKey() {
        final SyslogTcpConfig config = new SyslogTcpConfig();
        config.setPort(6514);
        tlsOf(config).setEnabled(true);
        tlsOf(config).setCertFilePath(s_certificate.certificate().getAbsolutePath());

        final Exception e = assertThrows(IllegalStateException.class, () -> SyslogTcpSslContextFactory.create(config));
        assertTrue(e.getMessage(), e.getMessage().contains("private-key-filepath"));
    }

    @Test
    public void refusesToBuildWhenAPathDoesNotExist() {
        final SyslogTcpConfig config = tlsConfig();
        tlsOf(config).setCertFilePath("/definitely/not/here/syslog.crt");

        final Exception e = assertThrows(IllegalStateException.class, () -> SyslogTcpSslContextFactory.create(config));
        // Naming the offending path is the whole point; a generic failure sends the
        // operator hunting through the config by hand.
        assertTrue(e.getMessage(), e.getMessage().contains("/definitely/not/here/syslog.crt"));
    }

    @Test
    public void refusesMutualTlsWithoutTrustedCertificates() {
        final SyslogTcpConfig config = tlsConfig();
        tlsOf(config).setClientAuth("require");

        final Exception e = assertThrows(IllegalStateException.class, () -> SyslogTcpSslContextFactory.create(config));
        assertTrue(e.getMessage(), e.getMessage().contains("trust-cert-filepath"));
    }

    private static SyslogTcpConfig tlsConfig() {
        final SyslogTcpConfig config = new SyslogTcpConfig();
        config.setPort(6514);
        tlsOf(config).setEnabled(true);
        tlsOf(config).setCertFilePath(s_certificate.certificate().getAbsolutePath());
        tlsOf(config).setPrivateKeyFilePath(s_certificate.privateKey().getAbsolutePath());
        return config;
    }

    /** Lazily attaches the tls element, so each test only sets what it cares about. */
    private static SyslogTcpTlsConfig tlsOf(final SyslogTcpConfig config) {
        if (config.getTls() == null) {
            config.setTls(new SyslogTcpTlsConfig());
        }
        return config.getTls();
    }

}
