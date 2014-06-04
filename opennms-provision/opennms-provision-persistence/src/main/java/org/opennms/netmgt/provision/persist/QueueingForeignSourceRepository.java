/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.net.URL;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class QueueingForeignSourceRepository implements ForeignSourceRepository, InitializingBean {
    private final ConcurrentMap<String,Requisition> m_pendingRequisitions     = new ConcurrentHashMap<String,Requisition>();
    private final ConcurrentMap<String,ForeignSource> m_pendingForeignSources = new ConcurrentHashMap<String,ForeignSource>();
    ForeignSourceRepository m_repository = null;
    private ExecutorService m_executor = Executors.newSingleThreadExecutor();

    public QueueingForeignSourceRepository() {
        super();
    }

    @Override
    public void flush() throws ForeignSourceRepositoryException {
        LogUtils.debugf(this, "flushing queue");
        // wait for everything currently in the queue to complete

        final CountDownLatch latch = new CountDownLatch(1);
        m_executor.execute(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (final InterruptedException e) {
            LogUtils.debugf(this, e, "Interrupted while waiting for ForeignSourceRepository flush.  Returning.");
            return;
        }
        LogUtils.debugf(this, "finished flushing queue");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_repository, "No foreign source repository was set!");
    }

    public ForeignSourceRepository getForeignSourceRepository() {
        return m_repository;
    }
    
    public void setForeignSourceRepository(final ForeignSourceRepository fsr) {
        m_repository = fsr;
    }

    @Override
    public Set<String> getActiveForeignSourceNames() {
        return m_repository.getActiveForeignSourceNames();
    }

    @Override
    public int getForeignSourceCount() throws ForeignSourceRepositoryException {
        return getActiveForeignSourceNames().size();
    }

    @Override
    public Set<ForeignSource> getForeignSources() throws ForeignSourceRepositoryException {
        return m_repository.getForeignSources();
    }

    @Override
    public ForeignSource getForeignSource(final String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_repository.getForeignSource(foreignSourceName);
    }

    @Override
    public void save(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        LogUtils.debugf(this, "Queueing save of foreign source %s", foreignSource.getName());
        m_pendingForeignSources.put(foreignSource.getName(), foreignSource);
        m_executor.execute(new QueuePersistRunnable());
    }

    @Override
    public void delete(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        LogUtils.debugf(this, "Queueing delete of foreign source %s", foreignSource.getName());
        m_pendingForeignSources.put(foreignSource.getName(), new DeletedForeignSource(foreignSource));
        m_executor.execute(new QueuePersistRunnable());
    }

    @Override
    public Set<Requisition> getRequisitions() throws ForeignSourceRepositoryException {
        return m_repository.getRequisitions();
    }

    @Override
    public Requisition getRequisition(final String foreignSourceName) throws ForeignSourceRepositoryException {
        return m_repository.getRequisition(foreignSourceName);
    }

    @Override
    public Requisition getRequisition(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        return m_repository.getRequisition(foreignSource);
    }

    @Override
    public Date getRequisitionDate(final String foreignSource) {
        if (m_pendingRequisitions.containsKey(foreignSource)) {
            return m_pendingRequisitions.get(foreignSource).getDate();
        }
        return m_repository.getRequisitionDate(foreignSource);
    }

    @Override
    public URL getRequisitionURL(final String foreignSource) {
        return m_repository.getRequisitionURL(foreignSource);
    }

    @Override
    public void save(final Requisition requisition) throws ForeignSourceRepositoryException {
        LogUtils.debugf(this, "Queueing save of requisition %s (containing %d nodes)", requisition.getForeignSource(), requisition.getNodeCount());
        m_pendingRequisitions.put(requisition.getForeignSource(), requisition);
        m_executor.execute(new QueuePersistRunnable());
    }

    @Override
    public void delete(final Requisition requisition) throws ForeignSourceRepositoryException {
        LogUtils.debugf(this, "Queueing delete of requistion %s", requisition.getForeignSource());
        m_pendingRequisitions.put(requisition.getForeignSource(), new DeletedRequisition(requisition));
        m_executor.execute(new QueuePersistRunnable());
    }

    @Override
    public ForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
        return m_repository.getDefaultForeignSource();
    }

    @Override
    public void putDefaultForeignSource(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_repository.putDefaultForeignSource(foreignSource);
    }

    @Override
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
        m_repository.resetDefaultForeignSource();
    }

    @Override
    public Requisition importResourceRequisition(final Resource resource) throws ForeignSourceRepositoryException {
        return m_repository.importResourceRequisition(resource);
    }

    @Override
    public OnmsNodeRequisition getNodeRequisition(final String foreignSource, final String foreignId) throws ForeignSourceRepositoryException {
        return m_repository.getNodeRequisition(foreignSource, foreignId);
    }

    @Override
    public void validate(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        m_repository.validate(foreignSource);
    }

    @Override
    public void validate(final Requisition requisition) throws ForeignSourceRepositoryException {
        m_repository.validate(requisition);
    }

    private final class QueuePersistRunnable implements Runnable {
        private final String m_prefix;

        public QueuePersistRunnable() {
            m_prefix = ThreadCategory.getPrefix();
        }

        @Override
        public void run() {
            final String prefix = ThreadCategory.getPrefix();
            try {
                ThreadCategory.setPrefix(m_prefix);
                LogUtils.debugf(this, "persisting repository changes");
                final Set<Entry<String,ForeignSource>> foreignSources = m_pendingForeignSources.entrySet();
                final Set<Entry<String,Requisition>>   requisitions   = m_pendingRequisitions.entrySet();

                LogUtils.debugf(this, "* %d pending foreign sources", m_pendingForeignSources.size());
                LogUtils.debugf(this, "* %d pending requisitions",    m_pendingRequisitions.size());

                for (final Entry<String,ForeignSource> entry : foreignSources) {
                    final String foreignSourceName = entry.getKey();
                    final ForeignSource foreignSource = entry.getValue();

                    if (foreignSource instanceof DeletedForeignSource) {
                        final DeletedForeignSource deletedForeignSource = (DeletedForeignSource)foreignSource;
                        m_repository.delete(deletedForeignSource.getOriginal());
                    } else {
                        m_repository.save(foreignSource);
                    }
                    m_pendingForeignSources.remove(foreignSourceName, foreignSource);
                }

                for (final Entry<String,Requisition> entry : requisitions) {
                    final String foreignSourceName = entry.getKey();
                    final Requisition requisition = entry.getValue();

                    if (requisition instanceof DeletedRequisition) {
                        final DeletedRequisition deletedRequisition = (DeletedRequisition)requisition;
                        m_repository.delete(deletedRequisition.getOriginal());
                    } else {
                        m_repository.save(requisition);
                    }
                    m_pendingRequisitions.remove(foreignSourceName, requisition);
                }

                LogUtils.debugf(this, "finished persisting repository changes");
            } finally {
                ThreadCategory.setPrefix(prefix);
            }
        }
    }

    private static final class DeletedForeignSource extends ForeignSource {
        private static final long serialVersionUID = -1484921681168837826L;
        private final ForeignSource m_foreignSource;

        public DeletedForeignSource(final ForeignSource foreignSource) {
            m_foreignSource = foreignSource;
            setName(foreignSource.getName());
        }

        public ForeignSource getOriginal() {
            return m_foreignSource;
        }
    }

    private static final class DeletedRequisition extends Requisition {
        private static final long serialVersionUID = -19738304185310191L;
        private final Requisition m_requisition;

        public DeletedRequisition(final Requisition requisition) {
            m_requisition = requisition;
            setForeignSource(requisition.getForeignSource());
        }

        public Requisition getOriginal() {
            return m_requisition;
        }
    }

}
