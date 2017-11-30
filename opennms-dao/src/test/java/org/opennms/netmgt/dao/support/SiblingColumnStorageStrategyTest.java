/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.StorageStrategyService;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SiblingColumnStorageStrategyTest {

    private StorageStrategyService service;
    private SiblingColumnStorageStrategy strategy;

    @Before
    public void setUp() throws Exception {
        // Create Mocks
        service = EasyMock.createMock(StorageStrategyService.class);
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddressUtils.addr("127.0.0.1"));
        agentConfig.setPort(1161);
        EasyMock.expect(service.getAgentConfig()).andReturn(agentConfig).anyTimes();
        EasyMock.replay(service);

        // Create Strategy and set for hrStorageTable
        strategy = new SiblingColumnStorageStrategy();
        strategy.setStorageStrategyService(service);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(service);
    }

    @Test
    public void testStrategy() throws Exception {
        strategy.setResourceTypeName("hrStorageIndex");

        // Create parameters for the strategy -- hrStorageTable
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(createParameter("sibling-column-name", "hrStorageDescr"));
        params.add(createParameter("replace-first", "s/^-$/_root_fs/"));
        params.add(createParameter("replace-first", "s/^-//"));
        params.add(createParameter("replace-all", "s/\\s//"));
        params.add(createParameter("replace-all", "s/:\\\\.*//"));

        // Set the list of parameters into the strategy -- hrStorageTable
        strategy.setParameters(params);

        // Test Resource Name - root file system (hrStorageTable)
        ResourcePath parentResource = ResourcePath.get("1");
        MockCollectionResource resource = new MockCollectionResource(parentResource, "1", "hrStorageIndex");
        resource.getAttributeMap().put("hrStorageDescr", "/");
        String resourceName = strategy.getResourceNameFromIndex(resource);
        Assert.assertEquals("_root_fs", resourceName);

        // Test Resource Name - /Volumes/iDisk file system (hrStorageTable)
        resource.setInstance("8");
        resource.getAttributeMap().put("hrStorageDescr", "Volumes-iDisk");
        Assert.assertEquals("Volumes-iDisk", strategy.getResourceNameFromIndex(resource));

        // Test RelativePath - hrStorageTable
        Assert.assertEquals(ResourcePath.get("1", "hrStorageIndex", "_root_fs"), strategy.getRelativePathForAttribute(parentResource, resourceName));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadParameters() throws Exception {
        strategy.setResourceTypeName("hrStorageIndex");

        // Create parameters for the strategy -- hrStorageTable
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(createParameter("sibling-column-oid", ".1.3.6.1.2.1.25.2.3.1.3"));
        params.add(createParameter("replace-first", "s/^-$/_root_fs/"));
        params.add(createParameter("replace-first", "s/^-//"));
        params.add(createParameter("replace-all", "s/\\s//"));
        params.add(createParameter("replace-all", "s/:\\\\.*//"));

        // Set the list of parameters into the strategy -- hrStorageTable
        strategy.setParameters(params);
    }

    @Test
    public void testMatchIndex() throws Exception {
        strategy.setResourceTypeName("macIndex");

        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(createParameter("sibling-column-name", "_index"));
        params.add(createParameter("replace-first", "s/^(([\\d]{1,3}\\.){8,8}).*$/$1/"));
        params.add(createParameter("replace-first", "s/\\.$//"));

        strategy.setParameters(params);

        ResourcePath parentResource = ResourcePath.get("1");
        MockCollectionResource resource = new MockCollectionResource(parentResource, "0.132.43.51.76.89.2.144.10.1.1.1", "macIndex");
        String resourceName = strategy.getResourceNameFromIndex(resource);
        Assert.assertEquals("0.132.43.51.76.89.2.144", resourceName);
    }

    private Parameter createParameter(String key, String value) {
        Parameter p = new Parameter();
        p.setKey(key);
        p.setValue(value);
        return p;
    }
}
