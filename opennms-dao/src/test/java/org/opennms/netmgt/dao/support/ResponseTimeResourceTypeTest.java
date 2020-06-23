/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private IpInterfaceDao ipInterfaceDao = createMock(IpInterfaceDao.class);

    private OnmsNode node = createMock(OnmsNode.class);

    private OnmsIpInterface ipInterface = createMock(OnmsIpInterface.class);

    private Set<OnmsIpInterface> ipInterfaces = new HashSet<>();

    private ResponseTimeResourceType responseTimeResourceType = new ResponseTimeResourceType(resourceStorageDao, ipInterfaceDao);

    private NodeDao nodeDao = createMock(NodeDao.class);

    private ResourceDao resourceDao = createMock(ResourceDao.class);

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

    @Test
    public void canGetResourcesForNode() throws IOException {
        expect(node.getIpInterfaces()).andReturn(ipInterfaces);
        expect(node.getLocation()).andReturn(null);
        expect(ipInterface.getIpAddress()).andReturn(InetAddress.getByName("127.0.0.1")).atLeastOnce();

        replay(node, ipInterface);
        NodeResourceType nodeResourceType = new NodeResourceType(resourceDao, nodeDao);
        OnmsResource nodeResource = new OnmsResource("1", "Node", nodeResourceType, Collections.emptySet(), ResourcePath.get("foo"));
        nodeResource.setEntity(node);
        
        List<OnmsResource> resources = responseTimeResourceType.getResourcesForParent(nodeResource);
        verify(node, ipInterface);

        assertEquals(1, resources.size());
        assertEquals("127.0.0.1", resources.get(0).getName());
    }

    @Test
    public void canGetResourcesForNodeAtLocation() throws IOException {
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName(NON_DEFAULT_LOCATION_NAME);

        expect(node.getIpInterfaces()).andReturn(ipInterfaces);
        expect(node.getLocation()).andReturn(location).atLeastOnce();
        expect(ipInterface.getIpAddress()).andReturn(InetAddress.getByName("127.0.0.1")).atLeastOnce();

        replay(node, ipInterface);
        NodeResourceType nodeResourceType = new NodeResourceType(resourceDao, nodeDao);
        OnmsResource nodeResource = new OnmsResource("1", "Node", nodeResourceType, Collections.emptySet(), ResourcePath.get("foo"));
        nodeResource.setEntity(node);

        List<OnmsResource> resources = responseTimeResourceType.getResourcesForParent(nodeResource);
        verify(node, ipInterface);

        assertEquals(1, resources.size());
        assertEquals("127.0.0.1", resources.get(0).getName());
    }

    @Test
    public void canGetChildByName() throws IOException {
        expect(ipInterfaceDao.get(node, "127.0.0.1")).andReturn(ipInterface);

        OnmsResource parent = createMock(OnmsResource.class);
        expect(parent.getEntity()).andReturn(node);

        replay(ipInterface, parent, ipInterfaceDao);
        OnmsResource resource = responseTimeResourceType.getChildByName(parent, "127.0.0.1");
        verify(ipInterface, parent, ipInterfaceDao);

        assertEquals("127.0.0.1", resource.getName());
        assertEquals(parent, resource.getParent());
    }
}
