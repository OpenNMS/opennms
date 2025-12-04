package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.config.snmp.SnmpConfig;

import java.io.IOException;

public interface SnmpConfigDao {
    SnmpConfig getConfig();
    void updateConfig(SnmpConfig config);
}
