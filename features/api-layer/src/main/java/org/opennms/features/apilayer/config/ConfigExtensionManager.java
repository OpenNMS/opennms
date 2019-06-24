/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
    private static final Logger LOG = LoggerFactory.getLogger(EventConfExtensionManager.class);

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
