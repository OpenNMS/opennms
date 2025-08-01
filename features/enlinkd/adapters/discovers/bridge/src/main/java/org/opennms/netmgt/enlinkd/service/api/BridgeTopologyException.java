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

public class BridgeTopologyException extends Exception implements Topology {

    private static final long serialVersionUID = -6913989384724814658L;

    private Topology m_topology;

    public BridgeTopologyException(String message) {
        super(message);
    }

    public BridgeTopologyException(String message,Throwable throwable) {
        super(message, throwable);
    }

    public BridgeTopologyException(String message, Topology topology) {
        super(message);
        m_topology=topology;
    }

    public BridgeTopologyException(String message,Topology topology, Throwable throwable) {
        super(message, throwable);
        m_topology=topology;
    }

    @Override
    public String printTopology() {
        if (m_topology == null) {
            return "no topology associated to this";
        }
        return m_topology.printTopology();
    }

}
