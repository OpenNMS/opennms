/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.rrd.NullRrdStrategy;
import org.opennms.netmgt.rrd.RrdStrategy;

public class NodeSnmpResourceTypeTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private NodeDao m_nodeDao = createNiceMock(NodeDao.class);

    private ResourceDao m_resourceDao = createNiceMock(ResourceDao.class);

    @Test
    public void canGetChildByName() throws IOException {
        final RrdStrategy<?, ?> rrdStrategy = new NullRrdStrategy();

        final FilesystemResourceStorageDao resourceStorageDao = new FilesystemResourceStorageDao();
        resourceStorageDao.setRrdDirectory(tempFolder.getRoot());
        resourceStorageDao.setRrdStrategy(rrdStrategy);

        File nodeSnmpFolder = tempFolder.newFolder("snmp", "1");
        File rrd = new File(nodeSnmpFolder, "ds" + rrdStrategy.getDefaultFileExtension());
        rrd.createNewFile();

        final NodeSnmpResourceType nodeSnmpResourceType = new NodeSnmpResourceType(resourceStorageDao);

        final OnmsResource parent = getNodeResource(1);

        final OnmsResource resource = nodeSnmpResourceType.getChildByName(parent, new String(""));
        assertEquals("node[1].nodeSnmp[]", resource.getId().toString());
        assertEquals(parent, resource.getParent());
    }

    private OnmsResource getNodeResource(int nodeId) {
        final NodeResourceType nodeResourceType = new NodeResourceType(m_resourceDao, m_nodeDao);
        final OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        node.setLabel("Node"+ nodeId);
        return nodeResourceType.createResourceForNode(node);
    }
}
