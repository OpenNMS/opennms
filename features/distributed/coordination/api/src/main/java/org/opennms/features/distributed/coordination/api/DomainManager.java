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
package org.opennms.features.distributed.coordination.api;

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
