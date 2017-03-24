/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.rws;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Stand By Url(s) for Rancid Servers.
 */
@XmlRootElement(name = "standby-url")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("rws-configuration.xsd")
public class StandbyUrl implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_DIRECTORY = "/rws";

    @XmlAttribute(name = "server_url", required = true)
    private String m_serverUrl;

    @XmlAttribute(name = "timeout")
    private Integer m_timeout;

    @XmlAttribute(name = "directory")
    private String m_directory;

    @XmlAttribute(name = "username")
    private String m_username;

    @XmlAttribute(name = "password")
    private String m_password;

    public String getServerUrl() {
        return m_serverUrl;
    }

    public void setServerUrl(final String serverUrl) {
        m_serverUrl = ConfigUtils.assertNotEmpty(serverUrl, "server_url");
    }

    public Integer getTimeout() {
        return m_timeout != null ? m_timeout : 3;
    }

    public void setTimeout(final Integer timeout) {
        m_timeout = timeout;
    }

    public String getDirectory() {
        return m_directory != null ? m_directory : DEFAULT_DIRECTORY;
    }

    public void setDirectory(final String directory) {
        m_directory = ConfigUtils.normalizeString(directory);
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(m_username);
    }

    public void setUsername(final String username) {
        m_username = ConfigUtils.normalizeString(username);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(m_password);
    }

    public void setPassword(final String password) {
        m_password = ConfigUtils.normalizeString(password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_serverUrl, 
                            m_timeout, 
                            m_directory, 
                            m_username, 
                            m_password);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof StandbyUrl) {
            final StandbyUrl that = (StandbyUrl)obj;
            return Objects.equals(this.m_serverUrl, that.m_serverUrl)
                    && Objects.equals(this.m_timeout, that.m_timeout)
                    && Objects.equals(this.m_directory, that.m_directory)
                    && Objects.equals(this.m_username, that.m_username)
                    && Objects.equals(this.m_password, that.m_password);
        }
        return false;
    }

}
