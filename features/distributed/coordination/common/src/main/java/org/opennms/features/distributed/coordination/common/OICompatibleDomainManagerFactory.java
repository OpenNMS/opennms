/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
