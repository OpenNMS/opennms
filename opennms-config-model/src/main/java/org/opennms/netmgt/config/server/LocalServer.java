/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.server;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the opennms-server.xml
 *  configuration file.
 */

@XmlRootElement(name="local-server")
@ValidateUsing("opennms-server.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalServer implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="server-name")
    private String m_serverName;

    @XmlAttribute(name="defaultCriticalPathIp")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress m_defaultCriticalPathIp;

    @XmlAttribute(name="defaultCriticalPathService")
    private String m_defaultCriticalPathService;

    @XmlAttribute(name="defaultCriticalPathTimeout")
    private Integer m_defaultCriticalPathTimeout;

    @XmlAttribute(name="defaultCriticalPathRetries")
    private Integer m_defaultCriticalPathRetries;

    /**
     * A flag to indicate if poller has to identify the nms
     *  server to restrict services to poll.
     */
    @XmlAttribute(name="verify-server")
    private Boolean m_verifyServer;

    public LocalServer() {
    }

    public String getServerName() {
        return m_serverName == null? "localhost" : m_serverName;
    }

    public void setServerName(final String serverName) {
        m_serverName = ConfigUtils.normalizeString(serverName);
    }

    public Optional<InetAddress> getDefaultCriticalPathIp() {
        return Optional.ofNullable(m_defaultCriticalPathIp);
    }

    public void setDefaultCriticalPathIp(final InetAddress ip) {
        m_defaultCriticalPathIp = ip;
    }

    public Optional<String> getDefaultCriticalPathService() {
        return Optional.ofNullable(m_defaultCriticalPathService);
    }

    public void setDefaultCriticalPathService(final String defaultCriticalPathService) {
        m_defaultCriticalPathService = ConfigUtils.normalizeString(defaultCriticalPathService);
    }

    public Integer getDefaultCriticalPathTimeout() {
        return m_defaultCriticalPathTimeout == null? 1500 : m_defaultCriticalPathTimeout;
    }

    public void setDefaultCriticalPathTimeout(final Integer timeout) {
        m_defaultCriticalPathTimeout = timeout;
    }

    public int getDefaultCriticalPathRetries() {
        return m_defaultCriticalPathRetries == null? 0 : m_defaultCriticalPathRetries;
    }

    public void setDefaultCriticalPathRetries(final Integer retries) {
        m_defaultCriticalPathRetries = retries;
    }

    public Boolean getVerifyServer() {
        return m_verifyServer == null? Boolean.FALSE : m_verifyServer;
    }

    public void setVerifyServer(final Boolean verifyServer) {
        m_verifyServer = verifyServer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_serverName,
                            m_defaultCriticalPathIp,
                            m_defaultCriticalPathService,
                            m_defaultCriticalPathTimeout,
                            m_defaultCriticalPathRetries,
                            m_verifyServer);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalServer) {
            final LocalServer that = (LocalServer) obj;
            return Objects.equals(this.m_serverName, that.m_serverName) &&
                    Objects.equals(this.m_defaultCriticalPathIp, that.m_defaultCriticalPathIp) &&
                    Objects.equals(this.m_defaultCriticalPathService, that.m_defaultCriticalPathService) &&
                    Objects.equals(this.m_defaultCriticalPathTimeout, that.m_defaultCriticalPathTimeout) &&
                    Objects.equals(this.m_defaultCriticalPathRetries, that.m_defaultCriticalPathRetries) &&
                    Objects.equals(this.m_verifyServer, that.m_verifyServer);
        }
        return false;
    }

}
