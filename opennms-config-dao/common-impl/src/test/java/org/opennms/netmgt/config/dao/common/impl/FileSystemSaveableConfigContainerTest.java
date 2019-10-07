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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.dao.common.api.SaveableConfigContainer;

public class FileSystemSaveableConfigContainerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void canReload() throws IOException {
        File xmlFile = tempFolder.newFile("test.xml");
        TestEntity testEntity = new TestEntity();
        testEntity.setTestValues(Arrays.asList("Hello", "World"));
        Files.write(xmlFile.toPath(), JaxbUtils.marshal(testEntity).getBytes());

        // Create the container and verify it initialized with the content on the filesystem
        SaveableConfigContainer<TestEntity> container = new FileSystemSaveableConfigContainer<>(TestEntity.class,
                "test", null, xmlFile);
        TestEntity fromContainer = container.getConfig();
        assertThat(testEntity, equalTo(fromContainer));

        // Re-write to file
        testEntity.setTestValues(Arrays.asList("new"));
        Files.write(xmlFile.toPath(), JaxbUtils.marshal(testEntity).getBytes());

        // The file has been updated but we shouldn't see that reflected yet since the polling interval should not have
        // elapsed
        fromContainer = container.getConfig();
        assertThat(testEntity, not(equalTo(fromContainer)));

        // Do a reload and now we should see the change
        container.reload();
        fromContainer = container.getConfig();
        assertThat(testEntity, equalTo(fromContainer));
    }

    @Test
    public void canSave() throws IOException {
        File xmlFile = tempFolder.newFile("test.xml");
        TestEntity testEntity = new TestEntity();
        testEntity.setTestValues(Arrays.asList("Hello", "World"));
        Files.write(xmlFile.toPath(), JaxbUtils.marshal(testEntity).getBytes());

        // Create the container and verify it initialized with the content on the filesystem
        SaveableConfigContainer<TestEntity> container = new FileSystemSaveableConfigContainer<>(TestEntity.class,
                "test", null, xmlFile);
        TestEntity fromContainer = container.getConfig();
        assertThat(testEntity, equalTo(fromContainer));

        // Change the cached copy of the config and trigger it to be written back to the filesystem
        fromContainer.setTestValues(Arrays.asList("hello", "from", "the", "code"));
        container.saveConfig();

        // Now inspect the filesystem directly to verify it matches the updated copy
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()));
        TestEntity unmarshaledEntity = JaxbUtils.unmarshal(TestEntity.class, xmlContent);
        assertThat(fromContainer, equalTo(unmarshaledEntity));
    }

    @Test
    public void canTriggerChangeCallbacks() throws IOException {
        AtomicInteger callbackCalled = new AtomicInteger(0);
        AtomicReference<TestEntity> callbackValue = new AtomicReference<>();

        File xmlFile = tempFolder.newFile("test.xml");
        TestEntity testEntity = new TestEntity();
        testEntity.setTestValues(Arrays.asList("Hello", "World"));
        Files.write(xmlFile.toPath(), JaxbUtils.marshal(testEntity).getBytes());

        // Create the container and verify it initialized with the content on the filesystem
        SaveableConfigContainer<TestEntity> container = new FileSystemSaveableConfigContainer<>(TestEntity.class,
                "test", Collections.singleton(e -> {
                    callbackCalled.incrementAndGet();
                    callbackValue.set(e);
                    
        }), xmlFile);
        
        // When the container initialized our callback should have been called for the first time
        await().atMost(100, TimeUnit.MILLISECONDS)
                .pollDelay(10, TimeUnit.MILLISECONDS)
                .until(() -> callbackCalled.get() == 1 && Objects.equals(callbackValue.get(), testEntity));
        
        // If we reload now, there should not be any callback triggered
        container.reload();
        
        // Wait for a bit to make sure the callback isn't called
        try {
            await().atMost(100, TimeUnit.MILLISECONDS)
                    .pollDelay(10, TimeUnit.MILLISECONDS)
                    .until(() -> callbackCalled.get() != 1 || !Objects.equals(callbackValue.get(), testEntity));
            fail("Callback should not have been called");
        } catch (Exception ignore) {}
        
        // Now make sure that a save also triggers the callback
        TestEntity fromContainer = container.getConfig();
        fromContainer.setTestValues(Arrays.asList("new", "values"));
        container.saveConfig();

        await().atMost(100, TimeUnit.MILLISECONDS)
                .pollDelay(10, TimeUnit.MILLISECONDS)
                .until(() -> callbackCalled.get() == 2 && Objects.equals(callbackValue.get(), fromContainer));
    }
}
