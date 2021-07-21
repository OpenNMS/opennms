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
 * At boot up, modules that register objects with TwinPublisher.
 * Modules also publish any subsequent updates to TwinPublisher.
 */

public interface TwinPublisher {

    /**
     * Session that can publish updates to T
     *
     * @param <T> type of object that needs replication.
     */
    interface Session<T> extends Closeable {
        /**
         * @param obj      an object that needs replication on Minion
         * @param location targeted Minion location for the object, set null for all locations.
         */
        void publish(T obj, String location) throws IOException;
    }

    /**
     * @param obj      an Object that needs replication.
     * @param key      unique key for the object.
     * @param location targeted Minion location for the object, set null for all locations.
     * @param <T>      type of object for replication
     * @return Session which provides updates to object.
     */
    <T> Session<T> register(T obj, String key, String location) throws IOException;
}
