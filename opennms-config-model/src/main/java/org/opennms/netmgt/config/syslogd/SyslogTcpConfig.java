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

/**
 * The settings for the syslog TCP listener.
 *
 * Carried as a single object so that the two {@code SyslogdConfig} implementations,
 * one reading syslogd-configuration.xml and one populated from the Minion .cfg, do not
 * each have to grow a dozen parallel accessors.
 *
 * The framing and client authentication properties are typed as strings rather than as
 * enums because the containers populate them from string properties, and a getter and
 * setter that disagree on type turns the property read-only under bean introspection.
 * Use {@link #resolveFraming()} and {@link #resolveTlsClientAuth()} to get the parsed
 * values; the setters validate eagerly so a typo fails at wiring time.
 */
public class SyslogTcpConfig {

    public static final int DEFAULT_MAX_MESSAGE_SIZE = 65536;
    public static final int DEFAULT_MAX_CONNECTIONS = 1024;
    public static final int DEFAULT_IDLE_TIMEOUT_SECONDS = 0;

    /** Null or non-positive leaves TCP ingestion switched off. */
    private Integer port;

    private String listenAddress;

    private String framing = SyslogTcpFraming.AUTO.getConfigValue();

    private int maxMessageSize = DEFAULT_MAX_MESSAGE_SIZE;

    private int maxConnections = DEFAULT_MAX_CONNECTIONS;

    private int idleTimeoutSeconds = DEFAULT_IDLE_TIMEOUT_SECONDS;

    private boolean tlsEnabled;

    private String tlsCertFilePath;

    private String tlsPrivateKeyFilePath;

    private String tlsTrustCertFilePath;

    private String tlsClientAuth = SyslogTcpClientAuth.NONE.getConfigValue();

    /**
     * Whether a TCP listener should be started at all. TCP is opt-in, so an install
     * that has never been touched leaves the port unset and gets the UDP-only
     * behaviour it had before.
     */
    public boolean isEnabled() {
        return port != null && port > 0;
    }

    public Integer getPort() {
        return port;
    }

    /**
     * Zero and null both mean disabled. The Minion .cfg always carries the key, so it
     * needs a value that switches TCP off; anything else out of range is a mistake.
     */
    public void setPort(final Integer port) {
        if (port != null && port != 0 && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("syslog TCP port must be between 1 and 65535, or 0 to disable, got " + port);
        }
        this.port = port;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(final String listenAddress) {
        this.listenAddress = listenAddress == null || listenAddress.trim().isEmpty() ? null : listenAddress.trim();
    }

    public String getFraming() {
        return framing;
    }

    public void setFraming(final String framing) {
        // Normalize through the enum so that an unsupported value is rejected here
        // rather than on the first message of the first connection.
        this.framing = SyslogTcpFraming.fromConfigValue(framing).getConfigValue();
    }

    public SyslogTcpFraming resolveFraming() {
        return SyslogTcpFraming.fromConfigValue(framing);
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(final int maxMessageSize) {
        if (maxMessageSize < 1) {
            throw new IllegalArgumentException("syslog TCP maximum message size must be positive, got " + maxMessageSize);
        }
        this.maxMessageSize = maxMessageSize;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(final int maxConnections) {
        if (maxConnections < 1) {
            throw new IllegalArgumentException("syslog TCP maximum connection count must be positive, got " + maxConnections);
        }
        this.maxConnections = maxConnections;
    }

    public int getIdleTimeoutSeconds() {
        return idleTimeoutSeconds;
    }

    public void setIdleTimeoutSeconds(final int idleTimeoutSeconds) {
        if (idleTimeoutSeconds < 0) {
            throw new IllegalArgumentException("syslog TCP idle timeout cannot be negative, got " + idleTimeoutSeconds);
        }
        this.idleTimeoutSeconds = idleTimeoutSeconds;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public void setTlsEnabled(final boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public String getTlsCertFilePath() {
        return tlsCertFilePath;
    }

    public void setTlsCertFilePath(final String tlsCertFilePath) {
        this.tlsCertFilePath = normalizePath(tlsCertFilePath);
    }

    public String getTlsPrivateKeyFilePath() {
        return tlsPrivateKeyFilePath;
    }

    public void setTlsPrivateKeyFilePath(final String tlsPrivateKeyFilePath) {
        this.tlsPrivateKeyFilePath = normalizePath(tlsPrivateKeyFilePath);
    }

    public String getTlsTrustCertFilePath() {
        return tlsTrustCertFilePath;
    }

    public void setTlsTrustCertFilePath(final String tlsTrustCertFilePath) {
        this.tlsTrustCertFilePath = normalizePath(tlsTrustCertFilePath);
    }

    public String getTlsClientAuth() {
        return tlsClientAuth;
    }

    public void setTlsClientAuth(final String tlsClientAuth) {
        this.tlsClientAuth = SyslogTcpClientAuth.fromConfigValue(tlsClientAuth).getConfigValue();
    }

    public SyslogTcpClientAuth resolveTlsClientAuth() {
        return SyslogTcpClientAuth.fromConfigValue(tlsClientAuth);
    }

    private static String normalizePath(final String path) {
        return path == null || path.trim().isEmpty() ? null : path.trim();
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, listenAddress, framing, maxMessageSize, maxConnections, idleTimeoutSeconds,
                tlsEnabled, tlsCertFilePath, tlsPrivateKeyFilePath, tlsTrustCertFilePath, tlsClientAuth);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SyslogTcpConfig)) {
            return false;
        }
        final SyslogTcpConfig other = (SyslogTcpConfig) obj;
        return Objects.equals(port, other.port)
                && Objects.equals(listenAddress, other.listenAddress)
                && Objects.equals(framing, other.framing)
                && maxMessageSize == other.maxMessageSize
                && maxConnections == other.maxConnections
                && idleTimeoutSeconds == other.idleTimeoutSeconds
                && tlsEnabled == other.tlsEnabled
                && Objects.equals(tlsCertFilePath, other.tlsCertFilePath)
                && Objects.equals(tlsPrivateKeyFilePath, other.tlsPrivateKeyFilePath)
                && Objects.equals(tlsTrustCertFilePath, other.tlsTrustCertFilePath)
                && Objects.equals(tlsClientAuth, other.tlsClientAuth);
    }

    @Override
    public String toString() {
        return "SyslogTcpConfig[port=" + port
                + ", listenAddress=" + (listenAddress == null ? "0.0.0.0" : listenAddress)
                + ", framing=" + framing
                + ", maxMessageSize=" + maxMessageSize
                + ", maxConnections=" + maxConnections
                + ", idleTimeoutSeconds=" + idleTimeoutSeconds
                + ", tlsEnabled=" + tlsEnabled
                + ", tlsClientAuth=" + tlsClientAuth + "]";
    }
}
