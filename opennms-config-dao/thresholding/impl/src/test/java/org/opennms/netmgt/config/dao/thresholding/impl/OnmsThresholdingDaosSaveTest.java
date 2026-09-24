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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.threshd.Filter;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

/**
 * Callers edit the DAO's live configuration in place and then call {@code saveConfig()}; a save the
 * configuration manager rejects must not leave those edits behind.
 */
public class OnmsThresholdingDaosSaveTest {

    @Test
    public void rejectedThreshdSaveRestoresTheStoredConfiguration() {
        final ThreshdConfiguration stored = new ThreshdConfiguration();
        stored.addPackage(newPackage("stored"));

        final ConfigurationManagerService cms = rejectingCms(OnmsThreshdDao.CONFIG_NAME, ConfigConvertUtil.objectToJson(stored));
        final OnmsThreshdDao dao = new OnmsThreshdDao(mock(JsonStore.class), cms);

        final ThreshdConfiguration live = dao.getWriteableConfig();
        live.addPackage(newPackage("rejected"));

        try {
            dao.saveConfig();
            fail("the save should have been rejected");
        } catch (final ValidationException expected) {
        }

        assertThat(dao.getWriteableConfig(), equalTo(stored));
        assertThat(dao.getReadOnlyConfig(), equalTo(stored));
    }

    @Test
    public void rejectedThresholdingSaveRestoresTheStoredConfiguration() {
        final ThresholdingConfig stored = new ThresholdingConfig();
        stored.addGroup(newGroup("stored"));

        final ConfigurationManagerService cms = rejectingCms(OnmsThresholdingDao.CONFIG_NAME, ConfigConvertUtil.objectToJson(stored));
        final OnmsThresholdingDao dao = new OnmsThresholdingDao(mock(JsonStore.class), cms);

        final ThresholdingConfig live = dao.getWriteableConfig();
        live.addGroup(newGroup("rejected"));

        try {
            dao.saveConfig();
            fail("the save should have been rejected");
        } catch (final ValidationException expected) {
        }

        assertThat(dao.getWriteableConfig(), equalTo(stored));
        assertThat(dao.getReadOnlyConfig(), equalTo(stored));
    }

    @Test
    public void failedReloadIsSuppressedAndTheSaveFailureRethrown() {
        final ThreshdConfiguration stored = new ThreshdConfiguration();
        stored.addPackage(newPackage("stored"));

        final ConfigurationManagerService cms = rejectingCms(OnmsThreshdDao.CONFIG_NAME, ConfigConvertUtil.objectToJson(stored));
        final OnmsThreshdDao dao = new OnmsThreshdDao(mock(JsonStore.class), cms);
        dao.getWriteableConfig();

        final IllegalStateException reloadFailure = new IllegalStateException("database is gone");
        when(cms.getJSONStrConfiguration(OnmsThreshdDao.CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID)).thenThrow(reloadFailure);

        try {
            dao.saveConfig();
            fail("the save should have been rejected");
        } catch (final ValidationException expected) {
            assertThat(expected.getSuppressed()[0], sameInstance(reloadFailure));
        }
    }

    private static ConfigurationManagerService rejectingCms(final String configName, final String storedJson) {
        final ConfigurationManagerService cms = mock(ConfigurationManagerService.class);
        when(cms.getJSONStrConfiguration(configName, ConfigDefinition.DEFAULT_CONFIG_ID)).thenReturn(Optional.of(storedJson));
        doThrow(new ValidationException("rejected by schema"))
                .when(cms).updateConfiguration(eq(configName), eq(ConfigDefinition.DEFAULT_CONFIG_ID), any(JsonAsString.class), anyBoolean());
        return cms;
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
