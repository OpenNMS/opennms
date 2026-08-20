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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * The TLS settings for the syslog TCP listener, as described by RFC 5425.
 *
 * A nested element rather than attributes on the parent, so that further TLS settings can
 * be added without widening the TCP element.
 *
 * The client authentication property is typed as a string rather than as an enum because
 * the containers populate it from a string property, and a getter and setter that disagree
 * on type turns the property read-only under bean introspection. The setter validates
 * eagerly, so a typo fails at wiring time.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogTcpTlsConfig {

    @XmlAttribute(name = "enabled")
    private boolean enabled;

    @XmlAttribute(name = "cert-filepath")
    private String certFilePath;

    @XmlAttribute(name = "private-key-filepath")
    private String privateKeyFilePath;

    @XmlAttribute(name = "trust-cert-filepath")
    private String trustCertFilePath;

    @XmlAttribute(name = "client-auth")
    private String clientAuth;

    /**
     * Present but not enabled is allowed, so that switching TLS off does not mean deleting
     * the certificate paths from the file.
     *
     * A primitive rather than a nullable Boolean: the getter and setter have to agree on
     * type or bean introspection treats the property as read-only, and absence means false
     * either way.
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getCertFilePath() {
        return certFilePath;
    }

    public void setCertFilePath(final String certFilePath) {
        this.certFilePath = normalizePath(certFilePath);
    }

    public String getPrivateKeyFilePath() {
        return privateKeyFilePath;
    }

    public void setPrivateKeyFilePath(final String privateKeyFilePath) {
        this.privateKeyFilePath = normalizePath(privateKeyFilePath);
    }

    public String getTrustCertFilePath() {
        return trustCertFilePath;
    }

    public void setTrustCertFilePath(final String trustCertFilePath) {
        this.trustCertFilePath = normalizePath(trustCertFilePath);
    }

    public String getClientAuth() {
        return clientAuth != null ? clientAuth : SyslogTcpClientAuth.NONE.getConfigValue();
    }

    public void setClientAuth(final String clientAuth) {
        this.clientAuth = SyslogTcpClientAuth.fromConfigValue(clientAuth).getConfigValue();
    }

    public SyslogTcpClientAuth resolveClientAuth() {
        return SyslogTcpClientAuth.fromConfigValue(clientAuth);
    }

    private static String normalizePath(final String path) {
        return path == null || path.trim().isEmpty() ? null : path.trim();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Boolean.valueOf(enabled), certFilePath, privateKeyFilePath, trustCertFilePath, clientAuth);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SyslogTcpTlsConfig)) {
            return false;
        }
        final SyslogTcpTlsConfig other = (SyslogTcpTlsConfig) obj;
        return enabled == other.enabled
                && Objects.equals(certFilePath, other.certFilePath)
                && Objects.equals(privateKeyFilePath, other.privateKeyFilePath)
                && Objects.equals(trustCertFilePath, other.trustCertFilePath)
                && Objects.equals(clientAuth, other.clientAuth);
    }

    @Override
    public String toString() {
        return "SyslogTcpTlsConfig[enabled=" + isEnabled()
                + ", clientAuth=" + getClientAuth() + "]";
    }
}
