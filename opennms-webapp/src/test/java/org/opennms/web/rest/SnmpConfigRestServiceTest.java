package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.xml.bind.JAXBContext;

import org.junit.Test;
import org.opennms.web.snmpinfo.SnmpInfo;

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
        m_jaxbContext = JAXBContext.newInstance(SnmpInfo.class);

    }

    @Test
    public void testGetForUnknownIp() throws Exception {

        String url = "/snmpConfig/1.1.1.1";
        
        SnmpInfo config = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);

        assertConfiguration(config, 9161, 1, 2000, "myPublic", "v1");

    }
    
    @Test
    public void testSetNewValue() throws Exception {
        
        String url = "/snmpConfig/1.1.1.1";
        
        SnmpInfo config = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);

        assertConfiguration(config, 9161, 1, 2000, "myPublic", "v1");

        config.setVersion("v2c");
        config.setTimeout(1000);
        config.setCommunity("new");

        putXmlObject(m_jaxbContext, url, 200, config);

        SnmpInfo newConfig = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
        
        assertConfiguration(newConfig, 9161, 1, 1000, "new", "v2c");
    }
    
    private void assertConfiguration(final SnmpInfo config, final int port, final int retries, final int timeout, final String commString, final String version) {
        assertNotNull(config);
        assertEquals(port, config.getPort());
        assertEquals(retries, config.getRetries());
        assertEquals(timeout, config.getTimeout());
        assertEquals(commString, config.getCommunity());
        assertEquals(version, config.getVersion());
                
    }
    
}
