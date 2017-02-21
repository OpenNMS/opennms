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

import java.nio.file.Paths;

import org.junit.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.netmgt.collection.api.StorageStrategyService;
import org.opennms.netmgt.model.ResourcePath;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class FrameRelayStorageStrategyTest {
    
    @Test
    public void testStrategy() {
        // Create Mocks
        StorageStrategyService service = EasyMock.createMock(StorageStrategyService.class);
        EasyMock.expect(service.getSnmpInterfaceLabel(1)).andReturn("Se0_0").anyTimes(); // Valid source interface
        EasyMock.expect(service.getSnmpInterfaceLabel(2)).andReturn(null).anyTimes(); // Invalid source interface
        EasyMock.replay(service);

        // Create Strategy
        FrameRelayStorageStrategy strategy = new FrameRelayStorageStrategy();
        strategy.setResourceTypeName("frCircuitIfIndex");
        strategy.setStorageStrategyService(service);
        
        // Test InterfaceName
        ResourcePath parentResource = ResourcePath.get("1");
        Assert.assertEquals("Se0_0", strategy.getInterfaceName(parentResource.getName(), "1"));

        // Test InterfaceName (invalid source interface index);
        Assert.assertEquals("2", strategy.getInterfaceName(parentResource.getName(), "2"));

        // Test Resource Name
        MockCollectionResource resource = new MockCollectionResource(parentResource, "1.100", "frCircuitIfIndex");
        String resourceName = strategy.getResourceNameFromIndex(resource);
        Assert.assertEquals("Se0_0.100", resourceName);

        // Test Resource Name (invalid source interface index)
        resource.setInstance("2.100");
        Assert.assertEquals("2.100", strategy.getResourceNameFromIndex(resource));

        // Test RelativePath
        Assert.assertEquals(ResourcePath.get("1", "frCircuitIfIndex", "Se0_0.100"), strategy.getRelativePathForAttribute(parentResource, resourceName));
        
        EasyMock.verify(service);
    }
}
