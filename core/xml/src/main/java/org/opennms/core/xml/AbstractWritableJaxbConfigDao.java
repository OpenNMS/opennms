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
