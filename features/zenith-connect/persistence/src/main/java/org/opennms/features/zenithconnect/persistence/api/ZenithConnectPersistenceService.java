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
package org.opennms.features.zenithconnect.persistence.api;

import java.util.List;

public interface ZenithConnectPersistenceService {
    /**
     * Get an object representing a list of registrations.
     * For now, there will only ever be a single registration.
     */
    ZenithConnectRegistrations getRegistrations() throws ZenithConnectPersistenceException;

    /**
     * Add a new registration.
     * Currently we only support a single registration, so this will replace any registration that
     * already exists.
     * Returns the added registration, including an id and createTimeMs.
     * @param preventDuplicates If true, will check to see if the given registration appears to be
     *     a duplicate of an existing registration (same systemId and same accessToken or refreshToken).
     *     If so, will throw an exception. This is to prevent e.g. the UI from sending multiple
     *     duplicate add requests.
     */
    ZenithConnectRegistration addRegistration(ZenithConnectRegistration registration, boolean preventDuplicates)
            throws ZenithConnectPersistenceException;

    /**
     * Add a new registration, ignoring preventDuplicates.
     */
    ZenithConnectRegistration addRegistration(ZenithConnectRegistration registration)
            throws ZenithConnectPersistenceException;

    /**
     * Update an existing registration. The given id and registration.id must match an existing registration.
     */
    void updateRegistration(String id, ZenithConnectRegistration registration) throws ZenithConnectPersistenceException;

    /**
     * Delete an existing registration.
     */
    void deleteRegistration(String id) throws ZenithConnectPersistenceException;
}
