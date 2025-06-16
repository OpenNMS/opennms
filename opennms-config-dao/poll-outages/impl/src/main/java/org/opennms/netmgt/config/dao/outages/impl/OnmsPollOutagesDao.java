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
package org.opennms.netmgt.config.dao.outages.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.common.api.SaveableConfigContainer;
import org.opennms.netmgt.config.dao.common.impl.FileSystemSaveableConfigContainer;
import org.opennms.netmgt.config.dao.outages.api.WriteablePollOutagesDao;
import org.opennms.netmgt.config.poller.outages.Outages;

import com.google.common.annotations.VisibleForTesting;

public class OnmsPollOutagesDao extends AbstractPollOutagesDao implements WriteablePollOutagesDao {
    private final SaveableConfigContainer<Outages> saveableConfigContainer;
    private final ObjectMapper objectMapper = JacksonUtils.createDefaultObjectMapper();
    private volatile Outages filesystemConfig;

    @VisibleForTesting
    OnmsPollOutagesDao(JsonStore jsonStore, File configFile) {
        super(jsonStore);
        Objects.requireNonNull(configFile);
        saveableConfigContainer = new FileSystemSaveableConfigContainer<>(Outages.class, "poll-outages",
                Collections.singleton(this::fileSystemConfigUpdated), configFile);
        reload();
    }

    public OnmsPollOutagesDao(JsonStore jsonStore) throws IOException {
        this(jsonStore, ConfigFileConstants.getFile(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME));
    }

    @Override
    public Lock getReadLock() {
        return saveableConfigContainer.getReadLock();
    }

    @Override
    public Lock getWriteLock() {
        return saveableConfigContainer.getWriteLock();
    }

    @Override
    public void withWriteLock(Consumer<Outages> consumerWithLock) {
        getWriteLock().lock();

        try {
            consumerWithLock.accept(getWriteableConfig());
        } finally {
            getWriteLock().unlock();
        }
    }

    @Override
    public void saveConfig() {
        saveableConfigContainer.saveConfig();
    }

    @Override
    public Outages getReadOnlyConfig() {
        return saveableConfigContainer.getConfig();
    }

    /**
     * This DAO doesn't currently merge any configuration so we can serve the read only configuration directly.
     */
    @Override
    public Outages getWriteableConfig() {
        return getReadOnlyConfig();
    }

    @Override
    public void reload() {
        saveableConfigContainer.reload();
    }

    @Override
    public void onConfigChanged() {
        try {
            jsonStore.put(JSON_STORE_KEY, objectMapper.writeValueAsString(filesystemConfig),
                    ConfigDaoConstants.JSON_KEY_STORE_CONTEXT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void fileSystemConfigUpdated(Outages updatedConfig) {
        filesystemConfig = updatedConfig;
        onConfigChanged();
    }
}
