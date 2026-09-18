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
package org.opennms.netmgt.wsman.eventlog;

import java.net.InetAddress;
import java.util.Objects;

/** One node to read: the interface that carries the WS-Man service and the node's location. */
public class EventLogTarget {

    private final int nodeId;
    private final String nodeLabel;
    private final InetAddress address;
    private final String location;

    public EventLogTarget(int nodeId, String nodeLabel, InetAddress address, String location) {
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
        this.address = Objects.requireNonNull(address);
        this.location = location;
    }

    public int getNodeId() {
        return nodeId;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, address);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EventLogTarget)) {
            return false;
        }
        final EventLogTarget other = (EventLogTarget) obj;
        return nodeId == other.nodeId && address.equals(other.address);
    }

    @Override
    public String toString() {
        return nodeLabel + " (" + nodeId + ") " + address.getHostAddress() + "@" + location;
    }
}
