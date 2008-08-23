package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
        
        SnmpConfiguration config = getXmlObject(url, 200, SnmpConfiguration.class);

        assertConfiguration(config, 9161, 1, 2000, "myPublic", 100, SnmpConfiguration.VERSION1);

    }
    
    @Test
    public void testSetDefaults() throws Exception {
        
        String url = "/snmpConfiguration/defaults";
        // Testing GET Collection
        
        SnmpConfiguration config = getXmlObject(url, 200, SnmpConfiguration.class);
        
        assertConfiguration(config, 9161, 1, 2000, "myPublic", 100, SnmpConfiguration.VERSION1);

        config.setVersion(SnmpConfiguration.VERSION2C);
        config.setTimeout(1000);
        config.setReadCommunity("new");
        
        putXmlObject(url, 200, config);
        
        
        SnmpConfiguration defaults = getXmlObject(url, 200, SnmpConfiguration.class);
        
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
        return getXmlObject("/snmpConfiguration/"+ipAddr, 200, SnmpAgentConfig.class);
    }
    
    public void setAgentConfig(String ipAddr, SnmpAgentConfig config) throws Exception {
        putXmlObject("/snmpConfiguration/"+ipAddr, 200, config);
    }
    
    

    private void sendPost(String url, String xml) throws Exception {
        sendData(POST, MediaType.APPLICATION_XML, url, xml);
    }

    private void sendPut(String url, String formData) throws Exception {
        sendData(PUT, MediaType.APPLICATION_FORM_URLENCODED, url, formData);
    }
    
    private void sendData(String requestType, String contentType, String url, String data) throws Exception {
        MockHttpServletRequest request = createRequest(requestType, url);
        request.setContentType(contentType);
        request.setContent(data.getBytes());
        MockHttpServletResponse response = createResponse();        
        dispatch(request, response);
        assertEquals(200, response.getStatus());
    }
    
    private <T> T getXmlObject(String url, int expectedStatus, Class<T> expectedClass) throws Exception {
        MockHttpServletRequest request = createRequest(GET, url);
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(expectedStatus, response.getStatus());
        
        System.err.printf("xml: %s\n", response.getContentAsString());
        
        InputStream in = new ByteArrayInputStream(response.getContentAsByteArray());
        
        Unmarshaller unmarshaller = m_jaxbContext.createUnmarshaller();
        
        T result = expectedClass.cast(unmarshaller.unmarshal(in));
        
        return result;

    }
    
    private void putXmlObject(String url, int expectedStatus, Object object) throws Exception {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        Marshaller marshaller = m_jaxbContext.createMarshaller();
        marshaller.marshal(object, out);
        byte[] content = out.toByteArray();
        

        MockHttpServletRequest request = createRequest(PUT, url);
        request.setContentType(MediaType.APPLICATION_XML);
        request.setContent(content);
        MockHttpServletResponse response = createResponse();        
        dispatch(request, response);
        assertEquals(expectedStatus, response.getStatus());
        
    }

    private String sendRequest(String requestType, String url, int spectedStatus) throws Exception {
        MockHttpServletRequest request = createRequest(requestType, url);
        MockHttpServletResponse response = createResponse();
        dispatch(request, response);
        assertEquals(spectedStatus, response.getStatus());
        String xml = response.getContentAsString();
        if (xml != null)
            System.err.println(xml);
        return xml;
    }
    

}
