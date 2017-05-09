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

package org.opennms.netmgt.config.syslogd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * List of Strings to UEI matches
 */
@XmlRootElement(name = "ueiMatch")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class UeiMatch implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The name of a syslog facility. If present, the facility of
     *  an incoming message must match one of the facilities named
     *  by an instance of this tag within the ueiMatch.
     */
    @XmlElement(name = "facility")
    private List<String> m_facilities = new ArrayList<>();

    /**
     * The name of a syslog severity. If present, the severity of
     *  an incoming message must match one of the severities named
     *  by an instance of this tag within the ueiMatch.
     */
    @XmlElement(name = "severity")
    private List<String> m_severities = new ArrayList<>();

    /**
     * String against which to match the process name; interpreted
     *  as a regular expression. If no process name is present in
     *  the incoming message, any process-match elements will be
     *  considered non-matches.
     */
    @XmlElement(name = "process-match")
    private ProcessMatch m_processMatch;

    /**
     * String against which to match the hostname; interpreted
     *  as a regular expression.
     */
    @XmlElement(name = "hostname-match")
    private HostnameMatch m_hostnameMatch;

    /**
     * String against which to match the host IP address; interpreted
     *  as a regular expression.
     */
    @XmlElement(name = "hostaddr-match")
    private HostaddrMatch m_hostaddrMatch;

    /**
     * String against which to match the message body; interpreted
     *  as a substring or a regular expression according to the
     *  value of the "type" attribute
     */
    @XmlElement(name = "match", required = true)
    private Match m_match;

    /**
     * UEI
     */
    @XmlElement(name = "uei", required = true)
    private String m_uei;

    /**
     * For regex matches, assign the value of a matching group
     *  to a named event parameter
     */
    @XmlElement(name = "parameter-assignment")
    private List<ParameterAssignment> m_parameterAssignments = new ArrayList<>();

    public UeiMatch() {
    }

    public List<String> getFacilities() {
        return m_facilities;
    }

    public void setFacilities(final List<String> facilities) {
        if (facilities == m_facilities) return;
        m_facilities.clear();
        if (facilities != null) m_facilities.addAll(facilities);
    }

    public void addFacility(final String facility) {
        m_facilities.add(facility);
    }

    public boolean removeFacility(final String facility) {
        return m_facilities.remove(facility);
    }

    public List<String> getSeverities() {
        return m_severities;
    }

    public void setSeverities(final List<String> severities) {
        if (severities == m_severities) return;
        m_severities.clear();
        if (severities != null) m_severities.addAll(severities);
    }

    public void addSeverity(final String severity) {
        m_severities.add(severity);
    }

    public boolean removeSeverity(final String severity) {
        return m_severities.remove(severity);
    }

    public Optional<ProcessMatch> getProcessMatch() {
        return Optional.ofNullable(m_processMatch);
    }

    public void setProcessMatch(final ProcessMatch processMatch) {
        m_processMatch = processMatch;
    }

    public Optional<HostnameMatch> getHostnameMatch() {
        return Optional.ofNullable(m_hostnameMatch);
    }

    public void setHostnameMatch(final HostnameMatch hostnameMatch) {
        m_hostnameMatch = hostnameMatch;
    }

    public Optional<HostaddrMatch> getHostaddrMatch() {
        return Optional.ofNullable(m_hostaddrMatch);
    }

    public void setHostaddrMatch(final HostaddrMatch hostaddrMatch) {
        m_hostaddrMatch = hostaddrMatch;
    }

    public Match getMatch() {
        return m_match;
    }

    public void setMatch(final Match match) {
        m_match = ConfigUtils.assertNotNull(match, "match");
    }

    public String getUei() {
        return m_uei;
    }

    public void setUei(final String uei) {
        m_uei = ConfigUtils.assertNotEmpty(uei, "uei");
    }

    public List<ParameterAssignment> getParameterAssignments() {
        return m_parameterAssignments;
    }

    public void setParameterAssignments(final List<ParameterAssignment> parameterAssignments) {
        if (parameterAssignments == m_parameterAssignments) return;
        m_parameterAssignments.clear();
        if (parameterAssignments != null) m_parameterAssignments.addAll(parameterAssignments);
    }

    public void addParameterAssignment(final ParameterAssignment parameterAssignment) {
        m_parameterAssignments.add(parameterAssignment);
    }

    public boolean removeParameterAssignment(final ParameterAssignment parameterAssignment) {
        return m_parameterAssignments.remove(parameterAssignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_facilities, 
                            m_severities, 
                            m_processMatch, 
                            m_hostnameMatch, 
                            m_hostaddrMatch, 
                            m_match, 
                            m_uei, 
                            m_parameterAssignments);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof UeiMatch) {
            final UeiMatch that = (UeiMatch)obj;
            return Objects.equals(this.m_facilities, that.m_facilities)
                    && Objects.equals(this.m_severities, that.m_severities)
                    && Objects.equals(this.m_processMatch, that.m_processMatch)
                    && Objects.equals(this.m_hostnameMatch, that.m_hostnameMatch)
                    && Objects.equals(this.m_hostaddrMatch, that.m_hostaddrMatch)
                    && Objects.equals(this.m_match, that.m_match)
                    && Objects.equals(this.m_uei, that.m_uei)
                    && Objects.equals(this.m_parameterAssignments, that.m_parameterAssignments);
        }
        return false;
    }

}
