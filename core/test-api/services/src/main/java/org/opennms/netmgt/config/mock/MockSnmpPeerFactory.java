package org.opennms.netmgt.config.mock;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.api.SnmpConfigDao;

public class MockSnmpPeerFactory extends SnmpPeerFactory {
    public MockSnmpPeerFactory() {
        super();
        this.snmpConfigDao = new SnmpConfigDao() {
            private SnmpConfig snmpConfig;

            {{
                snmpConfig = new SnmpConfig();
                snmpConfig.setVersion("v2c");
                snmpConfig.setReadCommunity("public");
                snmpConfig.setWriteCommunity("private");
                snmpConfig.setTimeout(1800);
                snmpConfig.setRetry(1);
            }}

            @Override
            public SnmpConfig getConfig() {
                return snmpConfig;
            }

            @Override
            public void updateConfig(final SnmpConfig snmpConfig) {
                this.snmpConfig = snmpConfig;
            }
        };
    }
}
