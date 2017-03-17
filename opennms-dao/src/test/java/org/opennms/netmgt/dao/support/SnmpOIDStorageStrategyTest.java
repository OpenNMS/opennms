/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import org.junit.Assert;

import org.junit.Test;
import org.opennms.netmgt.model.ResourcePath;
import org.snmp4j.smi.OID;

/**
 * @author <a href="mailto:roskens@opennms.org">Ronald Roskens</a>
 */
public class SnmpOIDStorageStrategyTest {
    
    @Test
    public void testStrategy() {
        // Create Strategy
        SnmpOIDStorageStrategy strategy = new SnmpOIDStorageStrategy();
        strategy.setResourceTypeName("ltmVSStatName");
        
        OID oid = new OID("47.67.111.109.109.111.110.47.118.115.45.119.119.119.46.101.120.97.109.112.108.101.46.99.111.109");

        ResourcePath parentResource = ResourcePath.get("1");
        
        MockCollectionResource resource = new MockCollectionResource(parentResource, oid.toSubIndex(true).toString(), "ltmVSStatName");
        String resourceName = strategy.getResourceNameFromIndex(resource);
        Assert.assertEquals("Common-vs-www.example.com", resourceName);
    }
}
