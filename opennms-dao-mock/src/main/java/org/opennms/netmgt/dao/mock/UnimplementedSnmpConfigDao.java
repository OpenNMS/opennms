package org.opennms.netmgt.dao.mock;

import java.net.InetAddress;

import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;

public class UnimplementedSnmpConfigDao implements SnmpConfigDao {

    @Override
    public SnmpAgentConfig getAgentConfig(InetAddress ipAddress) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveOrUpdate(SnmpAgentConfig config) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public SnmpConfiguration getDefaults() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void saveAsDefaults(SnmpConfiguration defaults) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
