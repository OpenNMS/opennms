/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.dao.jaxb;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.test.ThrowableAnticipator;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * The Class XmlDataCollectionConfigDaoJaxbTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlDataCollectionConfigDaoJaxbTest {

    /**
     * Test after properties set with no configuration set.
     */
    @Test
    public void testAfterPropertiesSetWithNoConfigSet() {
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property configResource must be set and be non-null"));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    /**
     * Test after properties set with bogus file resource.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAfterPropertiesSetWithBogusFileResource() throws Exception {
        Resource resource = new FileSystemResource("/bogus-file");
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();
        dao.setConfigResource(resource);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new MarshallingResourceFailureException(ThrowableAnticipator.IGNORE_MESSAGE));

        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    /**
     * Test after properties set with good configuration file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();

        File xmlCollectionConfig= new File("src/test/resources/", XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE);
        assertTrue(XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE + " is readable", xmlCollectionConfig.canRead());
        InputStream in = new FileInputStream(xmlCollectionConfig);

        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();

        Assert.assertNotNull("xml data collection should not be null", dao.getConfig());
    }

    /**
     * Test after properties set with nested files (external references to XML groups).
     * 
     * @throws Exception the exception
     */
    @Test
    public void testAfterPropertiesSetWithNestedFiles() throws Exception {
        System.setProperty("opennms.home", "src/test/resources");
        XmlDataCollectionConfigDaoJaxb dao = new XmlDataCollectionConfigDaoJaxb();

        File xmlCollectionConfig= new File("src/test/resources/etc", XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE);
        assertTrue(XmlDataCollectionConfig.XML_DATACOLLECTION_CONFIG_FILE + " is readable", xmlCollectionConfig.canRead());
        InputStream in = new FileInputStream(xmlCollectionConfig);

        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        XmlDataCollectionConfig config = dao.getConfig();
        Assert.assertNotNull("xml data collection should not be null", config);
        Assert.assertEquals(2, config.getXmlDataCollections().get(0).getXmlSources().get(0).getXmlGroups().size());
        Assert.assertEquals(5, config.getXmlDataCollections().get(1).getXmlSources().get(0).getXmlGroups().size());
    }

}
