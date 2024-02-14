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
