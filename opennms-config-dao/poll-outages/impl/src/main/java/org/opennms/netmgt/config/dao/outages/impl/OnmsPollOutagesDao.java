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

package org.opennms.netmgt.config.dao.outages.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.common.api.SaveableConfigContainer;
import org.opennms.netmgt.config.dao.common.impl.FileSystemSaveableConfigContainer;
import org.opennms.netmgt.config.dao.common.impl.JaxbToJsonStore;
import org.opennms.netmgt.config.dao.outages.api.WriteablePollOutagesDao;
import org.opennms.netmgt.config.poller.outages.Outages;

import com.google.common.annotations.VisibleForTesting;

public class OnmsPollOutagesDao extends AbstractPollOutagesDao implements WriteablePollOutagesDao, JaxbToJsonStore<Outages> {
    private final SaveableConfigContainer<Outages> saveableConfigContainer;

    @VisibleForTesting
    OnmsPollOutagesDao(JsonStore jsonStore, File configFile) {
        super(jsonStore);
        Objects.requireNonNull(configFile);
        saveableConfigContainer = new FileSystemSaveableConfigContainer<>(Outages.class, "poll-outages",
                Collections.singleton(getJsonWriterCallbackFunction(jsonStore, JSON_STORE_KEY,
                        ConfigDaoConstants.JSON_KEY_STORE_CONTEXT)), configFile);
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
            consumerWithLock.accept(getConfig());
        } finally {
            getWriteLock().unlock();
        }
    }

    @Override
    public void saveConfig() {
        saveableConfigContainer.saveConfig();
    }

    @Override
    public Outages getConfig() {
        return saveableConfigContainer.getConfig();
    }

    @Override
    public void reload() {
        saveableConfigContainer.reload();
    }
}
