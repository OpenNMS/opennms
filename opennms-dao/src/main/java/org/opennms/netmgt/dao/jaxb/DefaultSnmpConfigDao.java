package org.opennms.netmgt.dao.jaxb;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.netmgt.dao.jaxb.callback.ConfigurationReloadEventCallback;
import org.opennms.netmgt.dao.jaxb.callback.SnmpConfigConfigurationValidationCallback;
import org.opennms.netmgt.events.api.EventForwarder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public class DefaultSnmpConfigDao extends AbstractCmJaxbConfigDao<SnmpConfig> implements SnmpConfigDao {
    public static final String CONFIG_NAME = "snmp-config";

    @Autowired
    private EventForwarder eventForwarder;

    public DefaultSnmpConfigDao() {
        super(SnmpConfig.class, "SNMP Config");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public SnmpConfig getConfig() {
        return this.getConfig(this.getDefaultConfigId());
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback(){
        return new ConfigurationReloadEventCallback(eventForwarder);
    }

    @Override
    public Consumer<ConfigUpdateInfo> getValidationCallback(){
        return new SnmpConfigConfigurationValidationCallback();
    }

}
