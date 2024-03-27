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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Node {

    private final int m_nodeId;

    private final InetAddress m_snmpprimaryaddr;

    private final String m_sysoid;

    private final String m_sysname;

    private final String m_label;

    private final String m_location;

    public String getLocation() {
        return m_location;
    }

    public Node(final int nodeId, final String label,
            final InetAddress snmpPrimaryAddr, final String sysoid, final String sysname, final String location) {
        m_nodeId = nodeId;
        m_label=label;
        m_snmpprimaryaddr = snmpPrimaryAddr;
        m_sysoid = sysoid;
        m_sysname = sysname;
        m_location = location;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nodeId", m_nodeId)
                .append("snmpPrimaryAddr", str(m_snmpprimaryaddr))
                .append("sysOid", m_sysoid).toString();
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public InetAddress getSnmpPrimaryIpAddr() {
        return m_snmpprimaryaddr;
    }

    public String getSysoid() {
        return m_sysoid;
    }

    public String getSysname() {
        return m_sysname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(m_nodeId, node.m_nodeId) &&
                Objects.equals(m_snmpprimaryaddr, node.m_snmpprimaryaddr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId, m_snmpprimaryaddr);
    }

    public String getLabel() {
        return m_label;
    }

}