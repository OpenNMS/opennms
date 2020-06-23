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
