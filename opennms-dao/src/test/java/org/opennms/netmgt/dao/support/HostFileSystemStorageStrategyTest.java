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

import java.net.InetAddress;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.StorageStrategyService;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.core.io.ClassPathResource;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HostFileSystemStorageStrategyTest {
    
    @Test
    public void testStrategy() throws Exception {
        // Create Mocks
        StorageStrategyService service = EasyMock.createMock(StorageStrategyService.class);
        SnmpAgentConfig agentConfig = new SnmpAgentConfig();
        agentConfig.setAddress(InetAddress.getByName("127.0.0.1"));
        agentConfig.setPort(1161);
        EasyMock.expect(service.getAgentConfig()).andReturn(agentConfig).anyTimes();
        EasyMock.replay(service);
        
        // Initialize Mock SNMP Agent
        MockSnmpAgent snmpAgent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("/mock-snmp-agent.properties"), "127.0.0.1/1161");

        // Create Strategy
        HostFileSystemStorageStrategy strategy = new HostFileSystemStorageStrategy();
        strategy.setResourceTypeName("hrStorageIndex");
        strategy.setStorageStrategyService(service);
        
        // Test Resource Name - root file system
        String parentResource = "1";
        String resourceName = strategy.getResourceNameFromIndex(parentResource, "1");
        Assert.assertEquals("_root_fs", resourceName);

        // Test Resource Name - root file system
        Assert.assertEquals("Volumes-iDisk", strategy.getResourceNameFromIndex(parentResource, "8"));

        // Test RelativePath
        Assert.assertEquals("1/hrStorageIndex/_root_fs", strategy.getRelativePathForAttribute(parentResource, resourceName, null));
        
        snmpAgent.shutDownAndWait();
        EasyMock.verify(service);
    }
}
