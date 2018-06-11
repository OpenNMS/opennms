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
 * Provides a mechanism for associating one or more specific IP addresses
 * and/or IP address ranges with a set of AMI parms which will be used in
 * place of the default values during AMI operations.
 */
@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ami-config.xsd")
public class Definition implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "port")
    private Integer m_port;

    @XmlAttribute(name = "use-ssl")
    private Boolean m_useSsl;

    @XmlAttribute(name = "timeout")
    private Integer m_timeout;

    @XmlAttribute(name = "retry")
    private Integer m_retry;

    @XmlAttribute(name = "username")
    private String m_username;

    @XmlAttribute(name = "password")
    private String m_password;

    @XmlElement(name = "range")
    private List<Range> m_ranges = new ArrayList<>();

    @XmlElement(name = "specific")
    private List<String> m_specifics = new ArrayList<>();

    /**
     * Match Octets (as in IPLIKE)
     */
    @XmlElement(name = "ip-match")
    private List<String> m_ipMatches = new ArrayList<>();

    public Definition() {
    }

    public Definition(final Integer port, final Boolean useSsl,
            final Integer timeout, final Integer retry, final String username,
            final String password, final List<Range> ranges,
            final List<String> specifics, final List<String> ipMatches) {
        setPort(port);
        setUseSsl(useSsl);
        setTimeout(timeout);
        setRetry(retry);
        setUsername(username);
        setPassword(password);
        setRanges(ranges);
        setSpecifics(specifics);
        setIpMatches(ipMatches);
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(m_port);
    }

    public void setPort(final Integer port) {
        m_port = port;
    }

    public boolean getUseSsl() {
        return m_useSsl == null? false : m_useSsl;
    }

    public void setUseSsl(final Boolean useSsl) {
        m_useSsl = useSsl;
    }

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

    public Optional<String> getPassword() {
        return Optional.ofNullable(m_password);
    }

    public void setPassword(final String password) {
        m_password = password;
    }

    public List<Range> getRanges() {
        return m_ranges;
    }

    public void setRanges(final List<Range> ranges) {
        if (ranges == m_ranges) return;
        m_ranges.clear();
        if (ranges != null) m_ranges.addAll(ranges);
    }

    public List<String> getSpecifics() {
        return m_specifics;
    }

    public void setSpecifics(final List<String> specifics) {
        if (specifics == m_specifics) return;
        m_specifics.clear();
        if (specifics != null) m_specifics.addAll(specifics);
    }

    public void addSpecific(final String vSpecific) {
        m_specifics.add(vSpecific);
    }

    public List<String> getIpMatches() {
        return m_ipMatches;
    }

    public void setIpMatches(final List<String> ipMatches) {
        if (ipMatches == m_ipMatches) return;
        m_ipMatches.clear();
        if (ipMatches != null) m_ipMatches.addAll(ipMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_port, m_useSsl, m_timeout, m_retry, m_username, m_password, m_ranges, m_specifics, m_ipMatches);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Definition) {
            final Definition that = (Definition) obj;
            return Objects.equals(this.m_port, that.m_port) &&
                                  Objects.equals(this.m_useSsl, that.m_useSsl) &&
                                  Objects.equals(this.m_timeout, that.m_timeout) &&
                                  Objects.equals(this.m_retry, that.m_retry) &&
                                  Objects.equals(this.m_username, that.m_username) &&
                                  Objects.equals(this.m_password, that.m_password) &&
                                  Objects.equals(this.m_ranges, that.m_ranges) &&
                                  Objects.equals(this.m_specifics, that.m_specifics) &&
                                  Objects.equals(this.m_ipMatches, that.m_ipMatches);
        }
        return false;
    }
}
