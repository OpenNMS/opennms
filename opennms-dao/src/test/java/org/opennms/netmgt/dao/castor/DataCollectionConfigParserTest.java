/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao.castor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.test.ConfigurationTestUtils;

import org.springframework.core.io.InputStreamResource;

/**
 * DataCollectionConfigParserTest
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DataCollectionConfigParserTest {

    private DefaultDataCollectionConfigDao dao;

    private String datacollectionDirectory;

    @Before
    public void setUp() {
        InputStream in = null;
        try {
            dao = new DefaultDataCollectionConfigDao();
            File configFile = ConfigurationTestUtils.getFileForConfigFile("datacollection-config.xml");
            File configFolder = new File(configFile.getParentFile(), "datacollection");
            Assert.assertTrue(configFolder.isDirectory());
            datacollectionDirectory = configFolder.getAbsolutePath();
            in = new FileInputStream(configFile);
            dao.setConfigResource(new InputStreamResource(in));
            dao.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void testLoad() throws Exception {
        // Get Current Configuration
        DatacollectionConfig config = dao.getContainer().getObject();
        Assert.assertNotNull(config);

        // Execute Parser
        DataCollectionConfigParser parser = new DataCollectionConfigParser(datacollectionDirectory);
        parser.parse(config);

        // Validate Parser
        DatacollectionGroup globalContainer = parser.getGlobalContainer();
        Assert.assertEquals(77, globalContainer.getResourceTypeCount());
        Assert.assertEquals(126, globalContainer.getSystemDefCount());
        Assert.assertEquals(183, globalContainer.getGroupCount());

        // Validate SNMP Collection
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(53, collection.getResourceTypeCount()); 
        Assert.assertEquals(122, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(139, collection.getGroups().getGroupCount());
    }

}
