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

import java.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceException;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceService;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistration;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistrations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-zenithconnect-persistence.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class ZenithConnectPersistenceServiceImplIT {
    private static final String DEFAULT_SYSTEM_ID = "12345";
    private static final String DEFAULT_DISPLAY_NAME = "Test System";
    private static final String DEFAULT_ZENITH_HOST = "https://zenith.opennms.com";
    private static final String DEFAULT_ZENITH_RELATIVE_URL = "/zenith-connect";
    private static final String DEFAULT_ZENITH_ACCESS_TOKEN = "default-access-token";
    private static final String DEFAULT_ZENITH_REFRESH_TOKEN = "default-refresh-token";
    private static final String UUID_REGEX = "^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$";

    @Autowired
    private ZenithConnectPersistenceService persistenceService;

    @Test
    public void testAddAndGetRegistrations() throws ZenithConnectPersistenceException {
        // add a registration
        var registration = createDefaultRegistration();
        long currentTime = Instant.now().toEpochMilli();
        persistenceService.addRegistration(registration);

        // make sure it was added correctly
        ZenithConnectRegistrations registrations = persistenceService.getRegistrations();
        assertNotNull(registrations);

        ZenithConnectRegistration createdRegistration = registrations.first();
        assertRegistrationFieldsEqual(registration, createdRegistration);

        assertThat(createdRegistration.id, not(emptyString()));
        assertThat(createdRegistration.id, matchesPattern(UUID_REGEX));
        assertThat(createdRegistration.createTimeMs, greaterThanOrEqualTo(currentTime));
    }

    @Test
    public void testUpdateRegistration() throws ZenithConnectPersistenceException {
        // add a registration
        var registration = createDefaultRegistration();
        long currentTime = Instant.now().toEpochMilli();
        persistenceService.addRegistration(registration);

        // make sure it was added correctly
        ZenithConnectRegistrations registrations = persistenceService.getRegistrations();
        assertNotNull(registrations);

        ZenithConnectRegistration createdRegistration = registrations.first();
        assertRegistrationFieldsEqual(registration, createdRegistration);

        assertThat(createdRegistration.id, not(emptyString()));
        assertThat(createdRegistration.id, matchesPattern(UUID_REGEX));
        assertThat(createdRegistration.createTimeMs, greaterThanOrEqualTo(currentTime));

        // update the registration
        var updatingRegistration = createDefaultRegistration();
        updatingRegistration.id = createdRegistration.id;
        updatingRegistration.systemId = "23456";
        updatingRegistration.displayName = "Updated Registration";
        updatingRegistration.zenithHost = "https://updated.opennms.com";
        updatingRegistration.zenithRelativeUrl = "/zenith-connect-updated";
        updatingRegistration.accessToken = "updated-access-token";
        updatingRegistration.refreshToken = "updated-refresh-token";
        updatingRegistration.registered = true;
        updatingRegistration.active = true;

        persistenceService.updateRegistration(updatingRegistration.id, updatingRegistration);

        // make sure it updated correctly
        ZenithConnectRegistrations updatedRegistrations = persistenceService.getRegistrations();
        assertNotNull(updatedRegistrations);

        ZenithConnectRegistration updatedRegistration = updatedRegistrations.first();
        assertRegistrationFieldsEqual(updatedRegistration, updatingRegistration);

        assertThat(updatedRegistration.id, not(emptyString()));
        assertThat(updatedRegistration.id, matchesPattern(UUID_REGEX));
        assertEquals(createdRegistration.id, updatedRegistration.id);
        assertEquals(createdRegistration.createTimeMs, updatedRegistration.createTimeMs);
    }

    @Test
    public void testDeleteRegistration() throws ZenithConnectPersistenceException {
        // add a registration
        var registration = createDefaultRegistration();
        long currentTime = Instant.now().toEpochMilli();
        persistenceService.addRegistration(registration);

        // make sure it was added correctly
        ZenithConnectRegistrations registrations = persistenceService.getRegistrations();
        assertNotNull(registrations);

        ZenithConnectRegistration createdRegistration = registrations.first();
        assertRegistrationFieldsEqual(registration, createdRegistration);

        assertThat(createdRegistration.id, not(emptyString()));
        assertThat(createdRegistration.id, matchesPattern(UUID_REGEX));
        assertThat(createdRegistration.createTimeMs, greaterThanOrEqualTo(currentTime));

        // delete it
        persistenceService.deleteRegistration(createdRegistration.id);

        // make sure it was deleted correctly
        registrations = persistenceService.getRegistrations();
        assertNotNull(registrations);

        var list = registrations.getRegistrations();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    private ZenithConnectRegistration createDefaultRegistration() {
        var registration = new ZenithConnectRegistration();

        registration.systemId = DEFAULT_SYSTEM_ID;
        registration.displayName = DEFAULT_DISPLAY_NAME;
        registration.zenithHost = DEFAULT_ZENITH_HOST;
        registration.zenithRelativeUrl = DEFAULT_ZENITH_RELATIVE_URL;
        registration.accessToken = DEFAULT_ZENITH_ACCESS_TOKEN;
        registration.refreshToken = DEFAULT_ZENITH_REFRESH_TOKEN;
        registration.registered = false;
        registration.active = false;

        return registration;
    }

    /**
     * Does not check id or createTimeMs as these may not be equal.
     */
    private void assertRegistrationFieldsEqual(ZenithConnectRegistration expected, ZenithConnectRegistration actual) {
        assertEquals(expected.systemId, actual.systemId);
        assertEquals(expected.displayName, actual.displayName);
        assertEquals(expected.zenithHost, actual.zenithHost);
        assertEquals(expected.zenithRelativeUrl, actual.zenithRelativeUrl);
        assertEquals(expected.accessToken, actual.accessToken);
        assertEquals(expected.refreshToken, actual.refreshToken);
        assertEquals(expected.registered, actual.registered);
        assertEquals(expected.active, actual.active);
    }
}
