/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.kvstore.json.postgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.OptionalLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass = MockDatabase.class)
public class PostgresJsonStoreIT {
    @Autowired
    private JsonStore postgresJsonStore;

    @Test
    public void canPersistAndRetrieve() {
        String key = "key";
        String context = "value";
        String value = "{\"hello\": \"world\"}";

        // Shouldn't exist yet
        assertThat(postgresJsonStore.get(key, context), equalTo(Optional.empty()));

        // Write the value
        postgresJsonStore.put(key, value, context);

        // Get the value since it should exist now
        String retrievedValue = postgresJsonStore.get(key, context).get();
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    public void canGetOnlyIfNotStale() {
        String key = "key2";
        String context = "value2";
        String value = "{\"hello\": \"world\"}";

        // Shouldn't exist yet
        assertThat(postgresJsonStore.getIfStale(key, context, 0), equalTo(Optional.empty()));

        // Write the value
        long insertedAt = postgresJsonStore.put(key, value, context);

        // Attempt to get the value with a non-stale timestamp
        Optional<Optional<String>> retrievedValue = postgresJsonStore.getIfStale(key, context, insertedAt + 1);
        assertThat(retrievedValue, equalTo(Optional.of(Optional.empty())));

        // Attempt to get the value with a stale timestamp
        retrievedValue = postgresJsonStore.getIfStale(key, context, insertedAt - 1);
        assertThat(retrievedValue, equalTo(Optional.of(Optional.of(value))));
    }

    @Test
    public void doesNotGetExpiredRecords() throws InterruptedException {
        String key = "key3";
        String context = "value3";
        String value = "{\"hello\": \"world\"}";

        // Shouldn't exist yet
        assertThat(postgresJsonStore.getIfStale(key, context, 0), equalTo(Optional.empty()));

        // Write the value
        long insertedAt = postgresJsonStore.put(key, value, context, 1);

        // We should see it right now
        assertThat(postgresJsonStore.get(key, context).get(), equalTo(value));
        assertThat(postgresJsonStore.getLastUpdated(key, context).getAsLong(), equalTo(insertedAt));

        Thread.sleep(1000);

        // After 1 second passed it should be expired and we should no longer see it
        assertThat(postgresJsonStore.get(key, context), equalTo(Optional.empty()));
        assertThat(postgresJsonStore.getLastUpdated(key, context), equalTo(OptionalLong.empty()));
    }
}
