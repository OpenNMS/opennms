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

/**
 * A no-op implementation of {@link EntityScopeProvider} that returns {@link EmptyScope#EMPTY}
 * for all scope requests. This is used when metadata interpolation is disabled via the
 * {@code org.opennms.metadata.interpolation.enabled} system property.
 */
public final class NoOpEntityScopeProvider implements EntityScopeProvider {

    public static final NoOpEntityScopeProvider INSTANCE = new NoOpEntityScopeProvider();

    private NoOpEntityScopeProvider() {
    }

    @Override
    public Scope getScopeForScv() {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForEnv() {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForNode(final Integer nodeId) {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForInterface(final Integer nodeId, final String ipAddress) {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForInterfaceByIfIndex(final Integer nodeId, final int ifIndex) {
        return EmptyScope.EMPTY;
    }

    @Override
    public Scope getScopeForService(final Integer nodeId, final InetAddress ipAddress, final String serviceName) {
        return EmptyScope.EMPTY;
    }
}
