/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.api;

import java.io.Closeable;
import java.io.IOException;

/**
 * TwinPublisher lives on OpenNMS that handles all the Objects that need to be replicated.
 * At boot up, modules register module specific key with TwinPublisher.
 * Modules publish initial objects/updated objects on the session.
 */

public interface TwinPublisher extends Closeable {

    /**
     * Session that can publish initial objects and updates to T.
     *
     * @param <T> type of object that is getting replicated.
     */
    interface Session<T> extends Closeable {
        /**
         * @param obj an object that needs replication on Minion
         */
        void publish(T obj) throws IOException;
    }

    /**
     * @param <T>      type of object for replication
     * @param key      unique key for the object.
     * @param clazz    a class used for serialization.
     * @param location targeted Minion location for the object, set null for all locations.
     * @return Session which provides updates to object.
     */
    <T> Session<T> register(String key, Class<T> clazz, String location) throws IOException;

    default <T> Session<T> register(String key, Class<T> clazz) throws IOException {
        return register(key, clazz, null);
    }
}

