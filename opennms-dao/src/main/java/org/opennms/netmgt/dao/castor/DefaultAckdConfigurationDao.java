package org.opennms.netmgt.dao.castor;

import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.dao.AckdConfigurationDao;

public class DefaultAckdConfigurationDao extends AbstractCastorConfigDao<AckdConfiguration, AckdConfiguration> implements AckdConfigurationDao {

    public DefaultAckdConfigurationDao() {
        super(AckdConfiguration.class, "Ackd Configuration");
    }
    
    public AckdConfiguration getConfig() {
        return getContainer().getObject();
    }

    @Override
    public AckdConfiguration translateConfig(AckdConfiguration castorConfig) {
        return castorConfig;
    }

}
