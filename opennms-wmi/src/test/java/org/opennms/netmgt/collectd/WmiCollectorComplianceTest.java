/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.WmiDataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.wmi.Rrd;
import org.opennms.netmgt.config.wmi.WmiAgentConfig;
import org.opennms.netmgt.config.wmi.WmiCollection;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.InetAddrUtils;

import com.google.common.collect.ImmutableMap;

public class WmiCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    private WmiPeerFactory peerFactory;
    private WmiDataCollectionConfigFactory dataCollectionConfigFactory;

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
