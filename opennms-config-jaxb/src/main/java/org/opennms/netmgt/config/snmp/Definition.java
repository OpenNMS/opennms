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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Provides a mechanism for associating one or more specific IP addresses
 * and/or IP address ranges with a set of SNMP parms which will be used in
 * place of the default values during SNMP data collection.
 */
@XmlRootElement(name="definition")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"ranges","specifics","ipMatches"})
@JsonPropertyOrder({"ranges","specifics","ipMatches"})
/**
 * Keep the XML annotation is due to existing UI still using xml output
 */
public class Definition extends Configuration implements Serializable {
    private static final long serialVersionUID = 5646937263626185373L;
    /**
     * IP address range to which this definition
     *  applies.
     */
    @XmlElement(name="range")
    @JsonProperty("range")
    private List<Range> ranges = new ArrayList<>();

    /**
     * Specific IP address to which this definition
     *  applies.
     */
    @XmlElement(name="specific")
    @JsonProperty("specific")
    private List<String> specifics = new ArrayList<>();

    /**
     * Match Octets (as in IPLIKE)
     */
    @XmlElement(name="ip-match")
    @JsonProperty("ip-match")
    private List<String> ipMatches = new ArrayList<>();

    @XmlAttribute(name="location")
    @JsonProperty("location")
    private String location;

    @XmlAttribute(name="profile-label")
    @JsonProperty("profile-label")
    private String profileLabel;

    public Definition() {
        super();
    }

    public List<Range> getRanges() {
        if (ranges == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(ranges);
        }
    }

    public void setRanges(final List<Range> ranges) {
        this.ranges = new ArrayList<Range>(ranges);
    }

    public void addRange(final Range range) throws IndexOutOfBoundsException {
        ranges.add(range);
    }

    public boolean removeRange(final Range range) {
        return ranges.remove(range);
    }

    public List<String> getSpecifics() {
        if (specifics == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(specifics);
        }
    }

    public void setSpecifics(final List<String> specifics) {
        this.specifics = new ArrayList<String>(specifics);
    }

    public void addSpecific(final String specific) throws IndexOutOfBoundsException {
        specifics.add(specific);
    }

    public boolean removeSpecific(final String specific) {
        return specifics.remove(specific);
    }

    public List<String> getIpMatches() {
        if (ipMatches == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(ipMatches);
        }
    }

    public void setIpMatches(final List<String> ipMatches) {
        this.ipMatches = new ArrayList<String>(ipMatches);
    }

    public void addIpMatch(final String ipMatch) throws IndexOutOfBoundsException {
        ipMatches.add(ipMatch);
    }

    public boolean removeIpMatch(final String ipMatch) {
        return ipMatches.remove(ipMatch);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfileLabel() {
        return profileLabel;
    }

    public void setProfileLabel(String profileLabel) {
        this.profileLabel = profileLabel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ipMatches == null) ? 0 : ipMatches.hashCode());
        result = prime * result + ((ranges == null) ? 0 : ranges.hashCode());
        result = prime * result + ((specifics == null) ? 0 : specifics.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((profileLabel == null) ? 0 : profileLabel.hashCode());
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
        if (ipMatches == null) {
            if (other.ipMatches != null) {
                return false;
            }
        } else if (!ipMatches.equals(other.ipMatches)) {
            return false;
        }
        if (ranges == null) {
            if (other.ranges != null) {
                return false;
            }
        } else if (!ranges.equals(other.ranges)) {
            return false;
        }
        if (specifics == null) {
            if (other.specifics != null) {
                return false;
            }
        } else if (!specifics.equals(other.specifics)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        } else if (profileLabel!=null && other.profileLabel!=null && !profileLabel.equals(other.profileLabel)) {
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
                + getWriteCommunity() + ", ranges=" + ranges + ", specifics=" + specifics + ", ipMatches=" + ipMatches + "]";
    }
}
