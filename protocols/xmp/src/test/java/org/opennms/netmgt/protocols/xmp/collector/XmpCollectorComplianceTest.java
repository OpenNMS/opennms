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

package org.opennms.netmgt.protocols.xmp.collector;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;
import org.opennms.netmgt.config.xmpDataCollection.Groups;
import org.opennms.netmgt.config.xmpDataCollection.XmpCollection;
import org.opennms.netmgt.protocols.xmp.config.XmpAgentConfig;
import org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory;
import org.opennms.netmgt.protocols.xmp.config.XmpPeerFactory;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.InetAddrUtils;

import com.google.common.collect.ImmutableMap;

public class XmpCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    public XmpCollectorComplianceTest() {
        super(XmpCollector.class, false);
        
        XmpAgentConfig agentConfig = new XmpAgentConfig();
        XmpPeerFactory xmpPeerFactory = mock(XmpPeerFactory.class);
        when(xmpPeerFactory.getAgentConfig(InetAddrUtils.getLocalHostAddress())).thenReturn(agentConfig);

        XmpPeerFactory.setInstance(xmpPeerFactory);

        XmpConfig config = new XmpConfig();
        XmpConfigFactory xmpConfigFactory = mock(XmpConfigFactory.class);
        when(xmpConfigFactory.getXmpConfig()).thenReturn(config);
        XmpConfigFactory.setInstance(xmpConfigFactory);

        XmpCollection collection = new XmpCollection();
        collection.setGroups(new Groups());
        XmpCollectionFactory xmpCollectionFactory = mock(XmpCollectionFactory.class);
        when(xmpCollectionFactory.getXmpCollection(COLLECTION)).thenReturn(collection);
        when(xmpCollectionFactory.getRrdRepository(COLLECTION)).thenReturn(new RrdRepository());
        XmpCollectionFactory.setInstance(xmpCollectionFactory);
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
}
