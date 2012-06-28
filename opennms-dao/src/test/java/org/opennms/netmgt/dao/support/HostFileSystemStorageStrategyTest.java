/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.io.File;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.StorageStrategyService;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HostFileSystemStorageStrategyTest {

    @SuppressWarnings("deprecation")
	@Test
    public void testStrategy() throws Exception {
        // Create Mocks
        StorageStrategyService service = EasyMock.createMock(StorageStrategyService.class);
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddressUtils.addr("127.0.0.1"));
        agentConfig.setPort(1161);
        EasyMock.expect(service.getAgentConfig()).andReturn(agentConfig).anyTimes();
        EasyMock.replay(service);

        // Create Strategy
        HostFileSystemStorageStrategy strategy = new HostFileSystemStorageStrategy();
        strategy.setResourceTypeName("hrStorageIndex");
        strategy.setStorageStrategyService(service);

        // Test Resource Name - root file system
        String parentResource = "1";
        MockCollectionResource resource = new MockCollectionResource(parentResource, "1", "hrStorageIndex");
        resource.getAttribtueMap().put("hrStorageDescr", "/");
        String resourceName = strategy.getResourceNameFromIndex(resource);
        Assert.assertEquals("_root_fs", resourceName);

        // Test Resource Name - root file system
        resource.setInstance("8");
        resource.getAttribtueMap().put("hrStorageDescr", "Volumes-iDisk");
        Assert.assertEquals("Volumes-iDisk", strategy.getResourceNameFromIndex(resource));

        // Test RelativePath
        Assert.assertEquals("1" + File.separator + "hrStorageIndex" + File.separator + "_root_fs", strategy.getRelativePathForAttribute(parentResource, resourceName, null));

        EasyMock.verify(service);
    }
}
