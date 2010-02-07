//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.config.StorageStrategyService;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.core.io.ClassPathResource;
import org.opennms.netmgt.config.datacollection.Parameter;

/**
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class SiblingColumnStorageStrategyTest {
    
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

        // Create Strategy and set for hrStorageTable
        SiblingColumnStorageStrategy strategy = new SiblingColumnStorageStrategy();
        strategy.setResourceTypeName("hrStorageIndex");
        strategy.setStorageStrategyService(service);
        
        // Create parameters for the strategy -- hrStorageTable
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(createParameter("sibling-column-oid", ".1.3.6.1.2.1.25.2.3.1.3"));
        params.add(createParameter("replace-first", "s/^-$/_root_fs/"));
        params.add(createParameter("replace-first", "s/^-//"));
        params.add(createParameter("replace-all", "s/\\s//"));
        params.add(createParameter("replace-all", "s/:\\\\.*//"));
        
        // Set the list of parameters into the strategy -- hrStorageTable
        strategy.setParameters(params);
        
        // Test Resource Name - root file system (hrStorageTable)
        String parentResource = "1";
        String resourceName = strategy.getResourceNameFromIndex(parentResource, "1");
        Assert.assertEquals("_root_fs", resourceName);

        // Test Resource Name - /Volumes/iDisk file system (hrStorageTable)
        Assert.assertEquals("Volumes-iDisk", strategy.getResourceNameFromIndex(parentResource, "8"));

        // Test RelativePath - hrStorageTable
        Assert.assertEquals("1/hrStorageIndex/_root_fs", strategy.getRelativePathForAttribute(parentResource, resourceName, null));

        snmpAgent.shutDownAndWait();
        EasyMock.verify(service);
    }
    
    private Parameter createParameter(String key, String value) {
        Parameter p = new Parameter();
        p.setKey(key);
        p.setValue(value);
        return p;
    }
}
