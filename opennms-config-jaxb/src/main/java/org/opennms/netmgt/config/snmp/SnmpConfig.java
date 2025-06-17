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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the snmp-config.xml configuration file.
 */

@XmlRootElement(name="snmp-config")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("snmp-config.xsd")
public class SnmpConfig extends Configuration implements Serializable {
    private static final long serialVersionUID = -5963402509661530467L;

    /**
     * Maps IP addresses to specific SNMP parameters (retries, timeouts...)
     */
    @XmlElement(name="definition")
    private List<Definition> m_definitions = new ArrayList<>();

    @XmlElement(name="profiles")
    private SnmpProfiles m_snmpProfiles;

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

    public List<Definition> getDefinitions() {
        if (m_definitions == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_definitions);
        }
    }

    public void setDefinitions(final List<Definition> definitions) {
        m_definitions = new ArrayList<Definition>(definitions);
    }

    public void addDefinition(final Definition definitions) throws IndexOutOfBoundsException {
        this.m_definitions.add(definitions);
    }

    public boolean removeDefinition(final Definition definitions) {
        return m_definitions.remove(definitions);
    }

    public SnmpProfiles getSnmpProfiles() {
        return m_snmpProfiles;
    }

    public void setSnmpProfiles(SnmpProfiles snmpProfiles) {
        this.m_snmpProfiles = snmpProfiles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((m_definitions == null) ? 0 : m_definitions.hashCode());
        return result;
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
        if (m_definitions == null) {
            if (other.m_definitions != null) {
                return false;
            }
        } else if (!m_definitions.equals(other.m_definitions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SnmpConfig [definitions=" + m_definitions + "]";
    }

    public void visit(SnmpConfigVisitor visitor) {
        visitor.visitSnmpConfig(this);
        for (final Definition definition : m_definitions) {
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
