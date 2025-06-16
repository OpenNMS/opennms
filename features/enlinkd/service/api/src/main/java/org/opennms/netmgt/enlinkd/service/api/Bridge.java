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
package org.opennms.netmgt.enlinkd.service.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Bridge implements Topology {

    private final Integer m_nodeId;
    private Integer m_rootPort;
    private boolean m_isRootBridge;
    private Set<String> m_identifiers = new HashSet<>();
    private String m_designated;

    public Bridge(Integer id) {
        super();
        m_nodeId = id;
    }

    public Integer getRootPort() {
        return m_rootPort;
    }

    public boolean isNewTopology() {
        if (m_rootPort != null) {
            return false;
        }
        return !m_isRootBridge;
    }
    public void setRootPort(Integer rootPort) {
        m_rootPort = rootPort;
        m_isRootBridge = false;
    }

    public boolean isRootBridge() {
        return m_isRootBridge;
    }

    public void setRootBridge() {
        m_isRootBridge = true;
        m_rootPort = null;
    }

    public Integer getNodeId() {
        return m_nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bridge bridge = (Bridge) o;
        return Objects.equals(m_nodeId, bridge.m_nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId);
    }

    @Override
    public String printTopology() {
    	StringBuilder strbfr = new StringBuilder();
        strbfr.append("bridge: nodeid[");
        strbfr.append(m_nodeId);
        strbfr.append("],");
        if (m_isRootBridge) {
            strbfr.append(" isRootBridge,");
        } else {
            strbfr.append(" designated port:[");
            strbfr.append(m_rootPort);
            strbfr.append("],");
        }
        strbfr.append(" designated:[");
        strbfr.append(m_designated);
        strbfr.append("], identifiers:");
        strbfr.append(m_identifiers);
        return strbfr.toString();
    }

    public Set<String> getIdentifiers() {
        return m_identifiers;
    }

    public void setIdentifiers(Set<String> identifiers) {
        m_identifiers = identifiers;
    }

    public String getDesignated() {
        return m_designated;
    }

    public void setDesignated(String designated) {
        m_designated = designated;
    }

    public void clear() {
        m_identifiers.clear();
        m_designated = null;
    }

}
