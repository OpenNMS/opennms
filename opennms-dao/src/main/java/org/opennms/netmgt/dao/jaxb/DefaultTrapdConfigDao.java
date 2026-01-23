package org.opennms.netmgt.dao.jaxb;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.netmgt.dao.jaxb.callback.ConfigurationReloadEventCallback;
import org.opennms.netmgt.events.api.EventForwarder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public class DefaultTrapdConfigDao extends AbstractCmJaxbConfigDao<TrapdConfiguration> implements TrapdConfigDao {
    public static final String CONFIG_NAME = "trapd-config";

    @Autowired
    private EventForwarder eventForwarder;

    public DefaultTrapdConfigDao() {
        super(TrapdConfiguration.class, "Trapd Config");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public TrapdConfiguration getConfig() {
        return this.getConfig(this.getDefaultConfigId());
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback(){
        return new ConfigurationReloadEventCallback(eventForwarder);
    }

    @Override
    public Consumer getValidationCallback() {
        return super.getValidationCallback();
    }
}
