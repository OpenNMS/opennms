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

import java.io.File;

import org.opennms.netmgt.config.syslogd.SyslogTcpClientAuth;
import org.opennms.netmgt.config.syslogd.SyslogTcpConfig;
import org.opennms.netmgt.config.syslogd.SyslogTcpTlsConfig;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * Builds the TLS context for the syslog TCP listener, as described by RFC 5425.
 *
 * Every problem is raised rather than worked around: falling back to plaintext would
 * accept syslog on a port the operator believes is encrypted.
 */
public final class SyslogTcpSslContextFactory {

    private SyslogTcpSslContextFactory() {
    }

    public static SslContext create(final SyslogTcpConfig config) throws Exception {
        if (!config.isTlsEnabled()) {
            throw new IllegalStateException("TLS is not enabled for the syslog TCP listener");
        }

        final SyslogTcpTlsConfig tls = config.getTls();
        final File certificate = requireFile(tls.getCertFilePath(), "cert-filepath",
                "the certificate the listener presents to senders");
        final File privateKey = requireFile(tls.getPrivateKeyFilePath(), "private-key-filepath",
                "the private key matching the listener certificate");

        final SslContextBuilder builder = SslContextBuilder.forServer(certificate, privateKey);

        final SyslogTcpClientAuth clientAuth = tls.resolveClientAuth();
        if (clientAuth != SyslogTcpClientAuth.NONE) {
            final File trustCertificates = requireFile(tls.getTrustCertFilePath(), "trust-cert-filepath",
                    "the certificates used to verify sender certificates, required when client-auth is "
                            + clientAuth.getConfigValue());
            builder.trustManager(trustCertificates);
        }

        builder.clientAuth(toNettyClientAuth(clientAuth));
        return builder.build();
    }

    private static ClientAuth toNettyClientAuth(final SyslogTcpClientAuth clientAuth) {
        switch (clientAuth) {
            case OPTIONAL:
                return ClientAuth.OPTIONAL;
            case REQUIRE:
                return ClientAuth.REQUIRE;
            case NONE:
            default:
                return ClientAuth.NONE;
        }
    }

    private static File requireFile(final String path, final String attribute, final String description) {
        if (path == null) {
            throw new IllegalStateException("TLS is enabled for the syslog TCP listener but " + attribute
                    + " is not set. It must point at " + description + ".");
        }

        final File file = new File(path);
        if (!file.isFile()) {
            throw new IllegalStateException(attribute + " points at '" + path + "', which is not a file.");
        }
        if (!file.canRead()) {
            throw new IllegalStateException(attribute + " points at '" + path + "', which cannot be read.");
        }
        return file;
    }
}
