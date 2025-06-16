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

