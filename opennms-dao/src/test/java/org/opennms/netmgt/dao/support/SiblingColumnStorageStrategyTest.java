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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
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
        service = mock(StorageStrategyService.class);
        SnmpAgentConfig agentConfig = new SnmpAgentConfig(InetAddressUtils.addr("127.0.0.1"));
        agentConfig.setPort(1161);
        when(service.getAgentConfig()).thenReturn(agentConfig);

        // Create Strategy and set for hrStorageTable
        strategy = new SiblingColumnStorageStrategy();
        strategy.setStorageStrategyService(service);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(service);
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
