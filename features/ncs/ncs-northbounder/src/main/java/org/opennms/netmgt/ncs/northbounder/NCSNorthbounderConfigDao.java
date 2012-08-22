package org.opennms.netmgt.ncs.northbounder;

import org.opennms.core.xml.AbstractJaxbConfigDao;

public class NCSNorthbounderConfigDao extends AbstractJaxbConfigDao<NCSNorthbounderConfig, NCSNorthbounderConfig> {

	public NCSNorthbounderConfigDao() {
		super(NCSNorthbounderConfig.class,"Config for NCS Northbounder");
	}

	@Override
	protected NCSNorthbounderConfig translateConfig(NCSNorthbounderConfig config) {
		return config;
	}
	
	public NCSNorthbounderConfig getConfig() {
		return getContainer().getObject();
	}

}
