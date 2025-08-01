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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;

import org.junit.After;
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

    private NodeDao m_nodeDao = mock(NodeDao.class);

    private ResourceDao m_resourceDao = mock(ResourceDao.class);

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_nodeDao);
        verifyNoMoreInteractions(m_resourceDao);
    }

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
