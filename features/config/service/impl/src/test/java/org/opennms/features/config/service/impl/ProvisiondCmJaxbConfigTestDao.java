package org.opennms.features.config.service.impl;

import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;

public class ProvisiondCmJaxbConfigTestDao extends AbstractCmJaxbConfigDao<ProvisiondConfiguration> {
    public static final String CONFIG_NAME = "provisiond";
    public static final String CONFIG_ID = "default";

    public ProvisiondCmJaxbConfigTestDao() {
        super(ProvisiondConfiguration.class, "Provisiond Configuration");
    }
    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return CONFIG_ID;
    }

}
