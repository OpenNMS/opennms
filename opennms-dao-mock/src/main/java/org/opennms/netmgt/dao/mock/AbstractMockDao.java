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
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public abstract class AbstractMockDao<T, K extends Serializable> implements OnmsDao<T, K>, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMockDao.class);

    @Autowired
    private ServiceRegistry m_serviceRegistry;
    private Map<K,T> m_entries = Collections.synchronizedMap(new HashMap<K,T>());
    private IpInterfaceDao m_ipInterfaceDao;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private AssetRecordDao m_assetRecordDao;
    private CategoryDao m_categoryDao;
    private DistPollerDao m_distPollerDao;
    private MonitoredServiceDao m_monitoredServiceDao;
    private ServiceTypeDao m_serviceTypeDao;
    private AlarmDao m_alarmDao;
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
        LOG.debug("countAll()");
        return findAll().size();
    }

    @Override
    public void delete(final T entity) {
        LOG.debug("delete({})", entity);
        m_entries.remove(getId(entity));
    }

    @Override
    public void delete(final K key) {
        delete(get(key));
    }

    @Override
    public List<T> findAll() {
        //LogUtils.debugf(this, "findAll()");
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
        LOG.debug("findMatching({})", criteria);
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public int countMatching(final Criteria onmsCrit) {
        LOG.debug("countMatching({})", onmsCrit);
        final List<T> matched = findMatching(onmsCrit);
        return matched == null? 0 : matched.size();
    }

    @Override
    public int countMatching(final OnmsCriteria onmsCrit) {
        LOG.debug("countMatching({})", onmsCrit);
        final List<T> matched = findMatching(onmsCrit);
        return matched == null? 0 : matched.size();
    }

    @Override
    public T get(final K id) {
        LOG.debug("get({})", id);
        return m_entries.get(id);
    }

    @Override
    public T load(K id) {
        LOG.debug("load({})", id);
        return m_entries.get(id);
    }

    @Override
    public void save(final T entity) {
        if (entity == null) return;
        K id = getId(entity);
        if (id == null) {
            generateId(entity);
            id = getId(entity);
        }
        LOG.debug("save({})", entity);
        if (m_entries.containsKey(id)) {
            LOG.debug("save({}): id exists: {}", entity, id);
        }
        m_entries.put(id, entity);
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
        LOG.debug("update({})", entity);
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

    protected NodeDao getNodeDao() {
        if (m_nodeDao == null) {
            m_nodeDao = getServiceRegistry().findProvider(NodeDao.class);
            Assert.notNull(m_nodeDao);
        }
        return m_nodeDao;
    }
}
