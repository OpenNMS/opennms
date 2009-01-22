package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;

/*
 * TODO
 * 1. Need to figure it out how to create a Mock for EventProxy to validate events sent by RESTful service
 */
public class SnmpConfigRestServiceTest extends AbstractSpringJerseyRestTestCase {
    
    JAXBContext m_jaxbContext;

    @Override
    public void beforeServletStart() throws Exception {
        
        File dir = new File("target/test-work-dir");
        dir.mkdirs();
        
        File snmpConfigFile = File.createTempFile("snmp-config-", "xml");
        
        
        FileUtils.writeStringToFile(snmpConfigFile, 
                "<?xml version=\"1.0\"?>" +
        		"<snmp-config port=\"9161\" retry=\"1\" timeout=\"2000\"\n" + 
        		"             read-community=\"myPublic\" \n" + 
        		"             version=\"v1\" \n" + 
        		"             max-vars-per-pdu=\"100\"  />");
        
        
        SnmpPeerFactory.setFile(snmpConfigFile);
        
        m_jaxbContext = JAXBContext.newInstance(SnmpConfiguration.class, SnmpAgentConfig.class);

    }

    @Test
    public void testGetDefaults() throws Exception {

        String url = "/snmpConfiguration/defaults";
        // Testing GET Collection
        
        SnmpConfiguration config = getXmlObject(m_jaxbContext, url, 200, SnmpConfiguration.class);

        assertConfiguration(config, 9161, 1, 2000, "myPublic", 100, SnmpConfiguration.VERSION1);

    }
    
    @Test
    public void testSetDefaults() throws Exception {
        
        String url = "/snmpConfiguration/defaults";
        // Testing GET Collection
        
        SnmpConfiguration config = getXmlObject(m_jaxbContext, url, 200, SnmpConfiguration.class);
        
        assertConfiguration(config, 9161, 1, 2000, "myPublic", 100, SnmpConfiguration.VERSION1);

        config.setVersion(SnmpConfiguration.VERSION2C);
        config.setTimeout(1000);
        config.setReadCommunity("new");
        
        putXmlObject(m_jaxbContext, url, 200, config);
        
        
        SnmpConfiguration defaults = getXmlObject(m_jaxbContext, url, 200, SnmpConfiguration.class);
        
        assertConfiguration(defaults, 9161, 1, 1000, "new", 100, SnmpConfiguration.VERSION2C);
        
        
    }
    
    private void assertConfiguration(SnmpConfiguration config, int port, int retries, int timeout, String commString, int maxVarsPerPdu, int version) {
        assertNotNull(config);
        assertEquals(port, config.getPort());
        assertEquals(retries, config.getRetries());
        assertEquals(timeout, config.getTimeout());
        assertEquals(commString, config.getReadCommunity());
        assertEquals(maxVarsPerPdu, config.getMaxVarsPerPdu());
        assertEquals(version, config.getVersion());
                
    }
    
    private void assertAgentConfig(SnmpAgentConfig config, String ipAddr, int port, int retries, int timeout, String commString, int maxVarsPerPdu, int version) {
        assertConfiguration(config, port, retries, timeout, commString, maxVarsPerPdu, version);
        assertEquals(ipAddr, config.getAddress().getHostAddress());
    }
    
    @Test
    public void testUpdateConfig() throws Exception {

        assertAgentConfig(getAgentConfig("192.168.1.3"), "192.168.1.3", 9161, 1, 2000, "myPublic", 100, SnmpConfiguration.VERSION1);
        assertAgentConfig(getAgentConfig("192.168.1.7"), "192.168.1.7", 9161, 1, 2000, "myPublic", 100, SnmpConfiguration.VERSION1);
        
        
        SnmpAgentConfig dot3config = getAgentConfig("192.168.1.3");
        dot3config.setReadCommunity("new");
        dot3config.setTimeout(1000);
        dot3config.setVersion(SnmpConfiguration.VERSION2C);
        
        setAgentConfig("192.168.1.3", dot3config);
        		
        assertAgentConfig(getAgentConfig("192.168.1.3"), "192.168.1.3", 9161, 1, 1000, "new", 100, SnmpConfiguration.VERSION2C);
        assertAgentConfig(getAgentConfig("192.168.1.7"), "192.168.1.7", 9161, 1, 2000, "myPublic", 100, SnmpConfiguration.VERSION1);

    }
    
    public SnmpAgentConfig getAgentConfig(String ipAddr) throws Exception {
        return getXmlObject(m_jaxbContext, "/snmpConfiguration/"+ipAddr, 200, SnmpAgentConfig.class);
    }
    
    public void setAgentConfig(String ipAddr, SnmpAgentConfig config) throws Exception {
        putXmlObject(m_jaxbContext, "/snmpConfiguration/"+ipAddr, 200, config);
    }
    
}
