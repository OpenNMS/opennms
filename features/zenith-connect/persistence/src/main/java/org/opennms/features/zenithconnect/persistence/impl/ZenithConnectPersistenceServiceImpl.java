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
package org.opennms.features.zenithconnect.persistence.impl;

import com.google.common.base.Strings;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistration;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistrations;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceException;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZenithConnectPersistenceServiceImpl implements ZenithConnectPersistenceService {
    private static final Logger LOG = LoggerFactory.getLogger(ZenithConnectPersistenceServiceImpl.class);

    private static final String ZENITH_CONNECT_CONTEXT = "ZENITH_CONNECT";
    private static final String ZENITH_CONNECT_REGISTRATIONS_KEY = "registrations";

    private final JsonStore jsonStore;

    public ZenithConnectPersistenceServiceImpl(JsonStore jsonStore) {
        this.jsonStore = jsonStore;
    }

    /** {@inheritDoc} */
    public ZenithConnectRegistrations getRegistrations() throws ZenithConnectPersistenceException {
        return getRegistrationsImpl();
    }

    /** {@inheritDoc} */
    public ZenithConnectRegistration addRegistration(ZenithConnectRegistration registration, boolean preventDuplicates)
            throws ZenithConnectPersistenceException {
        if (preventDuplicates && isDuplicateRegistration(registration)) {
           throw new ZenithConnectPersistenceException(true);
        }

        registration.id = UUID.randomUUID().toString();
        registration.createTimeMs = Instant.now().toEpochMilli();

        var registrations = new ZenithConnectRegistrations(registration);
        setRegistrations(registrations);

        return registration;
    }

    public ZenithConnectRegistration addRegistration(ZenithConnectRegistration registration)
            throws ZenithConnectPersistenceException {
        return addRegistration(registration, false);
    }

    /** {@inheritDoc} */
    public void updateRegistration(String id, ZenithConnectRegistration registration) throws ZenithConnectPersistenceException {
        if (Strings.isNullOrEmpty(id) ||
          Strings.isNullOrEmpty(registration.id) ||
          !id.equals(registration.id)) {
            throw new ZenithConnectPersistenceException("Error updating registration, must include valid id.");
        }

        var existingRegistrations = this.getRegistrationsImpl();

        var existingRegistration = existingRegistrations.getRegistrations().stream()
                .filter(r -> r.id.equals(id))
                .findFirst().orElse(null);

        if (existingRegistration == null) {
            throw new ZenithConnectPersistenceException("Error updating registration, must update existing item.");
        }

        // retain the original created time
        registration.createTimeMs = existingRegistration.createTimeMs;

        var newRegistrations = new ZenithConnectRegistrations(registration);
        setRegistrations(newRegistrations);
    }

    /** {@inheritDoc} */
    public void deleteRegistration(String id) throws ZenithConnectPersistenceException {
        if (Strings.isNullOrEmpty(id)) {
            throw new ZenithConnectPersistenceException("Delete Registration: Invalid or empty id in request.");
        }

        var registrations = getRegistrationsImpl();

        if (!registrations.getRegistrations().stream().anyMatch(r -> r.id != null && r.id.equals(id))) {
            throw new ZenithConnectPersistenceException("Delete Registration: Invalid id or id not found.");
        }

        setRegistrations(new ZenithConnectRegistrations());
    }

    private ZenithConnectRegistrations getRegistrationsImpl() throws ZenithConnectPersistenceException {
        String json = jsonStore.get(ZENITH_CONNECT_REGISTRATIONS_KEY, ZENITH_CONNECT_CONTEXT).orElse(null);

        if (json != null) {
            try {
                var objectMapper = new ObjectMapper();
                ZenithConnectRegistrations registrations = objectMapper.readValue(json, ZenithConnectRegistrations.class);

                return registrations;
            } catch (Exception e) {
                throw new ZenithConnectPersistenceException("Error deserializing value from json store", e);
            }
        }

        return new ZenithConnectRegistrations();
    }

    private void setRegistrations(ZenithConnectRegistrations registrations) throws ZenithConnectPersistenceException {
        try {
            var objectMapper = new ObjectMapper();
            String registrationsJson = objectMapper.writeValueAsString(registrations);

            jsonStore.put(ZENITH_CONNECT_REGISTRATIONS_KEY, registrationsJson, ZENITH_CONNECT_CONTEXT);
        } catch (JsonProcessingException e) {
            throw new ZenithConnectPersistenceException("Could not serialize Zenith Connect registrations", e);
        }
    }

    private boolean isDuplicateRegistration(ZenithConnectRegistration registration)
            throws ZenithConnectPersistenceException {
        ZenithConnectRegistrations existingRegistrations = getRegistrationsImpl();
        ZenithConnectRegistration existingRegistration = existingRegistrations.first();

        if (existingRegistration != null) {
            boolean systemIdsMatch = registration.systemId != null && existingRegistration.systemId != null &&
                    registration.systemId.equals(existingRegistration.systemId);

            if (systemIdsMatch) {
                boolean accessTokenMatches = registration.accessToken != null &&
                        existingRegistration.accessToken != null &&
                        registration.accessToken.equals(existingRegistration.accessToken);

                boolean refreshTokenMatches = registration.refreshToken != null &&
                        existingRegistration.refreshToken != null &&
                        registration.refreshToken.equals(existingRegistration.refreshToken);

                if (accessTokenMatches || refreshTokenMatches) {
                    return true;
                }
            }
        }

        return false;
    }
}
