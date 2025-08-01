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
package org.opennms.netmgt.dao.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.dao.api.AlarmAssociationDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.LegacyOnmsDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public abstract class AbstractMockDao<T, K extends Serializable> implements LegacyOnmsDao<T, K>, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMockDao.class);

    @Autowired
    private ServiceRegistry m_serviceRegistry;
    private Map<K,T> m_entries = Collections.synchronizedMap(new HashMap<K,T>());
    private IpInterfaceDao m_ipInterfaceDao;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private AssetRecordDao m_assetRecordDao;
    private CategoryDao m_categoryDao;
    private MonitoringLocationDao m_locationDao;
    private DistPollerDao m_distPollerDao;
    private MonitoredServiceDao m_monitoredServiceDao;
    private ServiceTypeDao m_serviceTypeDao;
    private AlarmDao m_alarmDao;
    private AlarmAssociationDao m_alarmAssociationDao;
    private EventDao m_eventDao;
    private NodeDao m_nodeDao;

    protected abstract K getId(final T entity);
    protected abstract void generateId(T entity);

    public void afterPropertiesSet() {
        Assert.notNull(m_serviceRegistry);
    }

    protected ServiceRegistry getServiceRegistry() {
        return m_serviceRegistry;
    }

    @Override
    public void lock() {
    }

    @Override
    public void initialize(final Object obj) {
    }

    @Override
    public void flush() {
        // LogUtils.debugf(this, "flush()");
    }

    @Override
    public void clear() {
        /*
        LogUtils.debugf(this, "clear()");
        m_entries.clear();
        */
    }

    @Override
    public int countAll() {
        LOG.trace("countAll()");
        return findAll().size();
    }

    @Override
    public void delete(final T entity) {
        LOG.trace("delete({})", entity);
        m_entries.remove(getId(entity));
    }

    @Override
    public void delete(final K key) {
        delete(get(key));
    }

    @Override
    public List<T> findAll() {
        LOG.trace("findAll()");
        return new ArrayList<T>(m_entries.values());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findMatching(final Criteria criteria) {
        final BeanWrapperCriteriaVisitor visitor = new BeanWrapperCriteriaVisitor(findAll());
        criteria.visit(visitor);
        final Collection<? extends T> matches = (Collection<? extends T>)visitor.getMatches();
        return new ArrayList<T>(matches);
    }

    @Override
    public List<T> findMatching(final OnmsCriteria criteria) {
        LOG.trace("findMatching({})", criteria);
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(final Criteria onmsCrit) {
        LOG.trace("countMatching({})", onmsCrit);
        final List<T> matched = findMatching(onmsCrit);
        return matched == null? 0 : matched.size();
    }

    @Override
    public int countMatching(final OnmsCriteria onmsCrit) {
        LOG.trace("countMatching({})", onmsCrit);
        final List<T> matched = findMatching(onmsCrit);
        return matched == null? 0 : matched.size();
    }

    @Override
    public T get(final K id) {
        T retval = m_entries.get(id);
        if (LOG.isTraceEnabled()) {
            LOG.trace("get({}: {})", retval == null ? "null" : retval.getClass().getSimpleName(), id);
        }
        return retval;
    }

    @Override
    public T load(K id) {
        T retval = m_entries.get(id);
        if (LOG.isTraceEnabled()) {
            LOG.trace("load({}: {})", retval == null ? "null" : retval.getClass().getSimpleName(), id);
        }
        return retval;
    }

    @Override
    public K save(final T entity) {
        if (entity == null) return null;
        K id = getId(entity);
        if (id == null) {
            generateId(entity);
            id = getId(entity);
        }
        if (m_entries.containsKey(id)) {
            LOG.debug("save({}): id already exists: {}", entity, id);
        } else {
            LOG.trace("save({})", entity);
        }
        m_entries.put(id, entity);
        return id;
    }

    @Override
    public void saveOrUpdate(final T entity) {
        if (getId(entity) == null) {
            save(entity);
        } else {
            update(entity);
        }
    }

    @Override
    public void update(final T entity) {
        LOG.trace("update({})", entity);
        final K id = getId(entity);
        final T existingEntity = get(id);
        if (!entity.equals(existingEntity)) {
            LOG.warn("update({}): updates do not match: {}", entity, id);
        }
        m_entries.put(id, entity);
    }


    protected IpInterfaceDao getIpInterfaceDao() {
        if (m_ipInterfaceDao == null) {
            m_ipInterfaceDao = getServiceRegistry().findProvider(IpInterfaceDao.class);
            Assert.notNull(m_ipInterfaceDao);
        }
        return m_ipInterfaceDao;
    }

    protected SnmpInterfaceDao getSnmpInterfaceDao() {
        if (m_snmpInterfaceDao == null) {
            m_snmpInterfaceDao = getServiceRegistry().findProvider(SnmpInterfaceDao.class);
            Assert.notNull(m_snmpInterfaceDao);
        }
        return m_snmpInterfaceDao;
    }
    
    protected AssetRecordDao getAssetRecordDao() {
        if (m_assetRecordDao == null) {
            m_assetRecordDao = getServiceRegistry().findProvider(AssetRecordDao.class);
            Assert.notNull(m_assetRecordDao);
        }
        return m_assetRecordDao;
    }
    
    protected CategoryDao getCategoryDao() {
        if (m_categoryDao == null) {
            m_categoryDao = getServiceRegistry().findProvider(CategoryDao.class);
            Assert.notNull(m_categoryDao);
        }
        return m_categoryDao;
    }

    protected MonitoringLocationDao getMonitoringLocationDao() {
        if (m_locationDao == null) {
            m_locationDao = getServiceRegistry().findProvider(MonitoringLocationDao.class);
            Assert.notNull(m_locationDao);
        }
        return m_locationDao;
    }

    protected DistPollerDao getDistPollerDao() {
        if (m_distPollerDao == null) {
            m_distPollerDao = getServiceRegistry().findProvider(DistPollerDao.class);
            Assert.notNull(m_distPollerDao);
        }
        return m_distPollerDao;
    }

    protected MonitoredServiceDao getMonitoredServiceDao() {
        if (m_monitoredServiceDao == null) {
            m_monitoredServiceDao = getServiceRegistry().findProvider(MonitoredServiceDao.class);
            Assert.notNull(m_monitoredServiceDao);
        }
        return m_monitoredServiceDao;
    }

    protected ServiceTypeDao getServiceTypeDao() {
        if (m_serviceTypeDao == null) {
            m_serviceTypeDao = getServiceRegistry().findProvider(ServiceTypeDao.class);
            Assert.notNull(m_serviceTypeDao);
        }
        return m_serviceTypeDao;
    }

    protected EventDao getEventDao() {
        if (m_eventDao == null) {
            m_eventDao = getServiceRegistry().findProvider(EventDao.class);
            Assert.notNull(m_eventDao);
        }
        return m_eventDao;
    }

    protected AlarmDao getAlarmDao() {
        if (m_alarmDao == null) {
            m_alarmDao = getServiceRegistry().findProvider(AlarmDao.class);
            Assert.notNull(m_alarmDao);
        }
        return m_alarmDao;
    }

    protected AlarmAssociationDao getAlarmAssociationDao() {
        if (m_alarmAssociationDao == null) {
            m_alarmAssociationDao = getServiceRegistry().findProvider(AlarmAssociationDao.class);
            Assert.notNull(m_alarmAssociationDao);
        }
        return m_alarmAssociationDao;
    }

    protected NodeDao getNodeDao() {
        if (m_nodeDao == null) {
            m_nodeDao = getServiceRegistry().findProvider(NodeDao.class);
            Assert.notNull(m_nodeDao);
        }
        return m_nodeDao;
    }

    public static final class NullEventForwarder implements EventForwarder {
        @Override
        public void sendNow(Event event) { }

        @Override
        public void sendNow(Log eventLog) { }

        @Override
        public void sendNowSync(Event event) { }

        @Override
        public void sendNowSync(Log eventLog) { }
    }
}
