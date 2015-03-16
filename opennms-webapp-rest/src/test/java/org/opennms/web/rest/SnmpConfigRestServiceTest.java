/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.svclayer.model.SnmpInfo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * TODO
 * 1. Need to figure it out how to create a Mock for EventProxy to validate events sent by RESTful service
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventProxy.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpConfigRestServiceTest extends AbstractSpringJerseyRestTestCase {

	private static final int DEFAULT_PORT = 9161;
	private static final int DEFAULT_RETRIES = 5;
	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String DEFAULT_COMMUNITY = "myPublic";
	private static final String DEFAULT_VERSION = "v1";
	private static final int DEFAULT_MAX_VARS_PER_PDU = 100;
	private static final int DEFAULT_MAX_REPETITIONS = 3;

	private JAXBContext m_jaxbContext;
	private File m_snmpConfigFile;

	@Override
	protected void beforeServletStart() throws Exception {
		File dir = new File("target/test-work-dir");
		dir.mkdirs();
		setSnmpConfigFile(getSnmpDefaultConfigFileForSnmpV1());
		m_jaxbContext = JAXBContext.newInstance(SnmpInfo.class);
	}
	
	private String getSnmpDefaultConfigFileForSnmpV1() {
		return String.format("<?xml version=\"1.0\"?>"
				+ "<snmp-config port=\"%s\" retry=\"%s\" timeout=\"%s\"\n"
				+ "             read-community=\"%s\" \n" 
				+ "				version=\"%s\" \n"
				+ "             max-vars-per-pdu=\"%s\" max-repetitions=\"%s\"  />", 
				DEFAULT_PORT, DEFAULT_RETRIES, DEFAULT_TIMEOUT, DEFAULT_COMMUNITY, DEFAULT_VERSION, 
				DEFAULT_MAX_VARS_PER_PDU, DEFAULT_MAX_REPETITIONS);
	}
	
	private String getSnmpDefaultConfigFileForSnmpV3() {
		return String.format("<?xml version=\"1.0\"?>"
				+ "<snmp-config port=\"%s\" retry=\"%s\" timeout=\"%s\"\n"
				+ "				version=\"%s\" \n"
				+ "             max-vars-per-pdu=\"%s\" max-repetitions=\"%s\"  />", 
				DEFAULT_PORT, DEFAULT_RETRIES, DEFAULT_TIMEOUT, "v3", 
				DEFAULT_MAX_VARS_PER_PDU, DEFAULT_MAX_REPETITIONS);
	}
	
	private void setSnmpConfigFile(final String snmpConfigContent) throws IOException {
		m_snmpConfigFile = File.createTempFile("snmp-config-", ".xml");
		m_snmpConfigFile.deleteOnExit();
		FileUtils.writeStringToFile(m_snmpConfigFile, snmpConfigContent);
		SnmpPeerFactory.setFile(m_snmpConfigFile);
		SnmpPeerFactory.init();
	}

	/**
	 * Tests if the default values are set correctly according to the default configuration file if 
	 * the SNMP version is v1.
	 * 
	 * @throws Exception
	 */
	@Test
    public void testGetForUnknownIpSnmpV1() throws Exception {
        String url = "/snmpConfig/1.1.1.1";
        
        // Testing GET Collection
        SnmpInfo config = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
        SnmpInfo expectedConfig = createSnmpInfoWithDefaultsForSnmpV1();
        assertConfiguration(expectedConfig, config);
        assertSnmpV3PropertiesHaveNotBeenSet(config);
    }
	
	/**
	 * Tests if the default values are set correctly according to the default configuration file if 
	 * the SNMP version is v3.
	 * 
	 * @throws Exception
	 */
	@Test
    public void testGetForUnknownIpSnmpV3() throws Exception {
        String url = "/snmpConfig/1.1.1.1";
        
        setSnmpConfigFile(getSnmpDefaultConfigFileForSnmpV3());
        
        // Testing GET Collection
        SnmpInfo config = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
		SnmpInfo expectedConfig = createSnmpInfoWithDefaultsForSnmpV3("1.1.1.1");
        assertConfiguration(expectedConfig, config); // check if expected defaults matches actual defaults
        assertSnmpV1PropertiesHaveNotBeenSet(config);
        
    }

	@Test
	public void testSetNewValueForSnmpV2c() throws Exception {
		String url = "/snmpConfig/1.1.1.1";

		// Testing GET Collection
		SnmpInfo config = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
		SnmpInfo expectedConfig = createSnmpInfoWithDefaultsForSnmpV3("1.1.1.1");
		assertConfiguration(expectedConfig, config); // check if expected defaults matches actual defaults
		
		// change values
		config.setAuthPassPhrase("authPassPhrase");
		config.setAuthProtocol("authProtocol");
		config.setReadCommunity("readCommunity");
		config.setWriteCommunity("writeCommunity");
		config.setContextEngineId("contextEngineId");
		config.setContextName("contextName");
		config.setEngineId("engineId");
		config.setEnterpriseId("enterpriseId");
		config.setMaxRepetitions(1000);
		config.setMaxVarsPerPdu(2000);
		config.setPort(3000);
		config.setPrivPassPhrase("privPassPhrase");
		config.setPrivProtocol("privProtocol");
		config.setProxyHost("127.0.0.1");
		config.setRetries(4000);
		config.setSecurityLevel(5000);
		config.setSecurityName("securityName");
		config.setTimeout(6000);
		config.setVersion("v2c");
		config.setMaxRequestSize(7000);

		// store them via REST
		putXmlObject(m_jaxbContext, url, 303, config, "/snmpConfig/1.1.1.1");

		// prepare expected Result
		expectedConfig = new SnmpInfo();
		expectedConfig.setMaxRepetitions(1000);
		expectedConfig.setMaxVarsPerPdu(2000);
		expectedConfig.setPort(3000);
		expectedConfig.setRetries(4000);
		expectedConfig.setTimeout(6000);
		expectedConfig.setVersion("v2c");
		expectedConfig.setMaxRequestSize(7000);
		expectedConfig.setReadCommunity("readCommunity");
		expectedConfig.setWriteCommunity("writeCommunity");
		expectedConfig.setAuthPassPhrase(null);
		expectedConfig.setAuthProtocol(null);
		expectedConfig.setContextEngineId(null);
		expectedConfig.setContextName(null);
		expectedConfig.setEngineId(null);
		expectedConfig.setEnterpriseId(null);
		expectedConfig.setPrivPassPhrase(null);
		expectedConfig.setPrivProtocol(null);
		expectedConfig.setProxyHost("127.0.0.1");
		expectedConfig.setSecurityLevel(null);
		expectedConfig.setSecurityName(null);
		
		// read via REST
		SnmpInfo newConfig = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
		
		// check ...
		assertConfiguration(expectedConfig, newConfig); // ... if Changes were made
		dumpConfig();
	}
	
        @Test
        public void testSetNewValueForSnmpV3() throws Exception {
                String url = "/snmpConfig/1.1.1.1";

                // Testing GET Collection
                SnmpInfo changedConfig = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
                SnmpInfo expectedConfig = createSnmpInfoWithDefaultsForSnmpV3("1.1.1.1");
                assertConfiguration(expectedConfig, changedConfig); // check if expected defaults matches actual defaults
                
                // change values
                changedConfig.setAuthPassPhrase("authPassPhrase");
                changedConfig.setAuthProtocol("authProtocol");
                changedConfig.setReadCommunity("readCommunity");
                changedConfig.setWriteCommunity("writeCommunity");
                changedConfig.setContextEngineId("contextEngineId");
                changedConfig.setContextName("contextName");
                changedConfig.setEngineId("engineId");
                changedConfig.setEnterpriseId("enterpriseId");
                changedConfig.setMaxRepetitions(1000);
                changedConfig.setMaxVarsPerPdu(2000);
                changedConfig.setPort(3000);
                changedConfig.setProxyHost("127.0.0.1");
                changedConfig.setPrivPassPhrase("privPassPhrase");
                changedConfig.setPrivProtocol("privProtocol");
                changedConfig.setRetries(4000);
                changedConfig.setSecurityLevel(5000);
                changedConfig.setSecurityName("securityName");
                changedConfig.setTimeout(6000);
                changedConfig.setVersion("v3");
                changedConfig.setMaxRequestSize(7000);

                // store them via REST
                putXmlObject(m_jaxbContext, url, 303, changedConfig, "/snmpConfig/1.1.1.1");
                
                // prepare expected Result
                expectedConfig = new SnmpInfo();
                expectedConfig.setAuthPassPhrase("authPassPhrase");
                expectedConfig.setAuthProtocol("authProtocol");
                expectedConfig.setContextEngineId("contextEngineId");
                expectedConfig.setContextName("contextName");
                expectedConfig.setEngineId("engineId");
                expectedConfig.setEnterpriseId("enterpriseId");
                expectedConfig.setMaxRepetitions(1000);
                expectedConfig.setMaxVarsPerPdu(2000);
                expectedConfig.setPort(3000);
                expectedConfig.setProxyHost("127.0.0.1");
                expectedConfig.setPrivPassPhrase("privPassPhrase");
                expectedConfig.setPrivProtocol("privProtocol");
                expectedConfig.setRetries(4000);
                expectedConfig.setSecurityLevel(5000);
                expectedConfig.setSecurityName("securityName");
                expectedConfig.setTimeout(6000);
                expectedConfig.setVersion("v3");
                expectedConfig.setMaxRequestSize(7000);
                expectedConfig.setReadCommunity(null);
                expectedConfig.setWriteCommunity(null);
                
                // read via REST
                SnmpInfo newConfig = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
                
                // check ...
                assertConfiguration(expectedConfig, newConfig); // ... if changes were made

                dumpConfig();
        }
        
	@Test
	public void testGetAddresses() throws Exception {
	    String[] addrs = SnmpConfigRestService.getAddresses(null);
	    assertEquals(2, addrs.length);
	    assertEquals(null, addrs[0]);
	    
	    addrs = SnmpConfigRestService.getAddresses("   ");
	    assertEquals(2, addrs.length);
            assertEquals(null, addrs[0]);

            addrs = SnmpConfigRestService.getAddresses("192.168.0.1");
            assertEquals(1, addrs.length);
            assertEquals("192.168.0.1", addrs[0]);

            addrs = SnmpConfigRestService.getAddresses("192.168.0.1-192.168.0.255");
            assertEquals(2, addrs.length);
            assertEquals("192.168.0.1", addrs[0]);
            assertEquals("192.168.0.255", addrs[1]);
	}

        @Test
        public void testSetRanges() throws Exception {
                String url = "/snmpConfig/1.1.1.1";
                String urlRange = "/snmpConfig/1.1.1.1-2.2.2.2";

                // Testing GET Collection
                SnmpInfo changedConfig = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
                SnmpInfo expectedConfig = createSnmpInfoWithDefaultsForSnmpV3("1.1.1.1");
                assertConfiguration(expectedConfig, changedConfig); // check if expected defaults matches actual defaults

                // change values
                changedConfig.setAuthPassPhrase("authPassPhrase");
                changedConfig.setAuthProtocol("authProtocol");
                changedConfig.setReadCommunity("readCommunity");
                changedConfig.setWriteCommunity("writeCommunity");
                changedConfig.setContextEngineId("contextEngineId");
                changedConfig.setContextName("contextName");
                changedConfig.setEngineId("engineId");
                changedConfig.setEnterpriseId("enterpriseId");
                changedConfig.setMaxRepetitions(1000);
                changedConfig.setMaxVarsPerPdu(2000);
                changedConfig.setPort(3000);
                changedConfig.setProxyHost("127.0.0.1");
                changedConfig.setPrivPassPhrase("privPassPhrase");
                changedConfig.setPrivProtocol("privProtocol");
                changedConfig.setRetries(4000);
                changedConfig.setSecurityLevel(5000);
                changedConfig.setSecurityName("securityName");
                changedConfig.setTimeout(6000);
                changedConfig.setVersion("v3");
                changedConfig.setMaxRequestSize(7000);

                // store them via REST
                putXmlObject(m_jaxbContext, urlRange, 303, changedConfig, urlRange);
                
                // prepare expected Result
                expectedConfig = new SnmpInfo();
                expectedConfig.setAuthPassPhrase("authPassPhrase");
                expectedConfig.setAuthProtocol("authProtocol");
                expectedConfig.setContextEngineId("contextEngineId");
                expectedConfig.setContextName("contextName");
                expectedConfig.setEngineId("engineId");
                expectedConfig.setEnterpriseId("enterpriseId");
                expectedConfig.setMaxRepetitions(1000);
                expectedConfig.setMaxVarsPerPdu(2000);
                expectedConfig.setPort(3000);
                expectedConfig.setProxyHost("127.0.0.1");
                expectedConfig.setPrivPassPhrase("privPassPhrase");
                expectedConfig.setPrivProtocol("privProtocol");
                expectedConfig.setRetries(4000);
                expectedConfig.setSecurityLevel(5000);
                expectedConfig.setSecurityName("securityName");
                expectedConfig.setTimeout(6000);
                expectedConfig.setVersion("v3");
                expectedConfig.setMaxRequestSize(7000);
                expectedConfig.setReadCommunity(null);
                expectedConfig.setWriteCommunity(null);
                
                // read via REST
                SnmpInfo newConfig = getXmlObject(m_jaxbContext, url, 200, SnmpInfo.class);
                
                // check ...
                assertConfiguration(expectedConfig, newConfig); // ... if changes were made

                SnmpInfo otherConfig = createSnmpInfoWithDefaultsForSnmpV3("3.3.3.3");
                assertFalse(newConfig.equals(otherConfig));

                expectedConfig = createSnmpInfoWithDefaultsForSnmpV3("1.2.3.4");
                newConfig = getXmlObject(m_jaxbContext, "/snmpConfig/1.2.3.4", 200, SnmpInfo.class);
                assertEquals(expectedConfig, newConfig);

                dumpConfig();
        }
        
	private void dumpConfig() throws Exception {
		IOUtils.copy(new FileInputStream(m_snmpConfigFile), System.out);
	}

	private SnmpInfo createSnmpInfoWithDefaultsForSnmpV3(final String ipAddress) {
		SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ipAddress));
		return new SnmpInfo(agentConfig);
	}
	
	private SnmpInfo createSnmpInfoWithDefaultsForSnmpV1() {
		SnmpAgentConfig defaults = new SnmpAgentConfig();
		SnmpInfo config = new SnmpInfo();
		config.setPort(DEFAULT_PORT);
		config.setRetries(DEFAULT_RETRIES);
		config.setTimeout(DEFAULT_TIMEOUT);
		config.setReadCommunity(DEFAULT_COMMUNITY);
		config.setVersion(DEFAULT_VERSION);
		config.setMaxVarsPerPdu(DEFAULT_MAX_VARS_PER_PDU);
		config.setMaxRepetitions(DEFAULT_MAX_REPETITIONS);
		// defaults are not set via snmp-config.xml, but via defaults in SnmpAgentConfig
		config.setWriteCommunity(defaults.getWriteCommunity());
		config.setMaxRequestSize(defaults.getMaxRequestSize());
		return config;
	}
		
	private void assertConfiguration(SnmpInfo expectedConfig, SnmpInfo actualConfig) {
		assertNotNull(expectedConfig);
		assertNotNull(actualConfig);
		assertEquals(expectedConfig, actualConfig);
	}
	
	/**
	 * Ensures that no SNMP v3 only parameter is set. This is necessary 
	 * so we do not have an invalid SnmpInfo object if the default version is v1 or v2c.
	 * @param config
	 */
	private void assertSnmpV3PropertiesHaveNotBeenSet(SnmpInfo config) {
		assertEquals(false, config.hasSecurityLevel());
		assertEquals(null, config.getSecurityLevel());
		assertEquals(null, config.getSecurityName());
		assertEquals(null, config.getAuthPassPhrase());
		assertEquals(null, config.getAuthProtocol());
		assertEquals(null, config.getEngineId());
		assertEquals(null, config.getContextEngineId());
		assertEquals(null, config.getContextName());
		assertEquals(null, config.getPrivPassPhrase());
		assertEquals(null, config.getPrivProtocol());
		assertEquals(null, config.getEnterpriseId());
	}
	
	/**
	 * Ensures that no SNMP v1 only parameter is set. This is necessary 
	 * so we do not have an invalid SnmpInfo object if the default version is v3.
	 * @param config
	 */
	private void assertSnmpV1PropertiesHaveNotBeenSet(SnmpInfo config) {
		assertEquals(null, config.getReadCommunity()); // community String must be null !
		assertEquals(null, config.getWriteCommunity());
	}
}
