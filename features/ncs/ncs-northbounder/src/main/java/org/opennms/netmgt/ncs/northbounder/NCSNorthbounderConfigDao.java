package org.opennms.netmgt.ncs.northbounder;

import org.opennms.netmgt.dao.AbstractJaxbConfigDao;

public class NCSNorthbounderConfigDao extends AbstractJaxbConfigDao<NCSNorthbounderConfig, NCSNorthbounderConfig> {

	public NCSNorthbounderConfigDao() {
		super(NCSNorthbounderConfig.class,"Config for NCS Northbounder");
	}

	@Override
	public NCSNorthbounderConfig translateConfig(NCSNorthbounderConfig config) {
		return config;
	}
	
	public NCSNorthbounderConfig getConfig() {
		return getContainer().getObject();
	}

}
