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
package org.opennms.netmgt.config.snmp;

import org.codehaus.jackson.annotate.JsonProperty;

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
@XmlType(propOrder={"range", "specific", "ipMatch", "location", "profileLabel"})
public class Definition extends Configuration implements Serializable {
    private static final long serialVersionUID = 5646937263626185373L;
    /**
     * IP address range to which this definition
     *  applies.
     */
    @XmlElement(name="range")
    private List<Range> range = new ArrayList<>();

    /**
     * Specific IP address to which this definition
     *  applies.
     */
    @XmlElement(name="specific")
    private List<String> specific = new ArrayList<>();

    /**
     * Match Octets (as in IPLIKE)
     */
    @XmlElement(name="ip-match")
    private List<String> ipMatch = new ArrayList<>();

    @XmlAttribute(name="location")
    private String location;

    @XmlAttribute(name="profile-label")
    private String profileLabel;

    public Definition() {
        super();
    }

    public List<Range> getRanges() {
        if (range == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(range);
        }
    }

    public void setRanges(final List<Range> ranges) {
        this.range = new ArrayList<Range>(ranges);
    }

    public void addRange(final Range range) throws IndexOutOfBoundsException {
        this.range.add(range);
    }

    public boolean removeRange(final Range range) {
        return this.range.remove(range);
    }

    public List<String> getSpecifics() {
        if (specific == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(specific);
        }
    }

    public void setSpecifics(final List<String> specifics) {
        specific = new ArrayList<String>(specifics);
    }

    public void addSpecific(final String specific) throws IndexOutOfBoundsException {
        this.specific.add(specific);
    }

    public boolean removeSpecific(final String specific) {
        return this.specific.remove(specific);
    }

    public List<String> getIpMatches() {
        if (ipMatch == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(ipMatch);
        }
    }

    public void setIpMatches(final List<String> ipMatches) {
        ipMatch = new ArrayList<String>(ipMatches);
    }

    public void addIpMatch(final String ipMatch) throws IndexOutOfBoundsException {
        this.ipMatch.add(ipMatch);
    }

    public boolean removeIpMatch(final String ipMatch) {
        return this.ipMatch.remove(ipMatch);
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
        result = prime * result + ((ipMatch == null) ? 0 : ipMatch.hashCode());
        result = prime * result + ((range == null) ? 0 : range.hashCode());
        result = prime * result + ((specific == null) ? 0 : specific.hashCode());
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
        if (ipMatch == null) {
            if (other.ipMatch != null) {
                return false;
            }
        } else if (!ipMatch.equals(other.ipMatch)) {
            return false;
        }
        if (range == null) {
            if (other.range != null) {
                return false;
            }
        } else if (!range.equals(other.range)) {
            return false;
        }
        if (specific == null) {
            if (other.specific != null) {
                return false;
            }
        } else if (!specific.equals(other.specific)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        } else if (!profileLabel.equals(other.profileLabel)) {
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
                + getWriteCommunity() + ", ranges=" + range + ", specifics=" + specific + ", ipMatches=" + ipMatch + "]";
    }
}
