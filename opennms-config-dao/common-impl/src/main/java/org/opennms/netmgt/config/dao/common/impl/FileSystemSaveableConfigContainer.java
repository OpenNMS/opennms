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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.opennms.core.xml.AbstractWritableJaxbConfigDao;
import org.opennms.netmgt.config.dao.common.api.SaveableConfigContainer;
import org.springframework.core.io.FileSystemResource;

/**
 * Contains a JAXB annotated entity that is backed by a file on disk. The file is held in a
 * {@link AbstractWritableJaxbConfigDao} and is subject to the reloading strategy of that class.
 */
public class FileSystemSaveableConfigContainer<T> implements SaveableConfigContainer<T> {
    private final ConfigDao proxyConfigDao;

    public FileSystemSaveableConfigContainer(Class<T> clazz, String configName,
                                             Collection<Consumer<T>> onConfigChangeCallbacks,
                                             File configFile) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configFile);
        proxyConfigDao = new ConfigDao(clazz, configName);
        // When saveConfig() or reload() below are called, they will end up triggering invocation of these callbacks via
        // the config dao
        if (onConfigChangeCallbacks != null) {
            onConfigChangeCallbacks.forEach(proxyConfigDao::addOnReloadedCallback);
        }
        proxyConfigDao.setConfigResource(new FileSystemResource(configFile));
        proxyConfigDao.afterPropertiesSet();
    }

    @Override
    public void saveConfig() {
        try {
            proxyConfigDao.saveCurrent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        proxyConfigDao.update();
    }

    @Override
    public T getConfig() {
        getReadLock().lock();
        try {
            return proxyConfigDao.getContainer().getObject();
        } finally {
            getReadLock().unlock();
        }
    }

    @Override
    public Lock getReadLock() {
        return proxyConfigDao.getReadLock();
    }

    @Override
    public Lock getWriteLock() {
        return proxyConfigDao.getWriteLock();
    }

    private class ConfigDao extends AbstractWritableJaxbConfigDao<T, T> {
        public ConfigDao(Class<T> entityClass, String description) {
            super(entityClass, description);
        }

        @Override
        protected T translateConfig(T config) {
            return config;
        }
    }
}
