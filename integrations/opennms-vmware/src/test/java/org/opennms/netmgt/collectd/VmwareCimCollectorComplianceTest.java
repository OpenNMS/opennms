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
package org.opennms.netmgt.collectd;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.config.vmware.cim.VmwareCimCollection;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.dao.vmware.VmwareCimDatacollectionConfigDao;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.vmware.VmwareImporter;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.InetAddrUtils;

import com.google.common.collect.ImmutableMap;

public class VmwareCimCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    public VmwareCimCollectorComplianceTest() {
        super(VmwareCimCollector.class, true);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
            .put("collection", COLLECTION)
            .build();
    }

    @Override
    public Map<String, Object> getRequiredBeans() {
        final OnmsNode node = mock(OnmsNode.class, RETURNS_DEEP_STUBS);
        final NodeDao nodeDao = mock(NodeDao.class);
        final MockTransactionTemplate mockTransactionTemplate = new MockTransactionTemplate();
        mockTransactionTemplate.afterPropertiesSet();
        when(nodeDao.get(anyInt())).thenReturn(node);

        when(node.findMetaDataForContextAndKey(VmwareImporter.METADATA_CONTEXT, VmwareImporter.METADATA_MANAGEMENT_SERVER)).thenReturn(Optional.of(new OnmsMetaData(VmwareImporter.METADATA_CONTEXT, "", "mdx")));
        when(node.findMetaDataForContextAndKey(VmwareImporter.METADATA_CONTEXT, VmwareImporter.METADATA_MANAGED_ENTITY_TYPE)).thenReturn(Optional.of(new OnmsMetaData(VmwareImporter.METADATA_CONTEXT, "", "tsx")));
        when(node.getForeignId()).thenReturn("rsx");

        VmwareCimCollection collection = new VmwareCimCollection();
        VmwareCimDatacollectionConfigDao vmwareCimDatacollectionConfigDao = mock(VmwareCimDatacollectionConfigDao.class);
        when(vmwareCimDatacollectionConfigDao.getVmwareCimCollection(COLLECTION)).thenReturn(collection);
        when(vmwareCimDatacollectionConfigDao.getRrdRepository(COLLECTION)).thenReturn(new RrdRepository());

        VmwareServer vmwareServer = new VmwareServer();
        vmwareServer.setHostname(InetAddrUtils.getLocalHostAddress().getCanonicalHostName());
        Map<String, VmwareServer> serverMap = new ImmutableMap.Builder<String, VmwareServer>()
            .put("mdx", vmwareServer)
            .build();

        VmwareConfigDao vmwareConfigDao = mock(VmwareConfigDao.class);
        when(vmwareConfigDao.getServerMap()).thenReturn(serverMap);

        return new ImmutableMap.Builder<String, Object>()
                .put("nodeDao", nodeDao)
                .put("vmwareCimDatacollectionConfigDao", vmwareCimDatacollectionConfigDao)
                .put("vmwareConfigDao", vmwareConfigDao)
                .put("transactionTemplate", mockTransactionTemplate)
                .build();
    }
}
