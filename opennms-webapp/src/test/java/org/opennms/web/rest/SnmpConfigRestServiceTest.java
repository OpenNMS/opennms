package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.web.snmpinfo.SnmpInfo;

/*
 * TODO
 * 1. Need to figure it out how to create a Mock for EventProxy to validate events sent by RESTful service
 */

public class SnmpConfigRestServiceTest extends AbstractSpringJerseyRestTestCase {
    
    JAXBContext m_jaxbContext;
    private File m_snmpConfigFile;

    @Override
    public void beforeServletStart() throws Exception {
        
        File dir = new File("target/test-work-dir");
        dir.mkdirs();
        
        m_snmpConfigFile = File.createTempFile("snmp-config-", "xml");
        
        
        FileUtils.writeStringToFile(m_snmpConfigFile, 
                "<?xml version=\"1.0\"?>" +
        		"<snmp-config port=\"9161\" retry=\"1\" timeout=\"2000\"\n" + 
        		"             read-community=\"myPublic\" \n" + 
        		"             version=\"v1\" \n" + 
        		"             max-vars-per-pdu=\"100\"  />");
        
        
        SnmpPeerFactory.setFile(m_snmpConfigFile);
        
        m_jaxbContext = JAXBContext.newInstance(SnmpInfo.class);

    }

    @Test
    public void testGetForUnknownIp() throws Exception {

        String url = "/snmpConfig/1.1.1.1";
        // Testing GET Collection
        
        SnmpInfo config = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);

        assertConfiguration(config, 9161, 1, 2000, "myPublic", "v1");

    }
    
    @Test
    public void testSetNewValue() throws Exception {
        
        String url = "/snmpConfig/1.1.1.1";
        // Testing GET Collection
        
        SnmpInfo config = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
        
        assertConfiguration(config, 9161, 1, 2000, "myPublic", "v1");

        config.setVersion("v2c");
        config.setTimeout(1000);
        config.setCommunity("new");
        
        putXmlObject(m_jaxbContext, url, 200, config);
        
        
        SnmpInfo newConfig = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
        
        assertConfiguration(newConfig, 9161, 1, 1000, "new", "v2c");
        
        dumpConfig();
        
        
    }
    
    private void dumpConfig() throws Exception {
        IOUtils.copy(new FileInputStream(m_snmpConfigFile), System.out);
    }
    
    private void assertConfiguration(SnmpInfo config, int port, int retries, int timeout, String commString, String version) {
        assertNotNull(config);
        assertEquals(port, config.getPort());
        assertEquals(retries, config.getRetries());
        assertEquals(timeout, config.getTimeout());
        assertEquals(commString, config.getCommunity());
        assertEquals(version, config.getVersion());
                
    }
    
}
