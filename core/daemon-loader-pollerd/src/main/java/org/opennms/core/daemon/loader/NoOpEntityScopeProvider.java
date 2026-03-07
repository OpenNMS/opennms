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
package org.opennms.core.daemon.loader;

import java.net.InetAddress;

import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Scope;

/**
 * No-op EntityScopeProvider for standalone daemon containers that don't need
 * MATE metadata interpolation (e.g. ${node:label}, ${scv:credentials:user}).
 */
public class NoOpEntityScopeProvider implements EntityScopeProvider {

    @Override
    public Scope getScopeForScv() {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForEnv() {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForNode(Integer nodeId) {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForInterface(Integer nodeId, String ipAddress) {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForInterfaceByIfIndex(Integer nodeId, int ifIndex) {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForService(Integer nodeId, InetAddress ipAddress, String serviceName) {
        return EmptyScope.EMPTY;
    }
}
