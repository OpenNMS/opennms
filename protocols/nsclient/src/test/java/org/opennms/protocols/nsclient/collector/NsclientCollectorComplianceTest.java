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
package org.opennms.protocols.nsclient.collector;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.datacollction.nsclient.NsclientCollection;
import org.opennms.netmgt.config.datacollction.nsclient.Wpms;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.protocols.nsclient.NSClientAgentConfig;
import org.opennms.protocols.nsclient.config.NSClientDataCollectionConfigFactory;
import org.opennms.protocols.nsclient.config.NSClientPeerFactory;

import com.google.common.collect.ImmutableMap;

public class NsclientCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    private NSClientPeerFactory peerFactory;

    private NSClientDataCollectionConfigFactory dataCollectionConfigFactory;

    public NsclientCollectorComplianceTest() throws UnknownHostException {
        super(NSClientCollector.class, true);

        NSClientAgentConfig agentConfig = new NSClientAgentConfig();
        peerFactory = mock(NSClientPeerFactory.class);
        when(peerFactory.getAgentConfig(InetAddrUtils.getLocalHostAddress())).thenReturn(agentConfig);
        NSClientPeerFactory.setInstance(peerFactory);

        NsclientCollection collection = new NsclientCollection();
        collection.setWpms(new Wpms());
        dataCollectionConfigFactory = mock(NSClientDataCollectionConfigFactory.class);
        when(dataCollectionConfigFactory.getNSClientCollection(COLLECTION)).thenReturn(collection);
        when(dataCollectionConfigFactory.getRrdRepository(COLLECTION)).thenReturn(new RrdRepository());
        NSClientDataCollectionConfigFactory.setInstance(dataCollectionConfigFactory);
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
        NSClientPeerFactory.setInstance(null);
        NSClientDataCollectionConfigFactory.setInstance(null);
    }

    @Override
    public void afterMinion() {
        NSClientPeerFactory.setInstance(peerFactory);
        NSClientDataCollectionConfigFactory.setInstance(dataCollectionConfigFactory);
    }
}
