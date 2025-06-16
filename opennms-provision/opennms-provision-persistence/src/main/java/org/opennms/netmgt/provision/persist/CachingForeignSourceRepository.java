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
package org.opennms.netmgt.provision.persist;

import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class CachingForeignSourceRepository extends AbstractForeignSourceRepository implements InitializingBean {
    private final static Logger LOG = LoggerFactory.getLogger(CachingForeignSourceRepository.class);

    private final ReentrantReadWriteLock m_globalLock = new ReentrantReadWriteLock(true);
    private final ReadLock m_readLock = m_globalLock.readLock();
    private final WriteLock m_writeLock = m_globalLock.writeLock();

    private ForeignSourceRepository m_foreignSourceRepository;

    private Set<String> m_dirtyForeignSources = new HashSet<>();
    private Set<String> m_dirtyRequisitions    = new HashSet<>();

    private Set<String> m_foreignSourceNames;
    private Map<String,ForeignSource> m_foreignSources;
    private Map<String,Requisition> m_requisitions;
    private ForeignSource m_defaultForeignSource;
    private ScheduledExecutorService m_executor;

    public CachingForeignSourceRepository() {
        long refreshInterval = SystemProperties.getLong("org.opennms.netmgt.provision.persist.cacheRefreshInterval", 300000);

        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        m_executor = executor;

        // every refreshInterval milliseconds, save any modifications, and clean out existing cached data
        m_executor.scheduleAtFixedRate(getRefreshRunnable(), refreshInterval, refreshInterval, TimeUnit.MILLISECONDS);
    }

    protected void writeUnlock() {
        if (m_globalLock.getWriteHoldCount() > 0) {
            m_writeLock.unlock();
        }
    }

    protected void writeLock() {
        if (m_globalLock.getWriteHoldCount() == 0) {
            while (m_globalLock.getReadHoldCount() > 0) {
                m_readLock.unlock();
            }
            m_writeLock.lock();
        }
    }

    protected void readUnlock() {
        if (m_globalLock.getReadHoldCount() > 0) {
            m_readLock.unlock();
        }
    }

    protected void readLock() {
        m_readLock.lock();
    }

    protected void cleanCache() {
        getRefreshRunnable().run();
    }

    protected Runnable getRefreshRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                writeLock();
                try {

                    // clear foreign source name cache
                    m_foreignSourceNames = null;

                    // clear the foreign source cache
                    if (m_dirtyForeignSources.size() > 0) {
                        for (final String dirtyForeignSource : m_dirtyForeignSources) {
                            final ForeignSource fs = getForeignSourceMap().get(dirtyForeignSource);
                            try {
                                if (fs == null) {
                                    final ForeignSource current = m_foreignSourceRepository.getForeignSource(dirtyForeignSource);
                                    if (current != null) {
                                        m_foreignSourceRepository.delete(current);
                                    }
                                } else {
                                    m_foreignSourceRepository.save(fs);
                                }
                            } catch (final ForeignSourceRepositoryException e) {
                                LOG.error("Failed to persist foreign source {}", dirtyForeignSource, e);
                            }
                        }
                        m_dirtyForeignSources.clear();
                    }
                    m_foreignSources = null;

                    // clear the requisition cache
                    if (m_dirtyRequisitions.size() > 0) {
                        for (final String dirtyRequisition : m_dirtyRequisitions) {
                            final Requisition r = getRequisitionMap().get(dirtyRequisition);
                            try {
                                if (r == null) {
                                    final Requisition current = m_foreignSourceRepository.getRequisition(dirtyRequisition);
                                    if (current != null) {
                                        m_foreignSourceRepository.delete(r);
                                    }
                                } else {
                                    m_foreignSourceRepository.save(r);
                                }
                            } catch (final ForeignSourceRepositoryException e) {
                                LOG.error("Failed to persist requisition {}", dirtyRequisition, e);
                            }
                        }
                        m_dirtyForeignSources.clear();
                    }
                    m_requisitions = null;

                } finally {
                    writeUnlock();
                }
            }
        };
    }

    public ForeignSourceRepository getForeignSourceRepository() {
        return m_foreignSourceRepository;
    }

    public void setForeignSourceRepository(final ForeignSourceRepository fsr) {
        m_foreignSourceRepository = fsr;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_foreignSourceRepository);
    }

    @Override
    public Set<String> getActiveForeignSourceNames() {
        readLock();
        try {
            if (m_foreignSourceNames == null) {
                m_foreignSourceNames = m_foreignSourceRepository.getActiveForeignSourceNames();
            }
            return m_foreignSourceNames;
        } finally {
            readUnlock();
        }
    }

    @Override
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        readLock();
        try {
            return getForeignSources().size();
        } finally {
            readUnlock();
        }
    }

    private Map<String,ForeignSource> getForeignSourceMap() {
        readLock();
        try {
            if (m_foreignSources == null) {
                writeLock();
                try {
                    final Map<String,ForeignSource> fses = new TreeMap<String,ForeignSource>();
                    for (final ForeignSource fs : m_foreignSourceRepository.getForeignSources()) {
                        fses.put(fs.getName(), fs);
                    }
                    m_foreignSources = fses;
                } finally {
                    readLock();
                    writeUnlock();
                }
            }
            return m_foreignSources;
        } finally {
            readUnlock();
        }
    }

    @Override
    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        readLock();
        try {
            return new TreeSet<ForeignSource>(getForeignSourceMap().values());
        } finally {
            readUnlock();
        }
    }

    @Override
    public ForeignSource getForeignSource(final String foreignSourceName) throws ForeignSourceRepositoryException {
        readLock();
        try {
            ForeignSource fs = getForeignSourceMap().get(foreignSourceName);
            if (fs == null) {
                fs = getDefaultForeignSource();
                fs.setName(foreignSourceName);
            }
            return fs;
        } finally {
            readUnlock();
        }
    }

    @Override
    public void save(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        readLock();
        try {
            validate(foreignSource);
            final Map<String,ForeignSource> fses = getForeignSourceMap();
            fses.put(foreignSource.getName(), foreignSource);
            m_dirtyForeignSources.add(foreignSource.getName());
        } finally {
            readUnlock();
        }
    }

    @Override
    public void delete(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        readLock();
        try {
            getForeignSourceMap().remove(foreignSource.getName());
            m_dirtyForeignSources.add(foreignSource.getName());
        } finally {
            readUnlock();
        }
    }

    @Override
    public ForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
        readLock();
        try {
            if (m_defaultForeignSource == null) {
                writeLock();
                try {
                    m_defaultForeignSource = m_foreignSourceRepository.getDefaultForeignSource();
                } finally {
                    readLock();
                    readUnlock();
                }
            }
            return m_defaultForeignSource;
        } finally {
            readUnlock();
        }
    }

    @Override
    public void putDefaultForeignSource(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        writeLock();
        try {
            cleanCache();
            m_foreignSourceRepository.putDefaultForeignSource(foreignSource);
        } finally {
            writeUnlock();
        }
    }

    @Override
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
        writeLock();
        try {
            cleanCache();
            m_foreignSourceRepository.resetDefaultForeignSource();
        } finally {
            writeUnlock();
        }
    }

    private Map<String,Requisition> getRequisitionMap() {
        readLock();
        try {
            if (m_requisitions == null) {
                writeLock();
                try {
                    final Map<String,Requisition> requisitions = new TreeMap<String,Requisition>();
                    for (final Requisition requisition : m_foreignSourceRepository.getRequisitions()) {
                        requisitions.put(requisition.getForeignSource(), requisition);
                    }
                    m_requisitions = requisitions;
                } finally {
                    readLock();
                    writeUnlock();
                }
            }
            return m_requisitions;
        } finally {
            readUnlock();
        }
    }

    @Override
    public Requisition importResourceRequisition(final Resource resource) throws ForeignSourceRepositoryException {
        writeLock();
        try {
            return super.importResourceRequisition(resource);
        } finally {
            writeUnlock();
        }
    }

    @Override
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        readLock();
        try {
            return new TreeSet<Requisition>(getRequisitionMap().values());
        } finally {
            readUnlock();
        }
    }

    @Override
    public Requisition getRequisition(final String foreignSourceName) throws ForeignSourceRepositoryException {
        readLock();
        try {
            return getRequisitionMap().get(foreignSourceName);
        } finally {
            readUnlock();
        }
    }

    @Override
    public Requisition getRequisition(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        readLock();
        try {
            return getRequisitionMap().get(foreignSource.getName());
        } finally {
            readUnlock();
        }
    }

    @Override
    public Date getRequisitionDate(final String foreignSource) {
        return m_foreignSourceRepository.getRequisitionDate(foreignSource);
    }

    @Override
    public URL getRequisitionURL(final String foreignSource) {
        return m_foreignSourceRepository.getRequisitionURL(foreignSource);
    }

    @Override
    public void save(final Requisition requisition) throws ForeignSourceRepositoryException {
        writeLock();
        try {
            validate(requisition);
            getRequisitionMap().put(requisition.getForeignSource(), requisition);
            m_dirtyRequisitions.add(requisition.getForeignSource());
        } finally {
            writeUnlock();
        }
    }

    @Override
    public void delete(final Requisition requisition) throws ForeignSourceRepositoryException {
        writeLock();
        try {
            getRequisitionMap().remove(requisition.getForeignSource());
            m_dirtyRequisitions.add(requisition.getForeignSource());
        } finally {
            writeUnlock();
        }
    }

    @Override
    public OnmsNodeRequisition getNodeRequisition(final String foreignSource, final String foreignId) throws ForeignSourceRepositoryException {
        readLock();
        try {
            final Requisition requisition = getRequisitionMap().get(foreignSource);
            return (requisition == null? null : requisition.getNodeRequistion(foreignId));
        } finally {
            readUnlock();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        m_executor.shutdown();
        cleanCache();
        super.finalize();
    }

    @Override
    public void flush() throws ForeignSourceRepositoryException {
        getRefreshRunnable().run();
    }

    @Override
    public void clear() throws ForeignSourceRepositoryException {
        cleanCache();
        writeLock();
        try {
            m_foreignSourceRepository.clear();
        } finally {
            writeUnlock();
        }
    }
}
