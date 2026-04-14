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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-config.xsd")
@XmlType(propOrder = {"label", "filter"})
public class SnmpProfile extends Configuration {

    private static final long serialVersionUID = 6047134979704016780L;

    @JsonProperty("label")
    @XmlElement(name = "label")
    private String label = "";

    @JsonProperty("filter")
    @XmlElement(name = "filter")
    private String filter = "";

    public SnmpProfile() {
    }

    public SnmpProfile(Integer port,
                       Integer retry,
                       Integer timeout,
                       String readCommunity,
                       String writeCommunity,
                       String proxyHost,
                       String version,
                       Integer maxVarsPerPdu,
                       Integer maxRepetitions,
                       Integer maxRequestSize,
                       String securityName,
                       Integer securityLevel,
                       String authPassphrase,
                       String authProtocol,
                       String engineId,
                       String contextEngineId,
                       String contextName,
                       String privacyPassphrase,
                       String privacyProtocol,
                       String enterpriseId,
                       String label,
                       String filter) {
        super(port, retry, timeout, readCommunity, writeCommunity, proxyHost, version, maxVarsPerPdu,
                maxRepetitions, maxRequestSize, securityName, securityLevel, authPassphrase, authProtocol,
                engineId, contextEngineId, contextName, privacyPassphrase, privacyProtocol, enterpriseId);
        this.label = label;
        this.filter = filter;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (label != null) {
            this.label = label;
        }
    }

    @JsonIgnore
    public String getFilterExpression() {
        return filter;
    }

    public void setFilterExpression(String filterExpression) {
        if (filterExpression != null) {
            this.filter = filterExpression;
        }
    }

    public void visit(SnmpConfigVisitor visitor) {
        visitor.visitSnmpProfile(this);
        visitor.visitSnmpProfileFinished();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SnmpProfile that = (SnmpProfile) o;
        return Objects.equals(label, that.label) && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), label, filter);
    }

    @Override
    public String toString() {
        return "SnmpProfile [" +
                "label=" + label +
                ", filter=" + filter +
                ", proxyHost=" + getProxyHost() +
                ", maxVarsPerPdu=" + getMaxVarsPerPdu() +
                ", maxRepetitions=" + getMaxRepetitions() +
                ", maxRequestSize=" + getMaxRequestSize() +
                ", securityName=" + getSecurityName() +
                ", securityLevel=" + getSecurityLevel() +
                ", authPassphrase=" + MASKED_PASSWORD +
                ", authProtocol=" + getAuthProtocol() +
                ", engineId=" + getEngineId() +
                ", contextEngineId=" + getContextEngineId() +
                ", contextName=" + getContextName() +
                ", privacyPassphrase=" + MASKED_PASSWORD +
                ", privacyProtocol=" + getPrivacyProtocol() +
                ", enterpriseId=" + getEnterpriseId() +
                ", version=" + getVersion() +
                ", writeCommunity=" + MASKED_PASSWORD +
                ", readCommunity=" + MASKED_PASSWORD +
                ", timeout=" + getTimeout() +
                ", retry=" + getRetry() +
                ", port=" + getPort() +
                ", ttl=" + getTTL() +
                ", encrypted=" + getEncrypted() +
                "]";
    }
}
