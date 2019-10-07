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

package org.opennms.netmgt.config.dao.common.impl;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ReloadableConfigContainer;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

public class PollingJsonStoreReloadableConfigContainerTest {
    private static final String key = "key";
    private static final String context = "context";
    private final JsonStore jsonStore = mock(JsonStore.class);
    private final ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();

    @Test
    public void canReload() throws IOException {
        ReloadableConfigContainer<TestEntity> container =
                new PollingJsonStoreReloadableConfigContainer<>(jsonStore, key, context, TestEntity.class, 10000,
                        Retry.of("retryReloading", RetryConfig.custom()
                                .maxAttempts(60)
                                .waitDuration(Duration.ofSeconds(5))
                                .build()));

        TestEntity testEntity = new TestEntity();
        testEntity.setTestValues(Collections.singletonList("test"));
        String mappedJson = mapper.writeValueAsString(testEntity);

        when(jsonStore.getLastUpdated(key, context)).thenReturn(OptionalLong.of(1));
        when(jsonStore.get(key, context)).thenReturn(Optional.of(mappedJson));

        // The container and the Json store should be in sync at this point
        TestEntity fromJsonStore = container.getConfig();
        assertThat(testEntity, equalTo(fromJsonStore));

        testEntity.setTestValues(Collections.singletonList("tubes"));
        mappedJson = mapper.writeValueAsString(testEntity);
        when(jsonStore.getLastUpdated(key, context)).thenReturn(OptionalLong.of(System.currentTimeMillis() + 1));
        when(jsonStore.get(key, context)).thenReturn(Optional.of(mappedJson));

        // Now the container shouldn't be in sync since the poll interval will not have been exceeded
        fromJsonStore = container.getConfig();
        assertThat(testEntity, not(equalTo(fromJsonStore)));

        // Since we do an explicit reload, the container should now be in sync with the json store again
        container.reload();
        fromJsonStore = container.getConfig();
        assertThat(testEntity, equalTo(fromJsonStore));
    }

    @Test
    public void canStayInSyncWithPolling() throws IOException {
        long interval = 100;
        ReloadableConfigContainer<TestEntity> container =
                new PollingJsonStoreReloadableConfigContainer<>(jsonStore, key, context, TestEntity.class, interval,
                        Retry.of("retryReloading", RetryConfig.custom()
                                .maxAttempts(60)
                                .waitDuration(Duration.ofSeconds(5))
                                .build()));

        TestEntity testEntity = new TestEntity();
        testEntity.setTestValues(Collections.singletonList("test"));
        String mappedJson = mapper.writeValueAsString(testEntity);

        when(jsonStore.getLastUpdated(key, context)).thenReturn(OptionalLong.of(1));
        when(jsonStore.get(key, context)).thenReturn(Optional.of(mappedJson));

        // The container and the Json store should be in sync at this point
        TestEntity fromJsonStore = container.getConfig();
        assertThat(testEntity, equalTo(fromJsonStore));

        testEntity.setTestValues(Collections.singletonList("tubes"));
        mappedJson = mapper.writeValueAsString(testEntity);
        when(jsonStore.getLastUpdated(key, context)).thenReturn(OptionalLong.of(System.currentTimeMillis() + 1));
        when(jsonStore.get(key, context)).thenReturn(Optional.of(mappedJson));

        // Now the container shouldn't be in sync since the poll interval will not have been exceeded
        fromJsonStore = container.getConfig();
        assertThat(testEntity, not(equalTo(fromJsonStore)));

        // If we wait a bit, then the poll interval will be exceeded and we should in sync again
        await().atMost(interval + 100, TimeUnit.MILLISECONDS)
                .pollDelay(10, TimeUnit.MILLISECONDS)
                .until(() -> testEntity.equals(container.getConfig()));
    }

    @Test
    public void retriesAndUnblocksWhenJsonStoreAvailable() throws IOException {
        ReloadableConfigContainer<TestEntity> container =
                new PollingJsonStoreReloadableConfigContainer<>(jsonStore, key, context, TestEntity.class, 10000,
                        Retry.of("retryReloading", RetryConfig.custom()
                                .maxAttempts(10)
                                .waitDuration(Duration.ofMillis(10))
                                .build()));
        AtomicBoolean unblocked = new AtomicBoolean(false);

        // The first attempt to get should block until throwing an exception since the Json store isn't returning
        // anything
        try {
            container.getConfig();
            fail("Did not time out");
        } catch (Exception ignore) {
        }

        // Now try to get again async
        CompletableFuture.runAsync(() -> {
            container.getConfig();
            unblocked.set(true);
        });

        // Client should currently be blocked since the mock won't respond with any JSON
        assertThat(unblocked.get(), equalTo(false));

        when(jsonStore.getLastUpdated(key, context)).thenReturn(OptionalLong.of(1));
        when(jsonStore.get(key, context)).thenReturn(Optional.of(mapper.writeValueAsString(new TestEntity())));

        // Now that the mock is returning JSON the client's get should unblock
        await().atMost(1, TimeUnit.SECONDS)
                .until(unblocked::get);
    }

}
