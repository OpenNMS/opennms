/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.xml;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a writable AbstractJaxbConfigDao
 *
 * @author brozow
 * @author djgregor
 */
public abstract class AbstractWritableJaxbConfigDao<K,V> extends AbstractJaxbConfigDao<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractWritableJaxbConfigDao.class);

    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    public AbstractWritableJaxbConfigDao(final Class<K> entityClass, final String description) {
        super(entityClass, description);
    }

    public Lock getReadLock() {
        return m_readLock;
    }

    public Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * <p>getConfig</p>
     *
     * @return Returns the config.
     */
    protected V getObject() {
        getReadLock().lock();
        try {
            return getContainer().getObject();
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws java.io.IOException
     *             if any.
     */
    public void saveCurrent() throws IOException {
        File file;
        try {
            file = getConfigResource().getFile();
        } catch (final IOException e) {
            LOG.warn("Resource '{}' cannot be saved because it does not seem to have an underlying File object: {}", getConfigResource(), e);
            throw e;
        }

        getWriteLock().lock();

        try {
            JaxbUtils.marshal(getObject(), file);
        } finally {
            getWriteLock().unlock();
        }

        update();
    }

    /**
     * <p>
     * update
     * </p>
     *
     * @throws java.io.IOException
     *             if any.
     */
    public void update() {
        getReadLock().lock();
        try {
            getContainer().reload();
        } finally {
            getReadLock().unlock();
        }
    }
}
