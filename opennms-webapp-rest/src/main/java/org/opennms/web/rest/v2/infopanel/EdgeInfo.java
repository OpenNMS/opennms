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
package org.opennms.web.rest.v2.infopanel;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

/**
 * The {@code edge} variable for edge-scoped info-panel templates: the
 * Vaadin-free counterpart of the legacy {@code LinkdEdge} the old map put
 * into the template context. The legacy surface templates used --
 * {@code edge.discoveredBy}, {@code edge.sourcePort.ifIndex},
 * {@code edge.sourcePort.vertex} (the node) -- maps onto this shape as
 * {@code edge.discoveredBy}, {@code edge.sourcePort.ifIndex} and
 * {@code edge.sourcePort.node}.
 *
 * <p>Each side also exposes the resolved {@link OnmsSnmpInterface} (when the
 * port name matches one), which is the identity that future link status /
 * metrics work keys on as well -- extend here, not in a parallel model.
 */
public class EdgeInfo {

    /** One endpoint of the edge: a node plus (optionally) a resolved port. */
    public static class Port {
        private final OnmsNode node;
        private final String ifName;
        private final OnmsSnmpInterface snmpInterface;

        public Port(final OnmsNode node, final String ifName, final OnmsSnmpInterface snmpInterface) {
            this.node = node;
            this.ifName = ifName;
            this.snmpInterface = snmpInterface;
        }

        public OnmsNode getNode() {
            return node;
        }

        /** The port label persisted with the link (typically the ifName). */
        public String getIfName() {
            return ifName;
        }

        /** Resolved SNMP interface for the port name, or null when unmatched. */
        public OnmsSnmpInterface getSnmpInterface() {
            return snmpInterface;
        }

        /** Legacy-parity convenience ({@code LinkdPort.getIfIndex()}). */
        public Integer getIfIndex() {
            return snmpInterface != null ? snmpInterface.getIfIndex() : null;
        }
    }

    private final String discoveredBy;
    private final Port sourcePort;
    private final Port targetPort;

    public EdgeInfo(final String discoveredBy, final Port sourcePort, final Port targetPort) {
        this.discoveredBy = discoveredBy;
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
    }

    /** Discovery protocol (lldp, cdp, ospf, isis, bridge), or null for drawn links. */
    public String getDiscoveredBy() {
        return discoveredBy;
    }

    public Port getSourcePort() {
        return sourcePort;
    }

    public Port getTargetPort() {
        return targetPort;
    }
}
