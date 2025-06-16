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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class ResponseTimeResourceTypeTest {

    private static final String NON_DEFAULT_LOCATION_NAME = "!" + MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private FilesystemResourceStorageDao resourceStorageDao = new FilesystemResourceStorageDao();

    private IpInterfaceDao ipInterfaceDao = mock(IpInterfaceDao.class);

    private OnmsNode node = mock(OnmsNode.class);

    private OnmsIpInterface ipInterface = mock(OnmsIpInterface.class);

    private Set<OnmsIpInterface> ipInterfaces = new HashSet<>();

    private ResponseTimeResourceType responseTimeResourceType = new ResponseTimeResourceType(resourceStorageDao, ipInterfaceDao);

    private NodeDao nodeDao = mock(NodeDao.class);

    private ResourceDao resourceDao = mock(ResourceDao.class);

    @Before
    public void setUp() throws IOException {
        resourceStorageDao.setRrdDirectory(tempFolder.getRoot());
        resourceStorageDao.setRrdExtension(".rrd");

        File ifResponseFolder = tempFolder.newFolder(ResourceTypeUtils.RESPONSE_DIRECTORY, "127.0.0.1");
        File http = new File(ifResponseFolder, "http.rrd");
        http.createNewFile();

        ifResponseFolder = tempFolder.newFolder(ResourceTypeUtils.RESPONSE_DIRECTORY, ResourcePath.sanitize(NON_DEFAULT_LOCATION_NAME), "127.0.0.1");
        http = new File(ifResponseFolder, "http.rrd");
        http.createNewFile();

        ipInterfaces.add(ipInterface);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(ipInterfaceDao);
        verifyNoMoreInteractions(node);
        verifyNoMoreInteractions(ipInterface);
        verifyNoMoreInteractions(nodeDao);
        verifyNoMoreInteractions(resourceDao);
    }

    @Test
    public void canGetResourcesForNode() throws IOException {
        when(node.getIpInterfaces()).thenReturn(ipInterfaces);
        when(node.getLocation()).thenReturn(null);
        when(ipInterface.getIpAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        NodeResourceType nodeResourceType = new NodeResourceType(resourceDao, nodeDao);
        OnmsResource nodeResource = new OnmsResource("1", "Node", nodeResourceType, Collections.emptySet(), ResourcePath.get("foo"));
        nodeResource.setEntity(node);
        
        List<OnmsResource> resources = responseTimeResourceType.getResourcesForParent(nodeResource);

        assertEquals(1, resources.size());
        assertEquals("127.0.0.1", resources.get(0).getName());

        verify(ipInterface, atLeastOnce()).getIpAddress();
        verify(node, times(1)).getLocation();
        verify(node, times(1)).getIpInterfaces();
    }

    @Test
    public void canGetResourcesForNodeAtLocation() throws IOException {
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName(NON_DEFAULT_LOCATION_NAME);

        when(node.getIpInterfaces()).thenReturn(ipInterfaces);
        when(node.getLocation()).thenReturn(location);
        when(ipInterface.getIpAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        NodeResourceType nodeResourceType = new NodeResourceType(resourceDao, nodeDao);
        OnmsResource nodeResource = new OnmsResource("1", "Node", nodeResourceType, Collections.emptySet(), ResourcePath.get("foo"));
        nodeResource.setEntity(node);

        List<OnmsResource> resources = responseTimeResourceType.getResourcesForParent(nodeResource);

        assertEquals(1, resources.size());
        assertEquals("127.0.0.1", resources.get(0).getName());

        verify(node, atLeastOnce()).getLocation();
        verify(node, times(1)).getIpInterfaces();
        verify(ipInterface, atLeastOnce()).getIpAddress();
    }

    @Test
    public void canGetChildByName() throws IOException {
        when(ipInterfaceDao.get(node, "127.0.0.1")).thenReturn(ipInterface);

        OnmsResource parent = mock(OnmsResource.class);
        when(parent.getEntity()).thenReturn(node);

        OnmsResource resource = responseTimeResourceType.getChildByName(parent, "127.0.0.1");

        assertEquals("127.0.0.1", resource.getName());
        assertEquals(parent, resource.getParent());

        verify(parent, atLeastOnce()).getEntity();
        verify(node, times(1)).getLocation();
        verify(ipInterfaceDao, times(1)).get(any(OnmsNode.class), anyString());
    }
}
