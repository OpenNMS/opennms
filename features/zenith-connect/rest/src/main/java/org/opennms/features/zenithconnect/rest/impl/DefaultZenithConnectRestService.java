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
package org.opennms.features.zenithconnect.rest.impl;

import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceException;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceService;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistration;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistrations;
import org.opennms.features.zenithconnect.rest.api.ZenithConnectRestService;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class DefaultZenithConnectRestService implements ZenithConnectRestService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultZenithConnectRestService.class);
    private final ZenithConnectPersistenceService persistenceService;

    public DefaultZenithConnectRestService(ZenithConnectPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /**
     * Get all registrations.
     * Currently, there is only one registration at a time, so this will return a
     * ZenithConnectRegistrations object with a single object in the registrations list.
     */
    @Override
    public Response getRegistrations() {
        try {
            ZenithConnectRegistrations registrations = persistenceService.getRegistrations();
            return Response.ok(registrations).build();
        } catch (ZenithConnectPersistenceException e) {
            LOG.error("Could not get registrations, error retrieving or parsing registrations: {}.", e.getMessage(), e);
            return Response.serverError().build();
        }
    }

    /**
     * Add the given registration.
     * This will throw a 400 Bad Request if the request is a duplicate of an existing registration.
     * Throws a 500 Server Error if the request is malformed, or there was an error updating the database.
     */
    @Override
    public Response addRegistration(ZenithConnectRegistration registration) {
        try {
            var newRegistration = persistenceService.addRegistration(registration, true);
            return Response.status(Response.Status.CREATED).entity(newRegistration).build();
        } catch (ZenithConnectPersistenceException e) {
            LOG.error("Could not add registration: {}. Attempted to add duplicate: {}",
                    e.getMessage(), e.isAttemptedToAddDuplicate(), e);

            if (e.isAttemptedToAddDuplicate()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            return Response.serverError().build();
        }
    }

    /**
     * Update an existing registration. The id and registration.id must match an existing registration.
     */
    @Override
    public Response updateRegistration(String id, ZenithConnectRegistration registration) {
        try {
            persistenceService.updateRegistration(id, registration);
            return Response.ok().build();
        } catch (ZenithConnectPersistenceException e) {
            LOG.error("Could not update registration: {}.", e.getMessage(), e);
            return Response.serverError().build();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Response deleteRegistration(String id) {
        try {
            persistenceService.deleteRegistration(id);
            return Response.ok().build();
        } catch (ZenithConnectPersistenceException e) {
            LOG.error("Could not delete registration: {}.", e.getMessage(), e);
            return Response.serverError().build();
        }
    }
}

