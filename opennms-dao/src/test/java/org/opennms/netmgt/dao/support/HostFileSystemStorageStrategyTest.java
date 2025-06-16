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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.StorageStrategyService;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HostFileSystemStorageStrategyTest {

    @SuppressWarnings("deprecation")
	@Test
    public void testStrategy() throws Exception {
        // Create Mocks
        StorageStrategyService service = mock(StorageStrategyService.class);
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddressUtils.addr("127.0.0.1"));
        agentConfig.setPort(1161);

        when(service.getAgentConfig()).thenReturn(agentConfig);

        // Create Strategy
        HostFileSystemStorageStrategy strategy = new HostFileSystemStorageStrategy();
        strategy.setResourceTypeName("hrStorageIndex");
        strategy.setStorageStrategyService(service);

        // Test Resource Name - root file system
        ResourcePath parentResource = ResourcePath.get("1");
        MockCollectionResource resource = new MockCollectionResource(parentResource, "1", "hrStorageIndex");
        resource.getAttributeMap().put("hrStorageDescr", "/");
        String resourceName = strategy.getResourceNameFromIndex(resource);
        Assert.assertEquals("_root_fs", resourceName);

        // Test Resource Name - root file system
        resource.setInstance("8");
        resource.getAttributeMap().put("hrStorageDescr", "Volumes-iDisk");
        Assert.assertEquals("Volumes-iDisk", strategy.getResourceNameFromIndex(resource));

        // Test RelativePath
        Assert.assertEquals(ResourcePath.get("1", "hrStorageIndex", "_root_fs"), strategy.getRelativePathForAttribute(parentResource, resourceName));

        verifyNoMoreInteractions(service);
    }
}
