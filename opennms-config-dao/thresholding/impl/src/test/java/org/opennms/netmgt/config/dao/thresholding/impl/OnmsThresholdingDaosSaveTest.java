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
package org.opennms.netmgt.config.dao.thresholding.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.threshd.Filter;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

/**
 * Concurrency and failure behaviour of the configuration-manager backed threshold DAOs: writers edit a private
 * draft under the write lock, readers only ever see complete, published configurations, and reloads never
 * overwrite a writer's pending edits.
 */
public class OnmsThresholdingDaosSaveTest {

    private static final long BLOCKED_MILLIS = 200;

    @Test
    public void rejectedThreshdSaveLeavesThePublishedConfigurationUntouched() {
        final ThreshdConfiguration stored = threshd("stored");
        final FakeCms cms = new FakeCms(OnmsThreshdDao.CONFIG_NAME, stored);
        cms.rejectUpdates();
        final OnmsThreshdDao dao = new OnmsThreshdDao(mock(JsonStore.class), cms.mock);

        try {
            dao.withWriteLock(config -> {
                config.addPackage(newPackage("rejected"));
                dao.saveConfig();
            });
            fail("the save should have been rejected");
        } catch (final ValidationException expected) {
        }

        assertThat(dao.getWriteableConfig(), equalTo(stored));
        assertThat(dao.getReadOnlyConfig(), equalTo(stored));
    }

    @Test
    public void rejectedThresholdingSaveLeavesThePublishedConfigurationUntouched() {
        final ThresholdingConfig stored = thresholding("stored");
        final FakeCms cms = new FakeCms(OnmsThresholdingDao.CONFIG_NAME, stored);
        cms.rejectUpdates();
        final OnmsThresholdingDao dao = new OnmsThresholdingDao(mock(JsonStore.class), cms.mock);

        try {
            dao.withWriteLock(config -> {
                config.addGroup(newGroup("rejected"));
                dao.saveConfig();
            });
            fail("the save should have been rejected");
        } catch (final ValidationException expected) {
        }

        assertThat(dao.getWriteableConfig(), equalTo(stored));
        assertThat(dao.getReadOnlyConfig(), equalTo(stored));
    }

    @Test
    public void reloadWaitsForTheWriterSoItsEditsAreSaved() throws Exception {
        final FakeCms cms = new FakeCms(OnmsThreshdDao.CONFIG_NAME, threshd("stored"));
        final OnmsThreshdDao dao = new OnmsThreshdDao(mock(JsonStore.class), cms.mock);
        dao.getReadOnlyConfig();

        final AtomicReference<Thread> reloader = new AtomicReference<>();
        dao.withWriteLock(config -> {
            config.addPackage(newPackage("edited"));

            reloader.set(new Thread(dao::onConfigChanged));
            reloader.get().start();
            join(reloader.get(), BLOCKED_MILLIS);
            assertTrue("a reload must wait for the writer", reloader.get().isAlive());

            dao.saveConfig();
        });
        join(reloader.get(), TimeUnit.SECONDS.toMillis(10));
        assertFalse(reloader.get().isAlive());

        assertThat(cms.lastUpdate.get(), containsString("edited"));
        assertThat(packageNames(dao.getReadOnlyConfig()), equalTo(List.of("stored", "edited")));
    }

    @Test
    public void readersKeepAConsistentSnapshotWhileAWriterReplacesThePackages() {
        final FakeCms cms = new FakeCms(OnmsThreshdDao.CONFIG_NAME, threshd("stored"));
        cms.fireUpdateEvents();
        final OnmsThreshdDao dao = new OnmsThreshdDao(mock(JsonStore.class), cms.mock);
        final ThreshdConfiguration before = dao.getReadOnlyConfig();

        dao.withWriteLock(config -> {
            assertThat(config, not(sameInstance(before)));
            config.setPackages(List.of(newPackage("replacement")));
            assertThat("readers must not see unsaved edits", packageNames(dao.getReadOnlyConfig()), equalTo(List.of("stored")));
            dao.saveConfig();
        });

        assertThat(packageNames(before), equalTo(List.of("stored")));
        assertThat(packageNames(dao.getReadOnlyConfig()), equalTo(List.of("replacement")));
    }

    @Test
    public void configManagerUpdateDuringAWriteIsDeferredInsteadOfBlocking() throws Exception {
        final FakeCms cms = new FakeCms(OnmsThreshdDao.CONFIG_NAME, threshd("stored"));
        final OnmsThreshdDao dao = new OnmsThreshdDao(mock(JsonStore.class), cms.mock);
        dao.getReadOnlyConfig();

        dao.withWriteLock(config -> {
            // Someone else changes the configuration through the configuration manager while we hold the lock.
            cms.stored.set(ConfigConvertUtil.objectToJson(threshd("external")));
            final Thread notifier = new Thread(cms::fireUpdateEvent);
            notifier.start();
            join(notifier, TimeUnit.SECONDS.toMillis(10));
            assertFalse("the update handler must not block on the write lock", notifier.isAlive());
            assertThat(packageNames(dao.getReadOnlyConfig()), equalTo(List.of("stored")));
        });

        assertThat("the deferred reload runs when the lock is released",
                packageNames(dao.getReadOnlyConfig()), equalTo(List.of("external")));
    }

    @Test
    public void failedFirstThreshdLoadIsRetried() {
        final ThreshdConfiguration stored = threshd("stored");
        final FakeCms cms = new FakeCms(OnmsThreshdDao.CONFIG_NAME, stored);
        cms.failNextLoad();
        final OnmsThreshdDao dao = new OnmsThreshdDao(mock(JsonStore.class), cms.mock);

        try {
            dao.getReadOnlyConfig();
            fail("the first load should have failed");
        } catch (final IllegalStateException expected) {
        }

        assertThat(dao.getReadOnlyConfig(), equalTo(stored));
    }

    @Test
    public void failedFirstThresholdingLoadIsRetried() {
        final ThresholdingConfig stored = thresholding("stored");
        final FakeCms cms = new FakeCms(OnmsThresholdingDao.CONFIG_NAME, stored);
        cms.failNextLoad();
        final OnmsThresholdingDao dao = new OnmsThresholdingDao(mock(JsonStore.class), cms.mock);

        try {
            dao.getReadOnlyConfig();
            fail("the first load should have failed");
        } catch (final IllegalStateException expected) {
        }

        assertThat(dao.getReadOnlyConfig(), equalTo(stored));
    }

    @Test
    public void successfulSaveIsPublishedWithoutAnUpdateEvent() {
        final FakeCms cms = new FakeCms(OnmsThresholdingDao.CONFIG_NAME, thresholding("stored"));
        final OnmsThresholdingDao dao = new OnmsThresholdingDao(mock(JsonStore.class), cms.mock);

        dao.withWriteLock(config -> {
            config.addGroup(newGroup("added"));
            dao.saveConfig();
        });

        assertThat(dao.getReadOnlyConfig(), equalTo(thresholding("stored", "added")));
    }

    @Test
    public void successfulSaveWithAnUpdateEventReloadsOnlyOnce() {
        final FakeCms cms = new FakeCms(OnmsThresholdingDao.CONFIG_NAME, thresholding("stored"));
        cms.fireUpdateEvents();
        final OnmsThresholdingDao dao = new OnmsThresholdingDao(mock(JsonStore.class), cms.mock);
        dao.getReadOnlyConfig();

        dao.withWriteLock(config -> {
            config.addGroup(newGroup("added"));
            dao.saveConfig();
        });

        verify(cms.mock, times(2)).getJSONStrConfiguration(OnmsThresholdingDao.CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
        assertThat(dao.getReadOnlyConfig(), equalTo(thresholding("stored", "added")));
    }

    /** A configuration manager holding one document, backed by a Mockito mock. */
    private static final class FakeCms {
        final ConfigurationManagerService mock = mock(ConfigurationManagerService.class);
        final AtomicReference<String> stored = new AtomicReference<>();
        final AtomicReference<String> lastUpdate = new AtomicReference<>();
        final AtomicReference<Consumer<ConfigUpdateInfo>> handler = new AtomicReference<>();
        private final String configName;
        private volatile boolean failNextLoad;
        private volatile boolean rejectUpdates;
        private volatile boolean fireUpdateEvents;

        FakeCms(final String configName, final Object initial) {
            this.configName = configName;
            stored.set(ConfigConvertUtil.objectToJson(initial));
            doAnswer(invocation -> {
                handler.set(invocation.getArgument(2));
                return null;
            }).when(mock).registerEventHandler(eq(EventType.UPDATE), any(ConfigUpdateInfo.class), any());
            when(mock.getJSONStrConfiguration(configName, ConfigDefinition.DEFAULT_CONFIG_ID)).thenAnswer(invocation -> {
                if (failNextLoad) {
                    failNextLoad = false;
                    throw new IllegalStateException("database is gone");
                }
                return Optional.ofNullable(stored.get());
            });
            doAnswer(invocation -> {
                if (rejectUpdates) {
                    throw new ValidationException("rejected by schema");
                }
                final String json = invocation.<JsonAsString>getArgument(2).toString();
                lastUpdate.set(json);
                stored.set(json);
                if (fireUpdateEvents) {
                    fireUpdateEvent();
                }
                return null;
            }).when(mock).updateConfiguration(eq(configName), eq(ConfigDefinition.DEFAULT_CONFIG_ID), any(JsonAsString.class), anyBoolean());
        }

        void rejectUpdates() {
            rejectUpdates = true;
        }

        void failNextLoad() {
            failNextLoad = true;
        }

        void fireUpdateEvents() {
            fireUpdateEvents = true;
        }

        void fireUpdateEvent() {
            handler.get().accept(new ConfigUpdateInfo(configName, ConfigDefinition.DEFAULT_CONFIG_ID));
        }
    }

    private static void join(final Thread thread, final long millis) {
        try {
            thread.join(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private static List<String> packageNames(final ThreshdConfiguration config) {
        return config.getPackages().stream().map(Package::getName).toList();
    }

    private static ThreshdConfiguration threshd(final String... packageNames) {
        final ThreshdConfiguration config = new ThreshdConfiguration();
        for (final String name : packageNames) {
            config.addPackage(newPackage(name));
        }
        return config;
    }

    private static ThresholdingConfig thresholding(final String... groupNames) {
        final ThresholdingConfig config = new ThresholdingConfig();
        for (final String name : groupNames) {
            config.addGroup(newGroup(name));
        }
        return config;
    }

    private static Package newPackage(final String name) {
        final Package pkg = new Package();
        pkg.setName(name);
        pkg.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        return pkg;
    }

    private static Group newGroup(final String name) {
        final Group group = new Group();
        group.setName(name);
        group.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        return group;
    }
}
