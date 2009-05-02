/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 18, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public abstract class OpenNMSIntegrationTestCase extends AbstractTransactionalDataSourceSpringContextTests {
    
    public class MockAgentConfiguration {
        
        Resource m_snmpDataResource;
        List<String> m_proxiedAddresses = new ArrayList<String>();

        public Resource getSnmpDataResource() {
            return m_snmpDataResource;
        }
        
        public void setSnmpDataResource(Resource snmpDataResource) {
            m_snmpDataResource = snmpDataResource;
        }

        public Collection<String> getProxiedAddresses() {
            return m_proxiedAddresses;
        }
        
        public void addProxiedAddress(String ipAddr) {
            m_proxiedAddresses.add(ipAddr);
        }

    }


    protected MockDatabase m_db;
    private Properties m_substitions;
    private MockNetwork m_network;
    private MockSnmpAgent m_agent;
    
    private static MockEventIpcManager s_mockEventIpcManager;
    private static boolean s_setupHomeDir = false;

    public MockEventIpcManager getMockEventIpcManager() {
        return s_mockEventIpcManager;
    }


    protected void setupOpenNMSHomeDir() throws Exception {
        
        File etcSourceDir = ConfigurationTestUtils.getDaemonEtcDirectory();

        File currentDir = new File(System.getProperty("user.dir"));
        File targetDir = new File(currentDir, "target");
        File itTestDir = new File(targetDir, "integration-tests");
        File homeDir = new File(itTestDir, getClass().getName());
        
        if (!homeDir.exists()) {
            assertTrue("Unable to create tmp homeDir "+homeDir, homeDir.mkdirs());
        }
        
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        final File etcDestDir = new File(homeDir, "etc");
        File shareDir = new File(homeDir, "share");
        File rrdDir = new File(shareDir, "rrd");
        
        if (!rrdDir.exists()) {
            assertTrue("Unable to create rrd base dir", rrdDir.mkdirs());
        }
        
        final File createSql = new File(etcDestDir, "create.sql");

        m_db = new MockDatabase(false) {

            @Override
            protected String getCreateSqlLocation() {
                return createSql.getAbsolutePath();
            }

            @Override
            protected String getStoredProcDirectory() {
                return etcDestDir.getAbsolutePath();
            }
            
        };
        
        m_substitions = new Properties(System.getProperties());
        m_substitions.setProperty("install.database.driver", m_db.getDriver());
        m_substitions.setProperty("install.database.url", m_db.getUrl());
        m_substitions.setProperty("install.database.name", m_db.getTestDatabase());
        m_substitions.setProperty("install.database.user", "opennms");
        m_substitions.setProperty("install.database.password", "opennms");
        m_substitions.setProperty("install.share.dir", shareDir.getAbsolutePath());
        m_substitions.setProperty("install.rrdtool.bin", "/usr/local/bin/rrdtool");

        copyDirectory(etcSourceDir, etcDestDir);
        
        m_db.create();
        
        Properties opennmsProperties = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(new File(etcDestDir, "opennms.properties"));
            opennmsProperties.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }

        System.getProperties().putAll(opennmsProperties);
        
        // EventIpcManager is not self initializing yet
        
        if (s_mockEventIpcManager == null) {
            s_mockEventIpcManager = new MockEventIpcManager();
            
            EventIpcManagerFactory.setIpcManager(s_mockEventIpcManager);
        } else {
            s_mockEventIpcManager.reset();
        }
        
        m_network = createMockNetwork();
        
        populateDatabase();
        
        MockAgentConfiguration mockAgentConfig = getMockAgentConfiguration();
        if (mockAgentConfig != null) {

            String localhost = getLocalHostAddress();
            m_agent = MockSnmpAgent.createAgentAndRun(mockAgentConfig.getSnmpDataResource(), localhost + "/9161");

            File snmpConfigFile = new File(etcDestDir, "snmp-config.xml");
            SnmpConfig config = readSnmpConfig(snmpConfigFile);
            Definition def = new Definition();
            def.setPort(9161);
            def.setVersion("v2c");
            def.setReadCommunity("public");
            def.setProxyHost(localhost);

            for(String ipAddr : mockAgentConfig.getProxiedAddresses()) {
                def.addSpecific(ipAddr);
            }
            
            config.addDefinition(def);
            
            writeSnmpConfig(config, snmpConfigFile);
        }
        

    }


    private void writeSnmpConfig(SnmpConfig config, File snmpConfigFile) throws IOException, MarshalException, ValidationException {
        Writer cfgOut = null;
        try {
            cfgOut = new FileWriter(snmpConfigFile);
            Marshaller.marshal(config, cfgOut);
        } finally {
            IOUtils.closeQuietly(cfgOut);
        }
    }


    private SnmpConfig readSnmpConfig(File snmpConfig) throws IOException, FileNotFoundException, MarshalException, ValidationException {
        return CastorUtils.unmarshal(SnmpConfig.class, new FileSystemResource(snmpConfig));
    }
    
    protected String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            fail("Exception getting localhost");
        }
        
        return null;
    }


    protected MockAgentConfiguration getMockAgentConfiguration() {
        return null;
    }


    protected MockNetwork createMockNetwork() {
        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "Router");
        network.addInterface("192.168.1.1");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addInterface("192.168.1.2");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addNode(2, "Server");
        network.addInterface("192.168.1.3");
        network.addService("ICMP");
        network.addService("HTTP");
        network.addNode(3, "Firewall");
        network.addInterface("192.168.1.4");
        network.addService("SMTP");
        network.addService("HTTP");
        network.addInterface("192.168.1.5");
        network.addService("SMTP");
        network.addService("HTTP");
        return network;
    }
    
    protected void populateDatabase() throws Exception {
        m_db.populate(m_network);
    }

    private void copyDirectory(File srcDir, File destDir) throws IOException {
        assertNotNull(srcDir);
        assertNotNull(destDir);
        assertTrue(srcDir.exists());
        if (destDir.exists()) {
            assertTrue("Expected destDir to be a directory: "+destDir, destDir.isDirectory());
        } 
        
        doCopyDirectory(srcDir, destDir);
    }

    private void doCopyDirectory(File srcDir, File destDir) throws IOException {
        if (!destDir.exists()) {
            assertTrue("Unable to create directory "+destDir, destDir.mkdirs());
        }
        
        for(File srcFile : srcDir.listFiles()) {
            File destFile = new File(destDir, srcFile.getName());
            if (srcFile.isDirectory()) {
                doCopyDirectory(srcFile, destFile);
            } else {
                doCopyFile(srcFile, destFile);
            }
        }
    }

    private void doCopyFile(File srcFile, File destFile) throws IOException {
        String contents = readFileContents(srcFile);
        contents = preprocessConfigContents(srcFile, contents);
        String results = PropertiesUtils.substitute(contents, m_substitions, "${", "}");
        results = postprocessConfigContents(destFile, results);
        writeFileContents(results, destFile);
    }
    
    protected String postprocessConfigContents(File destFile, String results) {
        return results;
    }


    protected String preprocessConfigContents(File srcFile, String contents) {
        return contents;
    }


    private void writeFileContents(String results, File destFile) throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriter(destFile);
            IOUtils.write(results, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private String readFileContents(File srcFile) throws IOException {
        Reader reader = null;
        try {
            reader = new FileReader(srcFile);
            return IOUtils.toString(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public void runBare() throws Throwable {
        try {
            if (!s_setupHomeDir) {
                setupOpenNMSHomeDir();
                s_setupHomeDir = true;
            }
            super.runBare();
        } finally {
            stopAgent();
        }
    }


    private void stopAgent() throws InterruptedException {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
    }


    protected EventAnticipator getEventAnticipator() {
        return getMockEventIpcManager().getEventAnticipator();
    }
    
    
    
    

}
