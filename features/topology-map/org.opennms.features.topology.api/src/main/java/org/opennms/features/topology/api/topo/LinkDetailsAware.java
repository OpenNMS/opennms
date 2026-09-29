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
package org.opennms.features.topology.api.topo;

/**
 * An edge that knows the network link behind it. Enlinkd resolves both ends
 * while building the topology; this exposes them to consumers outside the
 * Vaadin plugins.
 */
public interface LinkDetailsAware {

    /** Null if discovery never resolved this end. */
    Integer getSourceIfIndex();

    /** Null if discovery never resolved this end. */
    Integer getTargetIfIndex();

    /**
     * The enlinkd {@code ProtocolSupported} name. A combined view holds several
     * protocols in one namespace, so the namespace does not identify an edge's
     * discovery protocol. Includes the synthetic topologies, whose ends have no
     * ifIndex.
     */
    String getDiscoveryProtocol();
}
