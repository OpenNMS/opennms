package org.opennms.netmgt.dao;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.springframework.core.io.ClassPathResource;


public class SnmpConfigDaoTest extends TestCase {
    
    private SnmpConfigDao m_snmpConfigDao;
    private File m_configFile;
    
    public void setUp() throws Exception {
        
        ClassPathResource srcResource = new ClassPathResource("snmp-config.xml");
        
        File dir = new File("target/test-work-dir");
        dir.mkdirs();
        
        File src = srcResource.getFile();
        
        File dst = File.createTempFile("snmp-config-"+getName()+"-", "xml", dir);
        dst.deleteOnExit();
        
        FileUtils.copyFile(src, dst, true);
        
        m_configFile = dst;
        
        SnmpPeerFactory.setFile(m_configFile);
        
        FactoryBasedSnmpConfigDao factoryBasedSnmpConfigDao = new FactoryBasedSnmpConfigDao();
        factoryBasedSnmpConfigDao.afterPropertiesSet();
        
        m_snmpConfigDao = factoryBasedSnmpConfigDao;
        
    }
    
    private void assertConfig(String addr, int maxVarsPerPdu, int version, String community) throws UnknownHostException {
        assertNotNull(m_snmpConfigDao);

        SnmpAgentConfig config = m_snmpConfigDao.get(InetAddress.getByName(addr));
        assertNotNull(config);
        
        assertEquals(addr, config.getAddress().getHostAddress());
        assertEquals(maxVarsPerPdu, config.getMaxVarsPerPdu());
        assertEquals(version, config.getVersion());
        assertEquals(community, config.getReadCommunity());
        
    }

    public void testGet() throws Exception {
        assertConfig("192.168.1.3", 10, 1, "public");
    }
    
    public void testUpdateDefaults() throws Exception {
        
        // assert original config
        assertConfig("192.168.1.3", 10, 1, "public");
        
        // update defaults
        SnmpConfiguration defaults = new SnmpConfiguration();
        defaults.setVersion(2);
        defaults.setReadCommunity("newcommunity");
        
        m_snmpConfigDao.saveAsDefaults(defaults);
        
        // assert new config
        assertConfig("192.168.1.3", 10, 2, "newcommunity");
    }
    
    public void testUpdateRangeData() throws Exception {

        // assert original config
        assertConfig("192.168.1.3", 10, 1, "public");
        assertConfig("192.168.1.7", 10, 1, "public");

        // update range config
        SnmpConfiguration rangeConfig = new SnmpConfiguration();
        rangeConfig.setVersion(2);
        rangeConfig.setReadCommunity("newcommunity");
        
        m_snmpConfigDao.saveConfigForRange(rangeConfig, InetAddress.getByName("192.168.1.1"), InetAddress.getByName("192.168.1.5"));
        
        // assert original config
        assertConfig("192.168.1.3", 10, 2, "newcommunity");
        assertConfig("192.168.1.7", 10, 1, "public");

    }

}
