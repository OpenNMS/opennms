package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.snmp.SnmpInstId;

public class GenericIndexResourceType extends ResourceType {
	private String m_name;
	private String m_persistenceSelectorStrategy;
	private String m_storageStrategy;

    private Map<SnmpInstId, GenericIndexResource> m_resourceMap = new HashMap<SnmpInstId, GenericIndexResource>();


	public GenericIndexResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection, org.opennms.netmgt.config.datacollection.ResourceType resourceType) {
		super(agent, snmpCollection);
		m_name = resourceType.getName();
		m_persistenceSelectorStrategy = resourceType.getPersistenceSelectorStrategy().getClazz();
		m_storageStrategy = resourceType.getStorageStrategy().getClazz();
	}

	@Override
	public CollectionResource findResource(SnmpInstId inst) {
		if (!m_resourceMap.containsKey(inst)) {
			m_resourceMap.put(inst, new GenericIndexResource(this, getName(), inst));
		}
		return m_resourceMap.get(inst);
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
    
}
