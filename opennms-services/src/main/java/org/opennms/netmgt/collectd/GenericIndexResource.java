package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;

import org.opennms.netmgt.snmp.SnmpInstId;

public class GenericIndexResource extends CollectionResource {

	private SnmpInstId m_inst;
	private String m_name;

	public GenericIndexResource(ResourceType def, String name, SnmpInstId inst) {
		super(def);
		m_name = name;
		m_inst = inst;
	}

	@Override
	public Collection<AttributeType> getAttributeTypes() {
		return getResourceType().getAttributeTypes();
	}

	@Override
	public CollectionAgent getCollectionAgent() {
		return getResourceType().getAgent();
	}
	
	// XXX should be based on the storageStrategy
	@Override
    protected File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File nodeDir = new File(rrdBaseDir, String.valueOf(getCollectionAgent().getNodeId()));
        File typeDir = new File(nodeDir, m_name);
        File instDir = new File(typeDir, m_inst.toString());
        log().debug("getResourceDir: " + instDir.toString());
        return instDir;
    }

    public String toString() {
        return "Node["+getCollectionAgent().getNodeId() + "]/type[" + m_name + "]/instance[" + m_inst + "]";
    }


	@Override
	protected int getType() {
		return -1;	// XXX is this right?
	}

	@Override
	public boolean shouldPersist(ServiceParameters params) {
		return true;// XXX should be based on the persistanceSelectorStrategy
	}

}
