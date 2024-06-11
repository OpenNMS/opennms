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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiDataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.wmi.Rrd;
import org.opennms.netmgt.config.wmi.WmiAgentConfig;
import org.opennms.netmgt.config.wmi.WmiCollection;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.InetAddrUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class WmiCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    private WmiPeerFactory peerFactory;
    private WmiDataCollectionConfigFactory dataCollectionConfigFactory;
    private DataCollectionConfigDao dataCollectionConfigDao;

    public WmiCollectorComplianceTest() {
        super(WmiCollector.class, true);

        WmiAgentConfig agentConfig = new WmiAgentConfig();
        peerFactory = mock(WmiPeerFactory.class);
        when(peerFactory.getAgentConfig(InetAddrUtils.getLocalHostAddress())).thenReturn(agentConfig);
        WmiPeerFactory.setInstance(peerFactory);

        WmiCollection collection = new WmiCollection();
        collection.setName("default");
        collection.setRrd(new Rrd(1, "RRA:AVERAGE:0.5:1:2016"));
        dataCollectionConfigFactory = mock(WmiDataCollectionConfigFactory.class);
        when(dataCollectionConfigFactory.getWmiCollection(COLLECTION)).thenReturn(collection);
        when(dataCollectionConfigFactory.getRrdRepository(COLLECTION)).thenReturn(new RrdRepository());
        WmiDataCollectionConfigFactory.setInstance(dataCollectionConfigFactory);
        dataCollectionConfigDao = mock(DataCollectionConfigDao.class);
        when(dataCollectionConfigDao.getConfiguredResourceTypes()).thenReturn(Maps.newHashMap());
        DataCollectionConfigFactory.setInstance(dataCollectionConfigDao);
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
    public void beforeMinion() {
        WmiPeerFactory.setInstance(null);
        WmiDataCollectionConfigFactory.setInstance(null);
    }

    @Override
    public void afterMinion() {
        WmiPeerFactory.setInstance(peerFactory);
        WmiDataCollectionConfigFactory.setInstance(dataCollectionConfigFactory);
    }
}
