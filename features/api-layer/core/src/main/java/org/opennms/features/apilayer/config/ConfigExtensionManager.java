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
package org.opennms.features.apilayer.config;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.config.api.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ConfigurationProvider} which is driven by API extensions.
 *
 * @param <E> extension interface type
 * @param <C> configuration bean type
 */
public abstract class ConfigExtensionManager<E,C> implements ConfigurationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigExtensionManager.class);

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Set<E> extensions = new LinkedHashSet<>();
    private final Class<C> clazz;

    private volatile C configObject;
    private volatile long lastUpdate = System.currentTimeMillis();

    public ConfigExtensionManager(Class<C> clazz, C initialObject) {
        this.clazz = Objects.requireNonNull(clazz);
        this.configObject = Objects.requireNonNull(initialObject);
    }

    @SuppressWarnings({ "rawtypes" })
    public void onBind(E extension, Map properties) {
        boolean didUpdate = false;
        rwLock.writeLock().lock();
        try {
            LOG.debug("bind called with {}: {}", extension, properties);
            if (extension != null) {
                extensions.add(extension);
                configObject = getConfigForExtensions(extensions);
                lastUpdate = System.currentTimeMillis();
                didUpdate = true;
            }
        } finally {
            rwLock.writeLock().unlock();
        }

        if (didUpdate) {
            triggerReload();
        }
    }

    @SuppressWarnings({ "rawtypes" })
    public void onUnbind(E extension, Map properties) {
        boolean didUpdate = false;
        rwLock.writeLock().lock();
        try {
            LOG.debug("unbind called with {}: {}", extension, properties);
            if (extension != null) {
                extensions.remove(extension);
                configObject = getConfigForExtensions(extensions);
                lastUpdate = System.currentTimeMillis();
                didUpdate = true;
            }
        } finally {
            rwLock.writeLock().unlock();
        }

        if (didUpdate) {
            triggerReload();
        }
    }

    @Override
    public void registeredToConfigReloadContainer() {
        LOG.debug("registeredToConfigReloadContainer - class: {}; clazz: {}", getClass().getCanonicalName(), clazz.getCanonicalName());
        triggerReload();
    }

    @Override
    public void deregisteredFromConfigReloadContainer() {
        LOG.debug("registeredToConfigReloadContainer - class: {}; clazz: {}", getClass().getCanonicalName(), clazz.getCanonicalName());
        triggerReload();
    }

    protected abstract C getConfigForExtensions(Set<E> extensions);

    protected abstract void triggerReload();

    @Override
    public Class<?> getType() {
        return clazz;
    }

    @Override
    public C getObject() {
        rwLock.readLock().lock();
        try {
            return configObject;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public long getLastUpdate() {
        rwLock.readLock().lock();
        try {
            return lastUpdate;
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
