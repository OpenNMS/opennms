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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;

public class DefaultResourceDaoTest {

    private NodeDao m_nodeDao;
    private CollectdConfigFactory m_collectdConfig;
    private ResourceTypesDao m_resourceTypesDao;
    private DefaultResourceDao m_resourceDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private FilesystemResourceStorageDao m_resourceStorageDao = new FilesystemResourceStorageDao();

    private FileAnticipator m_fileAnticipator;

    private FilterDao m_filterDao;

    private Date m_lastUpdateTime = new Date();

    private String m_rrdFileExtension;

    @Before
    public void setUp() throws Exception {
        m_fileAnticipator = new FileAnticipator();
        
        m_nodeDao = mock(NodeDao.class);
        m_resourceTypesDao = mock(ResourceTypesDao.class);
        m_filterDao = mock(FilterDao.class);
        m_ipInterfaceDao = mock(IpInterfaceDao.class);

        FilterDaoFactory.setInstance(m_filterDao);

        when(m_filterDao.getActiveIPAddressList(anyString())).thenReturn(Arrays.asList());
        setUpCollectdConfigFactory();

        RrdStrategy<?, ?> rrdStrategy = new JRobinRrdStrategy();
        m_rrdFileExtension = rrdStrategy.getDefaultFileExtension();

        m_resourceStorageDao.setRrdDirectory(m_fileAnticipator.getTempDir());
        m_resourceStorageDao.setRrdStrategy(rrdStrategy);

        m_resourceDao = new DefaultResourceDao();
        m_resourceDao.setNodeDao(m_nodeDao);
        m_resourceDao.setIpInterfaceDao(m_ipInterfaceDao);
        m_resourceDao.setCollectdConfig(m_collectdConfig);
        m_resourceDao.setResourceTypesDao(m_resourceTypesDao);
        m_resourceDao.setResourceStorageDao(m_resourceStorageDao);

        when(m_resourceTypesDao.getResourceTypes()).thenReturn(new HashMap<String, ResourceType>());
        when(m_resourceTypesDao.getLastUpdate()).thenReturn(m_lastUpdateTime);
        m_resourceDao.afterPropertiesSet();
        verify(m_resourceTypesDao, atLeastOnce()).getResourceTypes();
        verify(m_resourceTypesDao, atLeastOnce()).getLastUpdate();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(m_nodeDao);
        verifyNoMoreInteractions(m_resourceTypesDao);
        verifyNoMoreInteractions(m_filterDao);
        verifyNoMoreInteractions(m_ipInterfaceDao);
        m_fileAnticipator.tearDown();
    }
    
    private void setUpCollectdConfigFactory() throws IOException {
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/collectdconfiguration-testdata.xml");
        m_collectdConfig = new CollectdConfigFactory(stream);
        stream.close();
    }

    @Test
    public void testGetResourceByIdNewEmpty() {
        m_resourceDao.getResourceById(null);
        verifyNoMoreInteractions(m_filterDao);
        verifyNoMoreInteractions(m_resourceTypesDao);
    }

    @Test
    public void testGetResourceByIdNewTopLevelOnly() throws Exception {
        OnmsNode node = createNode();
        final var nodeId = node.getId().toString();
        when(m_nodeDao.get(nodeId)).thenReturn(node);

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, nodeId);
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);
        
        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("node", "1"));
        verify(m_nodeDao, times(1)).get(nodeId);

        assertNotNull("resource should not be null", resource);
    }

    @Test
    public void testGetResourceByIdNewTwoLevel() throws Exception {
        OnmsIpInterface ip = createIpInterfaceOnNode();
        final var nodeId = ip.getNode().getId().toString();
        final var date = new Date(System.currentTimeMillis()-86400000l);

        when(m_nodeDao.get(nodeId)).thenReturn(ip.getNode());
        when(m_ipInterfaceDao.get(ip.getNode(), "192.168.1.1")).thenReturn(ip);
        when(m_resourceTypesDao.getLastUpdate()).thenReturn(date);

        File response = m_fileAnticipator.tempDir("response");
        File ipDir = m_fileAnticipator.tempDir(response, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + m_rrdFileExtension);

        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("node", "1").resolve("responseTime", "192.168.1.1"));

        verify(m_nodeDao, times(1)).get(nodeId);
        verify(m_ipInterfaceDao, times(1)).get(ip.getNode(), "192.168.1.1");
        verify(m_resourceTypesDao, atLeastOnce()).getLastUpdate();

        assertNotNull("resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeExists() throws Exception {
        final var node = createNode();
        final var nodeId = node.getId().toString();

        when(m_nodeDao.get(nodeId)).thenReturn(node);

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, nodeId);
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);

        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("node", Integer.toString(node.getId())));

        verify(m_nodeDao, times(1)).get(nodeId);

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeSourceExists() throws Exception {
        OnmsNode node = createNode();
        when(m_nodeDao.get("source1:123")).thenReturn(node);

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File forSrcDir = m_fileAnticipator.tempDir(responseDir, "fs");
        File sourceDir = m_fileAnticipator.tempDir(forSrcDir, "source1");
        File idDir = m_fileAnticipator.tempDir(sourceDir, "123");
        m_fileAnticipator.tempFile(idDir, "foo" + m_rrdFileExtension);

        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("nodeSource", "source1:123"));

        verify(m_nodeDao, times(1)).get("source1:123");

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeDoesNotExist() {
        when(m_nodeDao.get("2")).thenReturn(null);

        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("node", "2"));

        verify(m_nodeDao, times(1)).get("2");

        assertNull("Resource should be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeExistsNoChildResources() throws Exception {
        final var node = createNode(2, "Node Two");
        final var nodeId = node.getId().toString();

        when(m_nodeDao.get(nodeId)).thenReturn(node);
        
        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("node", "2"));

        verify(m_nodeDao, times(1)).get(nodeId);

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceDomainExists() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        File domain = m_fileAnticipator.tempDir(snmp, "example1");
        File intf = m_fileAnticipator.tempDir(domain, "server1");
        m_fileAnticipator.tempFile(intf, "ifInOctects" + m_rrdFileExtension);

        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("domain", "example1"));

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceDomainDoesNotExistInCollectdConfig() {
        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("domain", "bogus"));

        assertNull("Resource should be null", resource);
    }

    @Test
    public void testGetTopLevelResourceWithInvalidResourceType() {
        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.get("bogus", null));

        assertNull("Resource should be null", resource);
    }

    @Test
    public void testGetResourceDomainInterfaceExists() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        File domain = m_fileAnticipator.tempDir(snmp, "example1");
        File intf = m_fileAnticipator.tempDir(domain, "server1");
        m_fileAnticipator.tempFile(intf, "ifInOctects" + m_rrdFileExtension);
        
        final var date = new Date(System.currentTimeMillis()-86400000l);
        when(m_resourceTypesDao.getLastUpdate()).thenReturn(date);
        ResourceId resourceId = ResourceId.get("domain", "example1").resolve("interfaceSnmp", "server1");
        
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);

        verify(m_resourceTypesDao, atLeastOnce()).getLastUpdate();
        
        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetResourceNoNode() throws Exception {
        ResourceId resourceId = ResourceId.get("node", "1").resolve("nodeSnmp", "");
        
        when(m_nodeDao.get("1")).thenReturn(null);

        m_resourceDao.getResourceById(resourceId);

        verify(m_nodeDao, times(1)).get("1");
    }

    @Test
    public void testFindNodeResourcesWithResponseTime() throws Exception {
        List<OnmsNode> nodes = new LinkedList<>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);

        when(m_nodeDao.findAll()).thenReturn(nodes);

        File response = m_fileAnticipator.tempDir("response");
        File ipDir = m_fileAnticipator.tempDir(response, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + m_rrdFileExtension);

        when(m_resourceTypesDao.getLastUpdate()).thenReturn(m_lastUpdateTime);

        List<OnmsResource> resources = m_resourceDao.findTopLevelResources();
        
        verify(m_nodeDao, times(1)).findAll();
        verify(m_resourceTypesDao, atLeastOnce()).getLastUpdate();

        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    @Test
    public void testFindNodeResourcesWithNodeSnmp() throws Exception {
        List<OnmsNode> nodes = new LinkedList<>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);

        when(m_nodeDao.findAll()).thenReturn(nodes);

        File snmp = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(snmp, "1");
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);

        when(m_resourceTypesDao.getLastUpdate()).thenReturn(m_lastUpdateTime);

        List<OnmsResource> resources = m_resourceDao.findTopLevelResources();

        verify(m_nodeDao, times(1)).findAll();
        verify(m_resourceTypesDao, atLeastOnce()).getLastUpdate();

        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    @Test
    public void testFindNodeResourcesWithNodeInterface() throws Exception {
        List<OnmsNode> nodes = new LinkedList<>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);

        when(m_nodeDao.findAll()).thenReturn(nodes);

        File snmp = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(snmp, "1");
        File intfDir = m_fileAnticipator.tempDir(nodeDir, "eth0");
        m_fileAnticipator.tempFile(intfDir, "foo" + m_rrdFileExtension);

        when(m_resourceTypesDao.getLastUpdate()).thenReturn(m_lastUpdateTime);

        List<OnmsResource> resources = m_resourceDao.findTopLevelResources();

        verify(m_nodeDao, times(1)).findAll();
        verify(m_resourceTypesDao, atLeastOnce()).getLastUpdate();

        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    @Test
    public void testGetResourceForNode() throws Exception {
        OnmsNode node = createNode();

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);

        OnmsResource resource = m_resourceDao.getResourceForNode(node);

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetResourceForNodeWithNullOnmsNode() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("node argument must not be null"));

        try {
             m_resourceDao.getResourceForNode(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }

    @Test
    public void testGetResourceForNodeWithData() throws Exception {
        OnmsNode node = createNode();

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);
        
        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        
        assertNotNull("Resource should exist", resource);
    }

    @Test
    public void testGetResourceForNodeNoData() {
        OnmsNode node = createNode();

        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        
        assertNotNull("Resource should exist", resource);
    }

    @Test
    public void testResourceIdGeneration() {
        CollectionResource mockNodeResource = mock(CollectionResource.class);
        CollectionResource mockInterfaceResource = mock(CollectionResource.class);
        CollectionResource mockGenericResource = mock(CollectionResource.class);

        when(mockNodeResource.getResourceTypeName()).thenReturn(CollectionResource.RESOURCE_TYPE_NODE);
        when(mockInterfaceResource.getResourceTypeName()).thenReturn(CollectionResource.RESOURCE_TYPE_IF);
        when(mockInterfaceResource.getInterfaceLabel()).thenReturn("wlp4s1-9061resds41c");
        when(mockGenericResource.getResourceTypeName()).thenReturn("diskIOIndex");
        when(mockGenericResource.getInterfaceLabel()).thenReturn("nvme0n1");

        ResourceId resourceIdForNode = m_resourceDao.getResourceId(mockNodeResource, 5);
        ResourceId resourceIdForInterface = m_resourceDao.getResourceId(mockInterfaceResource, 5);
        ResourceId resourceIdForGeneric = m_resourceDao.getResourceId(mockGenericResource, 5);

        Assert.assertEquals(resourceIdForNode.toString(), "node[5].nodeSnmp[]");
        Assert.assertEquals(resourceIdForInterface.toString(), "node[5].interfaceSnmp[wlp4s1-9061resds41c]");
        Assert.assertEquals(resourceIdForGeneric.toString(), "node[5].diskIOIndex[nvme0n1]");
        // When System property org.opennms.rrd.storeByForeignSource = true
        when(mockNodeResource.getParent()).thenReturn(ResourcePath.get("fs", "req1", "1223212"));

        resourceIdForNode = m_resourceDao.getResourceId(mockNodeResource, 5);
        Assert.assertEquals(resourceIdForNode.toString(), "nodeSource[req1:1223212].nodeSnmp[]");

        verify(mockNodeResource, atLeastOnce()).getResourceTypeName();
        verify(mockNodeResource, atLeastOnce()).getInterfaceLabel();
        verify(mockNodeResource, atLeastOnce()).getParent();
        verify(mockInterfaceResource, atLeastOnce()).getResourceTypeName();
        verify(mockInterfaceResource, atLeastOnce()).getInterfaceLabel();
        verify(mockInterfaceResource, atLeastOnce()).getParent();
        verify(mockGenericResource, atLeastOnce()).getResourceTypeName();
        verify(mockGenericResource, atLeastOnce()).getInterfaceLabel();
        verify(mockGenericResource, atLeastOnce()).getParent();

        verifyNoMoreInteractions(mockNodeResource);
        verifyNoMoreInteractions(mockInterfaceResource);
        verifyNoMoreInteractions(mockGenericResource);
    }

    private OnmsNode createNode() {
        return createNode(1, "Node One");
    }

    private OnmsNode createNode(int id, String label) {
        OnmsNode node = new OnmsNode();
        node.setId(id);
        node.setLabel(label);
        return node;
    }

    private OnmsIpInterface createIpInterface() throws UnknownHostException {
        OnmsIpInterface ip = new OnmsIpInterface();
        ip.setIpAddress(InetAddressUtils.addr("192.168.1.1"));
        return ip;
    }

    private OnmsIpInterface createIpInterfaceOnNode() throws UnknownHostException {
        OnmsIpInterface ip = createIpInterface();
        createNode().addIpInterface(ip);
        return ip;
    }
}
