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

package org.opennms.features.distributed.coordination.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

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
        LOG.debug("Notifying all registrants of ", currentRole, " status");

        // Handlers must not block so that this happens in a timely manner
        for (Map.Entry<String, RoleChangeHandler> handler : roleChangeHandlers.entrySet()) {
            try {
                handler.getValue().becomeActive(getDomain());
            } catch (Exception e) {
                LOG.warn("Got exception while notifying handler '", handler.getKey(), "' of role '",
                        currentRole, "' ", e);
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
            LOG.debug("Notifying all registrants of ", currentRole, " status");

            // Handlers must not block so that this happens in a timely manner
            for (Map.Entry<String, RoleChangeHandler> handler : roleChangeHandlers.entrySet()) {
                try {
                    handler.getValue().becomeStandby(getDomain());
                } catch (Exception e) {
                    LOG.warn("Got exception while notifying handler '", handler.getKey(), "' of role '",
                            currentRole, "' ", e);
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
            LOG.debug("Joined election pool for domain '", getDomain(), "'");
            onFirstRegister();
        } else if (this.roleChangeHandlers.containsKey(id)) {
            throw new IllegalArgumentException(id + " is already registered");
        }

        LOG.debug("Adding '", id, "' to domain '", getDomain(), "'");
        this.roleChangeHandlers.put(id, Objects.requireNonNull(roleChangeHandler));

        // If we are already active we can go ahead and trigger the callback for the first time
        if (getCurrentRole() == Role.ACTIVE) {
            LOG.debug("Already ACTIVE, notifying registrant");
            roleChangeHandler.becomeActive(getDomain());
        }
    }

    @Override
    public final synchronized void register(String id, Consumer<String> onActive, Consumer<String> onStandby) {
        Objects.requireNonNull(onActive);
        Objects.requireNonNull(onStandby);

        register(id, new RoleChangeHandler() {
            @Override
            public void becomeActive(String domain) {
                onActive.accept(domain);
            }

            @Override
            public void becomeStandby(String domain) {
                onStandby.accept(domain);
            }
        });
    }

    @Override
    public final synchronized void deregister(String id) {
        if (!this.roleChangeHandlers.containsKey(Objects.requireNonNull(id))) {
            throw new IllegalArgumentException(id + " is not registered");
        }

        LOG.debug("Removing '", id, "' from domain '", getDomain(), "'");
        this.roleChangeHandlers.remove(id);

        if (!isAnythingRegistered()) {
            if (currentRole == Role.ACTIVE) {
                currentRole = Role.UNKNOWN;
                LOG.debug("Surrendered leadership for domain '", getDomain(), "'");
            }

            LOG.debug("Left election pool for domain '", getDomain(), "'");
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
