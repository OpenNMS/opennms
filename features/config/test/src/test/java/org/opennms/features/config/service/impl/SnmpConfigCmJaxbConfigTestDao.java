package org.opennms.features.config.service.impl;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.callback.DefaultCmJaxbConfigDaoUpdateCallback;
import org.opennms.netmgt.config.snmp.SnmpConfig;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

public class SnmpConfigCmJaxbConfigTestDao extends AbstractCmJaxbConfigDao<SnmpConfig> {
    public static final String CONFIG_NAME = "snmp-config";

    public SnmpConfigCmJaxbConfigTestDao() {
        super(SnmpConfig.class, "SNMP Config");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback() {
        return new DefaultCmJaxbConfigDaoUpdateCallback<>(this);
    }

    @Override
    @PostConstruct
    public void postConstruct() {
        this.addOnReloadedCallback(getDefaultConfigId(), getUpdateCallback());
    }
}
