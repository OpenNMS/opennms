/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.support;

import static org.mockito.Mockito.*;

import org.junit.Assert;

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
        StorageStrategyService service = mock(StorageStrategyService.class);
        when(service.getSnmpInterfaceLabel(1)).thenReturn("Se0_0"); // Valid source interface
        when(service.getSnmpInterfaceLabel(2)).thenReturn(null); // Invalid source interface

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
        
        verify(service, times(2)).getSnmpInterfaceLabel(1);
        verify(service, times(2)).getSnmpInterfaceLabel(2);
    }
}
