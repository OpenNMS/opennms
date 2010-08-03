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
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.core.io.InputStreamResource;

/**
 * DefaultDataCollectionConfigDaoTest
 *
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultDataCollectionConfigDaoTest {
    
    @Test
    public void testConfiguration() throws Exception {
        DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
        File etcFolder = ConfigurationTestUtils.getDaemonEtcDirectory();
        File configFile = new File(etcFolder, "datacollection-config.xml");
        File configFolder = new File(etcFolder, "datacollection");
        dao.setConfigDirectory(configFolder.getAbsolutePath());
        dao.setConfigResource(new InputStreamResource(new FileInputStream(configFile)));
        dao.afterPropertiesSet();
        
        // Test Storage Flag
        Assert.assertEquals("select", dao.getSnmpStorageFlag("default"));
        
        // Test MIB Objects
        List<MibObject> mibObjects = dao.getMibObjectList("default", ".1.3.6.1.4.1.8072.3.2.255", "127.0.0.1", -1);
        Assert.assertNotNull(mibObjects);
        Assert.assertEquals(70, mibObjects.size());

        // Test Resource Types
        Map<String,ResourceType> resourceTypes = dao.getConfiguredResourceTypes();
        Assert.assertNotNull(resourceTypes);
        Assert.assertEquals(53, resourceTypes.size()); // Original=69, New=53 => Unused=16
        
        // Test Repository
        RrdRepository repository = dao.getRrdRepository("default");
        Assert.assertNotNull(repository);
        Assert.assertEquals(300, repository.getStep());
        
        // Test Step
        Assert.assertEquals(repository.getStep(), dao.getStep("default"));

        // Test RRA List
        List<String> rras = dao.getRRAList("default");
        Assert.assertEquals(repository.getRraList().size(), rras.size());
    }

}
