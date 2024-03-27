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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.Role;
import org.opennms.features.distributed.coordination.api.RoleChangeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A skeleton implementation of {@link DomainManager}.
 */
public abstract class AbstractDomainManager implements DomainManager {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDomainManager.class);

    /**
     * The current role.
     */
    private Role currentRole = Role.UNKNOWN;

    /**
     * The domain being managed.
     */
    private final String domain;

    /**
     * The map containing all the registrants (their Id and handlers).
     */
    private final Map<String, RoleChangeHandler> roleChangeHandlers = new HashMap<>();

    /**
     * Constructor.
     *
     * @param domain the domain to manage
     */
    protected AbstractDomainManager(String domain) {
        this.domain = Objects.requireNonNull(domain);
    }

    /**
     * Check the current view of the registered change handlers.
     *
     * @return an immutable copy of the currently registered change handlers
     */
    public synchronized final Map<String, RoleChangeHandler> getRoleChangeHandlers() {
        return Collections.unmodifiableMap(new HashMap<>(roleChangeHandlers));
    }

    /**
     * Executed when becoming ACTIVE for the managed domain. This blocks until all registrants have processed the
     * becomeActive() in their handler.
     */
    protected synchronized final void becomeActive() {
        currentRole = Role.ACTIVE;
        LOG.debug("Notifying all registrants of {} role", currentRole);

        // Handlers must not block so that this happens in a timely manner
        for (Map.Entry<String, RoleChangeHandler> handler : roleChangeHandlers.entrySet()) {
            try {
                handler.getValue().handleRoleChange(Role.ACTIVE, getDomain());
            } catch (Exception e) {
                LOG.warn("Got exception while notifying handler {} of role {}", handler.getKey(), currentRole, e);
            }
        }
    }

    /**
     * Executed when becoming STANDBY for the managed domain. This blocks until all registrants have processed the
     * becomeStandby() in their handler.
     */
    protected synchronized final void becomeStandby() {
        if (currentRole != Role.STANDBY) {
            currentRole = Role.STANDBY;
            LOG.debug("Notifying all registrants of {} role", currentRole);

            // Handlers must not block so that this happens in a timely manner
            for (Map.Entry<String, RoleChangeHandler> handler : roleChangeHandlers.entrySet()) {
                try {
                    handler.getValue().handleRoleChange(Role.STANDBY, getDomain());
                } catch (Exception e) {
                    LOG.warn("Got exception while notifying handler {} of role {}", handler.getKey(), currentRole, e);
                }
            }
        }
    }

    protected synchronized final Role getCurrentRole() {
        return currentRole;
    }

    protected final String getDomain() {
        return domain;
    }

    /**
     * Implementations must handle any specific logic for dealing with a registration that adds to the map of handlers
     * for the first time (such as connecting).
     */
    protected abstract void onFirstRegister();

    /**
     * Implementations must handle any specific logic for dealing with a deregistration that empties the map of handlers
     * (such as disconnecting).
     */
    protected abstract void onLastDeregister();

    @Override
    public final synchronized void register(String id, RoleChangeHandler roleChangeHandler) {
        Objects.requireNonNull(id);

        if (!isAnythingRegistered()) {
            LOG.debug("Joined election pool for domain {}", getDomain());
            onFirstRegister();
        } else if (this.roleChangeHandlers.containsKey(id)) {
            throw new IllegalArgumentException(id + " is already registered");
        }

        LOG.debug("Adding {} to domain {}", id, getDomain());
        this.roleChangeHandlers.put(id, Objects.requireNonNull(roleChangeHandler));

        // If we are already active we can go ahead and trigger the callback for the first time
        if (getCurrentRole() == Role.ACTIVE) {
            LOG.debug("Already ACTIVE, notifying registrant");
            roleChangeHandler.handleRoleChange(Role.ACTIVE, getDomain());
        }
    }

    @Override
    public final synchronized void deregister(String id) {
        if (!this.roleChangeHandlers.containsKey(Objects.requireNonNull(id))) {
            throw new NoSuchElementException(id + " is not registered");
        }

        LOG.debug("Removing {} from domain {}", id, getDomain());
        this.roleChangeHandlers.remove(id);

        if (!isAnythingRegistered()) {
            if (currentRole == Role.ACTIVE) {
                currentRole = Role.UNKNOWN;
                LOG.debug("Surrendered leadership for domain {}", getDomain());
            }

            LOG.debug("Left election pool for domain {}", getDomain());
            onLastDeregister();
        }
    }

    @Override
    public final synchronized boolean isRegistered(String id) {
        return roleChangeHandlers.containsKey(id);
    }

    @Override
    public final synchronized boolean isAnythingRegistered() {
        return !roleChangeHandlers.isEmpty();
    }

    @Override
    public String toString() {
        return "org.opennms.distributed.coordination.api.AbstractDomainManager{" +
                "currentRole=" + currentRole +
                ", domain=" + domain +
                ", roleChangeHandlers=" + roleChangeHandlers +
                '}';
    }
}
