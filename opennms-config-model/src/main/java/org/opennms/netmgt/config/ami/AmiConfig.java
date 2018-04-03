/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ami;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This is the top-level element for ami-config.xml, which configures access
 * parameters for the Asterisk Manager Interface (AMI).
 */
@XmlRootElement(name = "ami-config")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ami-config.xsd")
public class AmiConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "port")
    private Integer m_port;

    @XmlAttribute(name = "use-ssl")
    private Boolean m_useSsl;

    /**
     * Default connection timeout (in milliseconds).
     */
    @XmlAttribute(name = "timeout")
    private Integer m_timeout;

    /**
     * Default connection retries.
     */
    @XmlAttribute(name = "retry")
    private Integer m_retry;

    /**
     * Default AMI username (Name in brackets in Asterisk's manager.conf).
     */
    @XmlAttribute(name = "username", required = true)
    private String m_username;

    /**
     * Default AMI password.
     */
    @XmlAttribute(name = "password")
    private String m_password;

    /**
     * Maps IP addresses to specific AMI parameters (username, password,
     * port...)
     */
    @XmlElement(name = "definition")
    private List<Definition> m_definitions = new ArrayList<>();

    public AmiConfig() {
    }

    public AmiConfig(final Integer port, final Boolean useSsl, final Integer timeout,
            final Integer retry, final String username, final String password,
            final List<Definition> definitions) {
        setPort(port);
        setUseSsl(useSsl);
        setTimeout(timeout);
        setRetry(retry);
        setUsername(username);
        setPassword(password);
        setDefinitions(definitions);
    }

    public Integer getPort() {
        return m_port == null ? 5038 : m_port;
    }

    public void setPort(final Integer port) {
        m_port = port;
    }

    public Boolean getUseSsl() {
        return m_useSsl == null ? false : m_useSsl;
    }

    public void setUseSsl(final Boolean useSsl) {
        m_useSsl = useSsl;
    }

    public Integer getTimeout() {
        return m_timeout == null ? 3000 : m_timeout;
    }

    public void setTimeout(final Integer timeout) {
        m_timeout = timeout;
    }

    public Integer getRetry() {
        return m_retry == null ? 0 : m_retry;
    }

    public void setRetry(final Integer retry) {
        m_retry = retry;
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

    public List<Definition> getDefinitions() {
        return m_definitions;
    }

    public void setDefinitions(final List<Definition> definitions) {
        if (definitions == m_definitions) return;
        m_definitions.clear();
        if (definitions != null) m_definitions.addAll(definitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_port, m_useSsl, m_timeout, m_retry, m_username, m_password, m_definitions);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AmiConfig) {
            final AmiConfig that = (AmiConfig) obj;
            return Objects.equals(this.m_port, that.m_port) &&
                    Objects.equals(this.m_useSsl, that.m_useSsl) &&
                    Objects.equals(this.m_timeout, that.m_timeout) &&
                    Objects.equals(this.m_retry, that.m_retry) &&
                    Objects.equals(this.m_username, that.m_username) &&
                    Objects.equals(this.m_password, that.m_password) &&
                    Objects.equals(this.m_definitions, that.m_definitions);
        }
        return false;
    }
}
