package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.springframework.orm.ObjectRetrievalFailureException;

public class GenericIndexResourceType extends ResourceType {
	private String m_name;
//	private String m_persistenceSelectorStrategy;
	private StorageStrategy m_storageStrategy;

    private Map<SnmpInstId, GenericIndexResource> m_resourceMap = new HashMap<SnmpInstId, GenericIndexResource>();


	public GenericIndexResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection, org.opennms.netmgt.config.datacollection.ResourceType resourceType) {
		super(agent, snmpCollection);
		m_name = resourceType.getName();
                instantiatePersistenceSelectorStrategy(resourceType.getPersistenceSelectorStrategy().getClazz());
                instantiateStorageStrategy(resourceType.getStorageStrategy().getClazz());
        }
        
        private void instantiatePersistenceSelectorStrategy(String className) {
            // TODO write me
        }

        private void instantiateStorageStrategy(String className) {
            Class cinst;
            try {
                cinst = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class,
                                                          className,
                                                          "Could not load class",
                                                          e);
            }
            StorageStrategy storageStrategy;
            try {
                storageStrategy = (StorageStrategy) cinst.newInstance();
            } catch (InstantiationException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class,
                                                          className,
                                                          "Could not instantiate",
                                                          e);
            } catch (IllegalAccessException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class,
                                                          className,
                                                          "Could not instantiate",
                                                          e);
            }
            
            storageStrategy.setResourceTypeName(m_name);

	}

	@Override
	public CollectionResource findResource(SnmpInstId inst) {
		if (!m_resourceMap.containsKey(inst)) {
			m_resourceMap.put(inst, new GenericIndexResource(this, getName(), inst));
		}
		return m_resourceMap.get(inst);
	}

        public CollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        // This is here for completeness but it should not get called from here.
        // findResource should be called instead
            log().debug("findAliasedResource: Should not get called from GenericIndexResourceType");
            return null;
        }

	@Override
	public Collection<AttributeType> getAttributeTypes() {
        return getCollection().getAttributeTypes(getAgent(), DataCollectionConfigFactory.ALL_IF_ATTRIBUTES);
	}

	@Override
	public Collection<GenericIndexResource> getResources() {
		return m_resourceMap.values();
	}

	public String getName() {
		return m_name;
	}
        
        public StorageStrategy getStorageStrategy() {
            return m_storageStrategy;
        }
    
}
