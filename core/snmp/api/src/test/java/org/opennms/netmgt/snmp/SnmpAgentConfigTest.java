package org.opennms.netmgt.snmp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

public class SnmpAgentConfigTest {

    @Test
    public void testEqualsAndHashCode() {
        SnmpAgentConfig config = new SnmpAgentConfig();
        SnmpAgentConfig config2 = new SnmpAgentConfig();

        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        fillAll(config);
        Assert.assertFalse(config.equals(config2));
        Assert.assertFalse(config.hashCode() == config2.hashCode());

        fillAll(config2);
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());
    }

    /**
     * In #NMS-6860 we had the issue that the {@link SnmpAgentConfig} NRTG
     * uses was not build correctly. The problem was that {@link SnmpAgentConfig#toProtocolConfigString()} did create
     * a wrong formatted string. Therefore {@link SnmpAgentConfig#parseProtocolConfigurationString(String)} created
     * an agent config with wrong values from the wrong formatted string.<br/><br/>
     *
     * This test ensures if a serialized config string is parsed both objects (the original one and the parsed one)
     * are at least equal.
     */
    @Test
    public void testProtocolConfiguration() throws UnknownHostException {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(InetAddress.getByName("127.0.0.1"));
        config.setProxyFor(InetAddress.getByName("127.0.0.1"));
        SnmpAgentConfig  config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        config.setVersion(3);
        config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        config.setAuthPassPhrase(null);
        config.setPrivPassPhrase(null);
        config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        // config2 will have the default PrivPassphrase and such, so these will *not* actually equal each other
        Assert.assertFalse(config.equals(config2));
        Assert.assertFalse(config.hashCode() == config2.hashCode());
        config2.setAuthPassPhrase(null);
        config2.setPrivPassPhrase(null);
        // now they should match
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());

        fillAll(config);
        // toProtocolConfigurationString does not print all set value,
        // it is version dependent, therefore we have to manually
        // reset read and write community strings
        config.setReadCommunity(null);
        config.setWriteCommunity(null);
        config2 = SnmpAgentConfig.parseProtocolConfigurationString(config.toProtocolConfigString());
        config2.setReadCommunity(null);
        config2.setWriteCommunity(null);
        Assert.assertEquals(config, config2);
        Assert.assertEquals(config.hashCode(), config2.hashCode());
    }

    private void fillAll(SnmpAgentConfig config) {
        config.setTimeout(12);
        config.setAuthPassPhrase("some random pass phrase");
        config.setAuthProtocol("some random protocol");
        config.setContextEngineId("some context engine id");
        config.setContextName("some context name");
        config.setEngineId("some engine id");
        config.setEnterpriseId("some enterprise id");
        config.setMaxRepetitions(34);
        config.setMaxRequestSize(56);
        config.setMaxVarsPerPdu(78);
        config.setPort(99);
        config.setPrivPassPhrase("some random private pass phrase");
        config.setPrivProtocol("some random private protocol");
        config.setReadCommunity("read community string");
        config.setWriteCommunity("write community string");
        config.setRetries(17);
        config.setSecurityLevel(3);
        config.setSecurityName("dummy");
        config.setVersion(3);
    }
}
