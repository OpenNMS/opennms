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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the snmp-config.xml configuration file.
 */
@XmlRootElement(name="snmp-config")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-config.xsd")
@XmlType(propOrder={"definition", "profiles"})
public class SnmpConfig extends Configuration implements Serializable {
    private static final long serialVersionUID = -5963402509661530467L;

    /**
     * Maps IP addresses to specific SNMP parameters (retries, timeouts...)
     */
    @JsonProperty("definition")
    @XmlElement(name="definition")
    private List<Definition> definition = new ArrayList<>();

    @JsonProperty("profiles")
    @XmlElement(name="profiles")
    @XmlJavaTypeAdapter(SnmpProfilesAdapter.class)
    private SnmpProfiles profiles = new SnmpProfiles();

    public SnmpConfig() {
        super();
    }

    public SnmpConfig(
            final Integer port,
            final Integer retry,
            final Integer timeout,
            final String readCommunity,
            final String writeCommunity,
            final String proxyHost,
            final String version,
            final Integer maxVarsPerPdu,
            final Integer maxRepetitions,
            final Integer maxRequestSize,
            final String securityName,
            final Integer securityLevel,
            final String authPassphrase,
            final String authProtocol,
            final String engineId,
            final String contextEngineId,
            final String contextName,
            final String privacyPassphrase,
            final String privacyProtocol,
            final String enterpriseId,
            final List<Definition> definitions) {
        super(port, retry, timeout, readCommunity, writeCommunity, proxyHost, version, maxVarsPerPdu, maxRepetitions, maxRequestSize,
              securityName, securityLevel, authPassphrase, authProtocol, engineId, contextEngineId, contextName, privacyPassphrase,
              privacyProtocol, enterpriseId);
        setDefinitions(definitions);
    }

    public SnmpConfig(Configuration baseConfig, List<Definition> definitions) {
        super(
            baseConfig.getPort(),
            baseConfig.getRetry(),
            baseConfig.getTimeout(),
            baseConfig.getReadCommunity(),
            baseConfig.getWriteCommunity(),
            baseConfig.getProxyHost(),
            baseConfig.getVersion(),
            baseConfig.getMaxVarsPerPdu(),
            baseConfig.getMaxRepetitions(),
            baseConfig.getMaxRequestSize(),
            baseConfig.getSecurityName(),
            baseConfig.getSecurityLevel(),
            baseConfig.getAuthPassphrase(),
            baseConfig.getAuthProtocol(),
            baseConfig.getEngineId(),
            baseConfig.getContextEngineId(),
            baseConfig.getContextName(),
            baseConfig.getPrivacyPassphrase(),
            baseConfig.getPrivacyProtocol(),
            baseConfig.getEnterpriseId()
        );

        setDefinitions(definitions);
    }

    @JsonIgnore
    public List<Definition> getDefinitions() {
        if (definition == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(definition);
        }
    }

    public void setDefinitions(final List<Definition> definitions) {
        if (definitions == null) {
            this.definition = new ArrayList<>();
        } else {
            this.definition = new ArrayList<Definition>(definitions);
        }
    }

    public void addDefinition(final Definition definitions) throws IndexOutOfBoundsException {
        if (definitions != null) {
            this.definition.add(definitions);
        }
    }

    public boolean removeDefinition(final Definition definitions) {
        if (definitions != null) {
            return this.definition.remove(definitions);
        }

        return false;
    }

    @JsonIgnore
    public SnmpProfiles getSnmpProfiles() {
        return profiles;
    }

    public void setSnmpProfiles(SnmpProfiles snmpProfiles) {
        this.profiles = Objects.requireNonNullElseGet(snmpProfiles, SnmpProfiles::new);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), definition, profiles);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof SnmpConfig)) {
            return false;
        }
        SnmpConfig other = (SnmpConfig) obj;
        return Objects.equals(definition, other.definition)
                && Objects.equals(profiles, other.profiles);
    }

    @Override
    public String toString() {
        return "SnmpConfig [" +
                "definition=" + definition +
                ", profiles=" + profiles +
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

    @Override
    public void fixSecurityLevel() {
        super.fixSecurityLevel();
        definition.forEach(Configuration::fixSecurityLevel);
        profiles.getSnmpProfiles().forEach(Configuration::fixSecurityLevel);
    }

    public void visit(SnmpConfigVisitor visitor) {
        visitor.visitSnmpConfig(this);
        for (final Definition definition : definition) {
            definition.visit(visitor);
        }
        visitor.visitSnmpConfigFinished();
    }

    public Definition findDefinition(final InetAddress agentInetAddress) {
        final AddressSnmpConfigVisitor visitor = new AddressSnmpConfigVisitor(agentInetAddress);
        visit(visitor);
        return visitor.getDefinition();
    }
}
