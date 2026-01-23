package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.config.trapd.TrapdConfiguration;

public interface TrapdConfigDao {
    TrapdConfiguration getConfig();
    void updateConfig(TrapdConfiguration config);
}
