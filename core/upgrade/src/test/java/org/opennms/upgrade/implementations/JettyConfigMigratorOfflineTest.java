/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.ConfigFileConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The Test Class for JettyConfigMigratorOffline.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class JettyConfigMigratorOfflineTest {

    /** The factory. */
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
    /** The xpath. */
    XPath xpath = XPathFactory.newInstance().newXPath();

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        System.setProperty("opennms.home", "target/home");
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
    }

    /**
     * Test SSL.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSSL() throws Exception {
        File propertiesFile = ConfigFileConstants.getConfigFileByName("opennms.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        properties.setProperty("org.opennms.netmgt.jetty.https-port", "9999");
        properties.store(new FileWriter(propertiesFile), "Updated!");
        JettyConfigMigratorOffline migrator = new JettyConfigMigratorOffline();
        migrator.execute();
        verify(true, false);
    }
    
    /**
     * Test AJP.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAJP() throws Exception {
        File propertiesFile = ConfigFileConstants.getConfigFileByName("opennms.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        properties.setProperty("org.opennms.netmgt.jetty.ajp-port", "9999");
        properties.store(new FileWriter(propertiesFile), "Updated!");
        JettyConfigMigratorOffline migrator = new JettyConfigMigratorOffline();
        migrator.execute();
        verify(false, true);        
    }

    /**
     * Test both.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBoth() throws Exception {
        File propertiesFile = ConfigFileConstants.getConfigFileByName("opennms.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        properties.setProperty("org.opennms.netmgt.jetty.https-port", "9999");
        properties.setProperty("org.opennms.netmgt.jetty.ajp-port", "9999");
        properties.store(new FileWriter(propertiesFile), "Updated!");
        JettyConfigMigratorOffline migrator = new JettyConfigMigratorOffline();
        migrator.execute();
        verify(true, true);        
    }

    /**
     * Test none.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNone() throws Exception {
        JettyConfigMigratorOffline migrator = new JettyConfigMigratorOffline();
        migrator.execute();
        Assert.assertFalse(new File("target/home/etc/jetty.xml").exists());
    }

    /**
     * Verify.
     *
     * @param sslStatus the SSL status
     * @param ajpStatus the AJP status
     * @throws Exception the exception
     */
    private void verify(boolean sslStatus, boolean ajpStatus) throws Exception {
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("target/home/etc/jetty.xml"));
        Node sslNode = (Node) xpath.evaluate("/Configure/Call/Arg/New[@class='org.eclipse.jetty.server.ssl.SslSelectChannelConnector']", doc, XPathConstants.NODE);
        if (sslStatus) {
            Assert.assertNotNull(sslNode);
        } else {
            Assert.assertNull(sslNode);
        }
        Node ajpNode = (Node) xpath.evaluate("/Configure/Call/Arg/New[@class='org.eclipse.jetty.ajp.Ajp13SocketConnector']", doc, XPathConstants.NODE);
        if (ajpStatus) {
            Assert.assertNotNull(ajpNode);
        } else {
            Assert.assertNull(ajpNode);
        }
    }
    
}
