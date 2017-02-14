package org.opennms.netmgt.collection.persistence.rrd;

import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;

public class RrdPersisterFactory implements PersisterFactory {

    private RrdStrategy<?, ?> m_rrdStrategy;

    private ResourceStorageDao m_resourceStorageDao;

    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return createPersister(params, repository, 0, false, false, false);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean forceStoreByGroup, boolean dontReorderAttributes) {
        return createPersister(params, repository, 0, dontPersistCounters,
            forceStoreByGroup, dontReorderAttributes);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository, long collectionTime,
            boolean dontPersistCounters, boolean forceStoreByGroup, boolean dontReorderAttributes) {
        if (ResourceTypeUtils.isStoreByGroup() || forceStoreByGroup) {
            return createGroupPersister(params, repository, collectionTime, dontPersistCounters, dontReorderAttributes);
        } else {
            return createOneToOnePersister(params, repository, collectionTime, dontPersistCounters, dontReorderAttributes);
        }
    }

    public Persister createGroupPersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean dontReorderAttributes) {
        return createGroupPersister(params, repository, 0, dontPersistCounters,
            dontReorderAttributes);
    }

    public Persister createGroupPersister(ServiceParameters params, RrdRepository repository,
            long collectionTime, boolean dontPersistCounters,
            boolean dontReorderAttributes) {
        GroupPersister persister = new GroupPersister(params, repository, m_rrdStrategy, m_resourceStorageDao);
        persister.setCollectionTime(collectionTime);
        persister.setIgnorePersist(dontPersistCounters);
        persister.setDontReorderAttributes(dontReorderAttributes);
        return persister;
    }

    public Persister createOneToOnePersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean dontReorderAttributes) {
        return createOneToOnePersister(params, repository, 0,
            dontPersistCounters, dontReorderAttributes);
    }

    public Persister createOneToOnePersister(ServiceParameters params, RrdRepository repository,
            long collectionTime, boolean dontPersistCounters,
            boolean dontReorderAttributes) {
        OneToOnePersister persister = new OneToOnePersister(params, repository, m_rrdStrategy, m_resourceStorageDao);
        persister.setCollectionTime(collectionTime);
        persister.setIgnorePersist(dontPersistCounters);
        persister.setDontReorderAttributes(dontReorderAttributes);
        return persister;
    }

    public RrdStrategy<?, ?> getRrdStrategy() {
        return m_rrdStrategy;
    }

    public void setRrdStrategy(RrdStrategy<?, ?> rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }

    public ResourceStorageDao getResourceStorageDao() {
        return m_resourceStorageDao;
    }

    public void setResourceStorageDao(ResourceStorageDao resourceStorageDao) {
        m_resourceStorageDao = resourceStorageDao;
    }
}

