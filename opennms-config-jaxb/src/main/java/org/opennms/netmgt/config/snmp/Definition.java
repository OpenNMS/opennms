/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.snmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Provides a mechanism for associating one or more specific IP addresses
 * and/or IP address ranges with a set of SNMP parms which will be used in
 * place of the default values during SNMP data collection.
 */

@XmlRootElement(name="definition")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"m_ranges","m_specifics","m_ipMatches"})
public class Definition extends Configuration implements Serializable {
    private static final long serialVersionUID = 5646937263626185373L;
    /**
     * IP address range to which this definition
     *  applies.
     */
    @XmlElement(name="range")
    private List<Range> m_ranges = new ArrayList<>();

    /**
     * Specific IP address to which this definition
     *  applies.
     */
    @XmlElement(name="specific")
    private List<String> m_specifics = new ArrayList<>();

    /**
     * Match Octets (as in IPLIKE)
     */
    @XmlElement(name="ip-match")
    private List<String> m_ipMatches = new ArrayList<>();

    @XmlAttribute(name="location")
    private String m_location;

    public Definition() {
        super();
    }

    public List<Range> getRanges() {
        if (m_ranges == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_ranges);
        }
    }

    public void setRanges(final List<Range> ranges) {
        m_ranges = new ArrayList<Range>(ranges);
    }

    public void addRange(final Range range) throws IndexOutOfBoundsException {
        m_ranges.add(range);
    }

    public boolean removeRange(final Range range) {
        return m_ranges.remove(range);
    }

    public List<String> getSpecifics() {
        if (m_specifics == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_specifics);
        }
    }

    public void setSpecifics(final List<String> specifics) {
        m_specifics = new ArrayList<String>(specifics);
    }

    public void addSpecific(final String specific) throws IndexOutOfBoundsException {
        m_specifics.add(specific);
    }

    public boolean removeSpecific(final String specific) {
        return m_specifics.remove(specific);
    }

    public List<String> getIpMatches() {
        if (m_ipMatches == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_ipMatches);
        }
    }

    public void setIpMatches(final List<String> ipMatches) {
        m_ipMatches = new ArrayList<String>(ipMatches);
    }

    public void addIpMatch(final String ipMatch) throws IndexOutOfBoundsException {
        m_ipMatches.add(ipMatch);
    }

    public boolean removeIpMatch(final String ipMatch) {
        return m_ipMatches.remove(ipMatch);
    }

    public String getLocation() {
        return m_location;
    }

    public void setLocation(String location) {
        this.m_location = location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((m_ipMatches == null) ? 0 : m_ipMatches.hashCode());
        result = prime * result + ((m_ranges == null) ? 0 : m_ranges.hashCode());
        result = prime * result + ((m_specifics == null) ? 0 : m_specifics.hashCode());
        result = prime * result + ((m_location == null) ? 0 : m_location.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Definition)) {
            return false;
        }
        final Definition other = (Definition) obj;
        if (m_ipMatches == null) {
            if (other.m_ipMatches != null) {
                return false;
            }
        } else if (!m_ipMatches.equals(other.m_ipMatches)) {
            return false;
        }
        if (m_ranges == null) {
            if (other.m_ranges != null) {
                return false;
            }
        } else if (!m_ranges.equals(other.m_ranges)) {
            return false;
        }
        if (m_specifics == null) {
            if (other.m_specifics != null) {
                return false;
            }
        } else if (!m_specifics.equals(other.m_specifics)) {
            return false;
        }
        if (m_location == null) {
            if (other.m_location != null) {
                return false;
            }
        } else if (!m_location.equals(other.m_location)) {
            return false;
        }

        return true;
    }

    public void visit(final SnmpConfigVisitor visitor) {
        visitor.visitDefinition(this);

        visitor.visitSpecifics(this.getSpecifics());
        visitor.visitSpecificsFinished();
        visitor.visitRanges(this.getRanges());
        visitor.visitRangesFinished();
        visitor.visitIpMatches(this.getIpMatches());
        visitor.visitIpMatchesFinished();

        visitor.visitDefinitionFinished();
    }

    @Override
    public String toString() {
        return "Definition [authPassphrase=" + getAuthPassphrase() + ", authProtocol=" + getAuthProtocol() + ", contextEngineId=" + getContextEngineId() + ", contextName="
                + getContextName() + ", engineId=" + getEngineId() + ", enterpriseId=" + getEnterpriseId() + ", maxRepetitions=" + getMaxRepetitions() + ", maxRequestSize="
                + getMaxRequestSize() + ", maxVarsPerPdu=" + getMaxVarsPerPdu() + ", port=" + getPort() + ", privacyPassphrase=" + getPrivacyPassphrase() + ", privacyProtocol="
                + getPrivacyProtocol() + ", proxyHost=" + getProxyHost() + ", readCommunity=" + getReadCommunity() + ", retries=" + getRetry() + ", securityLevel="
                + getSecurityLevel() + ", securityName=" + getSecurityName() + ", timeout=" + getTimeout() + ", version=" + getVersion() + ", writeCommunity="
                + getWriteCommunity() + ", ranges=" + m_ranges + ", specifics=" + m_specifics + ", ipMatches=" + m_ipMatches + "]";
    }
}
