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
import javax.xml.bind.annotation.XmlElement;

/**
 * The settings for the syslog TCP listener, the optional {@code tcp} element of
 * syslogd-configuration.xml.
 *
 * Carried as a single object so that the two {@code SyslogdConfig} implementations, one
 * reading the XML and one populated from the Minion .cfg, do not each have to grow a dozen
 * parallel accessors. The Minion keeps flat properties and populates this the same way.
 *
 * The framing property is typed as a string rather than as an enum because the containers
 * populate it from a string property, and a getter and setter that disagree on type turns
 * the property read-only under bean introspection. Use {@link #resolveFraming()} to get the
 * parsed value; the setter validates eagerly, so a typo fails at wiring time.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogTcpConfig {

    public static final int DEFAULT_MAX_MESSAGE_SIZE = 65536;
    public static final int DEFAULT_MAX_CONNECTIONS = 1024;
    public static final int DEFAULT_IDLE_TIMEOUT_SECONDS = 0;

    /**
     * Required in the XML, where the presence of the element is what asks for a listener.
     * The Minion .cfg always carries the key, so zero switches TCP off there.
     */
    @XmlAttribute(name = "port")
    private Integer port;

    @XmlAttribute(name = "listen-address")
    private String listenAddress;

    @XmlAttribute(name = "framing")
    private String framing;

    @XmlAttribute(name = "max-message-size")
    private Integer maxMessageSize;

    @XmlAttribute(name = "max-connections")
    private Integer maxConnections;

    @XmlAttribute(name = "idle-timeout")
    private Integer idleTimeoutSeconds;

    @XmlElement(name = "tls")
    private SyslogTcpTlsConfig tls;

    /**
     * Whether a listener should be started at all. TCP is opt-in: the XML leaves the element
     * out and the Minion leaves the port at zero.
     */
    public boolean isEnabled() {
        return port != null && port > 0;
    }

    public Integer getPort() {
        return port;
    }

    /**
     * Zero and null both mean disabled, because the Minion .cfg always carries the key and
     * needs a value that switches TCP off. Anything else out of range is a mistake.
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
        return framing != null ? framing : SyslogTcpFraming.AUTO.getConfigValue();
    }

    public void setFraming(final String framing) {
        // Normalized through the enum, so an unsupported value is rejected here rather than
        // on the first message of the first connection.
        this.framing = SyslogTcpFraming.fromConfigValue(framing).getConfigValue();
    }

    public SyslogTcpFraming resolveFraming() {
        return SyslogTcpFraming.fromConfigValue(framing);
    }

    public int getMaxMessageSize() {
        return maxMessageSize != null ? maxMessageSize : DEFAULT_MAX_MESSAGE_SIZE;
    }

    public void setMaxMessageSize(final int maxMessageSize) {
        if (maxMessageSize < 1) {
            throw new IllegalArgumentException("syslog TCP maximum message size must be positive, got " + maxMessageSize);
        }
        this.maxMessageSize = maxMessageSize;
    }

    public int getMaxConnections() {
        return maxConnections != null ? maxConnections : DEFAULT_MAX_CONNECTIONS;
    }

    public void setMaxConnections(final int maxConnections) {
        if (maxConnections < 1) {
            throw new IllegalArgumentException("syslog TCP maximum connection count must be positive, got " + maxConnections);
        }
        this.maxConnections = maxConnections;
    }

    public int getIdleTimeoutSeconds() {
        return idleTimeoutSeconds != null ? idleTimeoutSeconds : DEFAULT_IDLE_TIMEOUT_SECONDS;
    }

    public void setIdleTimeoutSeconds(final int idleTimeoutSeconds) {
        if (idleTimeoutSeconds < 0) {
            throw new IllegalArgumentException("syslog TCP idle timeout cannot be negative, got " + idleTimeoutSeconds);
        }
        this.idleTimeoutSeconds = idleTimeoutSeconds;
    }

    /** Null when the element carries no tls child, which is plaintext. */
    public SyslogTcpTlsConfig getTls() {
        return tls;
    }

    public void setTls(final SyslogTcpTlsConfig tls) {
        this.tls = tls;
    }

    public boolean isTlsEnabled() {
        return tls != null && tls.isEnabled();
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, listenAddress, framing, maxMessageSize, maxConnections, idleTimeoutSeconds, tls);
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
                && Objects.equals(maxMessageSize, other.maxMessageSize)
                && Objects.equals(maxConnections, other.maxConnections)
                && Objects.equals(idleTimeoutSeconds, other.idleTimeoutSeconds)
                && Objects.equals(tls, other.tls);
    }

    @Override
    public String toString() {
        return "SyslogTcpConfig[port=" + port
                + ", listenAddress=" + (listenAddress == null ? "0.0.0.0" : listenAddress)
                + ", framing=" + getFraming()
                + ", maxMessageSize=" + getMaxMessageSize()
                + ", maxConnections=" + getMaxConnections()
                + ", idleTimeoutSeconds=" + getIdleTimeoutSeconds()
                + ", tls=" + tls + "]";
    }
}
