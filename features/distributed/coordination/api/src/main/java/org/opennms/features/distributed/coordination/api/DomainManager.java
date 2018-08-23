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

package org.opennms.features.distributed.coordination.api;

import java.util.function.Consumer;

/**
 * A manager that is responsible for registering and deregistering clients for a given domain.
 */
public interface DomainManager {
    /**
     * Register with the domain being managed. This is a non-blocking call.
     * <p>
     * Ids must be unique to this manager. Attempting to register the same Id twice will result in an exception.
     * <p>
     * The methods specified by the {@link RoleChangeHandler} passed to this method must not block.
     *
     * @param id                the Id to register
     * @param roleChangeHandler the role change handler to register
     */
    void register(String id, RoleChangeHandler roleChangeHandler);

    /**
     * Register with the domain being managed. This is a non-blocking call.
     * <p>
     * Ids must be unique to this manager. Attempting to register the same Id twice will result in an exception.
     * <p>
     * The consumers passed to this register method must not block.
     *
     * @param id        the Id to register
     * @param onActive  the callback to handle becoming active which must not block
     * @param onStandby the callback to handle becoming standby which must not block
     */
    void register(String id, Consumer<String> onActive, Consumer<String> onStandby);

    /**
     * Deregister with the domain being managed. This is a non-blocking call.
     *
     * @param id the Id to register
     */
    void deregister(String id);

    /**
     * Checks if a given Id is registered.
     *
     * @param id the Id to check
     * @return true if registered, false otherwise
     */
    boolean isRegistered(String id);

    /**
     * Checks if anything is currently registered with the domain being managed.
     *
     * @return true if one or more registrants are currently registered, false otherwise
     */
    boolean isAnythingRegistered();
}
