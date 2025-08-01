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
package org.opennms.netmgt.config.wmi.agent;

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
 * This is the top-level element for wmi-config.xml
 */
@XmlRootElement(name = "wmi-config")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("wmi-config.xsd")
public class WmiConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Default timeout (in milliseconds).
     */
    @XmlAttribute(name = "timeout")
    private Integer m_timeout;

    /**
     * Default number of retries.
     */
    @XmlAttribute(name = "retry")
    private Integer m_retry;

    /**
     * Default username.
     */
    @XmlAttribute(name = "username")
    private String m_username;

    /**
     * Default Windows Domain.
     */
    @XmlAttribute(name = "domain")
    private String m_domain;

    /**
     * Default user password.
     */
    @XmlAttribute(name = "password")
    private String m_password;

    /**
     * Maps IP addresses to specific SNMP parmeters
     *  (retries, timeouts...)
     */
    @XmlElement(name = "definition")
    private List<Definition> m_definitions = new ArrayList<>();

    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(m_timeout);
    }

    public void setTimeout(final Integer timeout) {
        m_timeout = timeout;
    }

    public Optional<Integer> getRetry() {
        return Optional.ofNullable(m_retry);
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

    public Optional<String> getDomain() {
        return Optional.ofNullable(m_domain);
    }

    public void setDomain(final String domain) {
        m_domain = ConfigUtils.normalizeString(domain);
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

    public void addDefinition(final Definition definition) {
        m_definitions.add(definition);
    }

    public boolean removeDefinition(final Definition definition) {
        return m_definitions.remove(definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_timeout, 
                            m_retry, 
                            m_username, 
                            m_domain, 
                            m_password, 
                            m_definitions);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof WmiConfig) {
            final WmiConfig that = (WmiConfig)obj;
            return Objects.equals(this.m_timeout, that.m_timeout)
                    && Objects.equals(this.m_retry, that.m_retry)
                    && Objects.equals(this.m_username, that.m_username)
                    && Objects.equals(this.m_domain, that.m_domain)
                    && Objects.equals(this.m_password, that.m_password)
                    && Objects.equals(this.m_definitions, that.m_definitions);
        }
        return false;
    }

}
