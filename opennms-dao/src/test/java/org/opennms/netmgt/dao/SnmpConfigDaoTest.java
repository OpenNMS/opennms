package org.opennms.netmgt.dao;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;


public class SnmpConfigDaoTest extends TestCase {
    
    private SnmpConfigDao m_snmpConfigDao;
    private File m_configFile;
    
    public void setUp() throws Exception {
        
        File dir = new File("target/test-work-dir");
        dir.mkdirs();
        
        m_configFile = File.createTempFile("snmp-config-"+getName()+"-", "xml", dir);
//        m_configFile.deleteOnExit();
        
        FileUtils.writeStringToFile(m_configFile, 
                "<?xml version=\"1.0\"?>" +
                "<snmp-config port=\"9161\" retry=\"1\" timeout=\"2000\"\n" + 
                "             read-community=\"myPublic\" \n" + 
                "             version=\"v1\" \n" + 
                "             max-vars-per-pdu=\"27\"  />");
        
        
        SnmpPeerFactory.setFile(m_configFile);
        
        FactoryBasedSnmpConfigDao factoryBasedSnmpConfigDao = new FactoryBasedSnmpConfigDao();
        factoryBasedSnmpConfigDao.afterPropertiesSet();
        
        m_snmpConfigDao = factoryBasedSnmpConfigDao;
        
    }
    
    private void assertConfig(String addr, int maxVarsPerPdu, int version, String community) throws UnknownHostException {
        assertNotNull(m_snmpConfigDao);

        SnmpAgentConfig config = m_snmpConfigDao.getAgentConfig(InetAddressUtils.addr(addr));
        assertNotNull(config);
        
        assertEquals(addr, InetAddressUtils.str(config.getAddress()));
        assertEquals(maxVarsPerPdu, config.getMaxVarsPerPdu());
        assertEquals(version, config.getVersion());
        assertEquals(community, config.getReadCommunity());
        
    }

    public void testGet() throws Exception {
        assertConfig("192.168.1.3", 27, 1, "myPublic");
    }
    
    public void testUpdateDefaults() throws Exception {
        
        // assert original config
        assertConfig("192.168.1.3", 27, 1, "myPublic");
        
        // update defaults
        SnmpConfiguration defaults = new SnmpConfiguration();
        defaults.setVersion(2);
        defaults.setMaxVarsPerPdu(72);
        defaults.setReadCommunity("newcommunity");
        
        m_snmpConfigDao.saveAsDefaults(defaults);
        
        // assert new config
        assertConfig("192.168.1.3", 72, 2, "newcommunity");
    }
    
    public void testUpdateConfig() throws Exception {

        // assert original config
        assertConfig("192.168.1.3", 27, 1, "myPublic");
        assertConfig("192.168.1.7", 27, 1, "myPublic");

        // update range config
        SnmpAgentConfig agentConfig = m_snmpConfigDao.getAgentConfig(InetAddressUtils.addr("192.168.1.3"));
        agentConfig.setVersion(2);
        agentConfig.setReadCommunity("newcommunity");
        
        m_snmpConfigDao.saveOrUpdate(agentConfig);
        
        // assert original config
        assertConfig("192.168.1.3", 27, 2, "newcommunity");
        assertConfig("192.168.1.7", 27, 1, "myPublic");

    }

}
