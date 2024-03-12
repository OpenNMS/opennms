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
package org.opennms.core.mate.api;

import java.net.InetAddress;

public interface EntityScopeProvider {

    interface Contexts {
        String ASSET = "asset";
        String INTERFACE = "interface";
        String NODE = "node";
        String SERVICE = "service";
    }

    Scope getScopeForScv();

    Scope getScopeForNode(final Integer nodeId);

    Scope getScopeForInterface(final Integer nodeId, final String ipAddress);

    Scope getScopeForInterfaceByIfIndex(final Integer nodeId, final int ifIndex);

    Scope getScopeForService(final Integer nodeId, final InetAddress ipAddress, final String serviceName);

    default ScopeProvider getScopeProviderForScv() {
        return () -> getScopeForScv();
    }

    default ScopeProvider getScopeProviderForNode(final Integer nodeId) {
        return () -> getScopeForNode(nodeId);
    }

    default ScopeProvider getScopeProviderForInterface(final Integer nodeId, final String ipAddress) {
        return () -> getScopeForInterface(nodeId, ipAddress);
    }

    default ScopeProvider getScopeProviderForInterfaceByIfIndex(final Integer nodeId, final int ifIndex) {
        return () -> getScopeForInterfaceByIfIndex(nodeId, ifIndex);
    }

    default ScopeProvider getScopeProviderForService(final Integer nodeId, final InetAddress ipAddress, final String serviceName) {
        return () -> getScopeForService(nodeId, ipAddress, serviceName);
    }
}
