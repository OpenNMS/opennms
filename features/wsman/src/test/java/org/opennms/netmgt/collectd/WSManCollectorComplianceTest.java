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

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.wsman.Collection;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.Rrd;
import org.opennms.netmgt.config.wsman.WsmanDatacollectionConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.snmp.InetAddrUtils;

import com.google.common.collect.ImmutableMap;

public class WSManCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    public WSManCollectorComplianceTest() {
        super(WsManCollector.class, true);
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

    public Map<String, Object> getRequiredBeans() {
        OnmsNode node = mock(OnmsNode.class, RETURNS_DEEP_STUBS);
        NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(anyInt())).thenReturn(node);

        Definition agentConfig = new Definition();
        WSManConfigDao wsManConfigDao = mock(WSManConfigDao.class);
        when(wsManConfigDao.getAgentConfig(InetAddrUtils.getLocalHostAddress())).thenReturn(agentConfig);

        WsmanDatacollectionConfig config = new WsmanDatacollectionConfig();
        config.setRrdRepository("target");
        Collection collection = new Collection();
        collection.setRrd(new Rrd());
        WSManDataCollectionConfigDao wsManDataCollectionConfigDao = mock(WSManDataCollectionConfigDao.class);
        when(wsManDataCollectionConfigDao.getCollectionByName(COLLECTION)).thenReturn(collection);
        when(wsManDataCollectionConfigDao.getConfig()).thenReturn(config);

        return new ImmutableMap.Builder<String, Object>()
                .put("nodeDao", nodeDao)
                .put("wsManConfigDao", wsManConfigDao)
                .put("wsManDataCollectionConfigDao", wsManDataCollectionConfigDao)
                .build();
    }
}
