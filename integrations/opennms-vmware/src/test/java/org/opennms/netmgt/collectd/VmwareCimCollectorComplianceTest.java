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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.config.vmware.cim.VmwareCimCollection;
import org.opennms.netmgt.dao.VmwareCimDatacollectionConfigDao;
import org.opennms.netmgt.dao.VmwareConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
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
        OnmsNode node = mock(OnmsNode.class, RETURNS_DEEP_STUBS);
        NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(anyInt())).thenReturn(node);

        when(node.getAssetRecord().getVmwareManagementServer()).thenReturn("mdx");
        when(node.getAssetRecord().getVmwareManagedEntityType()).thenReturn("tsx");
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
                .build();
    }
}
