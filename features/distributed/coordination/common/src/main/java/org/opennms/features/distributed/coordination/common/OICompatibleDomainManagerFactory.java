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
package org.opennms.features.distributed.coordination.common;

import java.util.Objects;

import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;
import org.opennms.features.distributed.coordination.api.Role;
import org.opennms.features.distributed.coordination.api.RoleChangeHandler;


public class OICompatibleDomainManagerFactory implements org.opennms.integration.api.v1.coordination.DomainManagerFactory {

    private final DomainManagerFactory delegate;

    public OICompatibleDomainManagerFactory(DomainManagerFactory delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public org.opennms.integration.api.v1.coordination.DomainManager getManagerForDomain(String domain) {
        final DomainManager domainManager = delegate.getManagerForDomain(domain);
        if (domainManager == null) {
            return null;
        }
        return new org.opennms.integration.api.v1.coordination.DomainManager() {
            @Override
            public void register(String id, org.opennms.integration.api.v1.coordination.RoleChangeHandler roleChangeHandler) {
                domainManager.register(id, new RoleChangeHandler() {
                    @Override
                    public void handleRoleChange(Role role, String domain) {
                        roleChangeHandler.handleRoleChange(toRole(role), domain);
                    }
                });
            }

            @Override
            public void deregister(String id) {
                domainManager.deregister(id);
            }

            @Override
            public boolean isRegistered(String id) {
                return domainManager.isRegistered(id);
            }

            @Override
            public boolean isAnythingRegistered() {
                return domainManager.isAnythingRegistered();
            }
        };
    }

    private static org.opennms.integration.api.v1.coordination.Role toRole(Role role) {
        if (role == null) {
            return null;
        }
        switch (role) {
            case ACTIVE:
                return org.opennms.integration.api.v1.coordination.Role.ACTIVE;
            case STANDBY:
                return org.opennms.integration.api.v1.coordination.Role.STANDBY;
        }
        return org.opennms.integration.api.v1.coordination.Role.UNKNOWN;
    }
}
