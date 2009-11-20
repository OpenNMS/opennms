//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.support;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.netmgt.config.StorageStrategyService;

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
        String parentResource = "1";
        Assert.assertEquals("Se0_0", strategy.getInterfaceName(parentResource, "1"));

        // Test InterfaceName (invalid source interface index);
        Assert.assertEquals("2", strategy.getInterfaceName(parentResource, "2"));

        // Test Resource Name
        String resourceName = strategy.getResourceNameFromIndex(parentResource, "1.100");
        Assert.assertEquals("Se0_0.100", resourceName);

        // Test Resource Name (invalid source interface index)
        Assert.assertEquals("2.100", strategy.getResourceNameFromIndex(parentResource, "2.100"));

        // Test RelativePath
        Assert.assertEquals("1/frCircuitIfIndex/Se0_0.100", strategy.getRelativePathForAttribute(parentResource, resourceName, null));
        
        EasyMock.verify(service);
    }
}
