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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

public class DefaultResourceDaoTest {

    private EasyMockUtils m_easyMockUtils;

    private NodeDao m_nodeDao;
    private LocationMonitorDao m_locationMonitorDao;
    private CollectdConfigFactory m_collectdConfig;
    private DataCollectionConfigDao m_dataCollectionConfigDao;
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
        
        m_easyMockUtils = new EasyMockUtils();
        m_nodeDao = m_easyMockUtils.createMock(NodeDao.class);
        m_locationMonitorDao = m_easyMockUtils.createMock(LocationMonitorDao.class);
        m_dataCollectionConfigDao = m_easyMockUtils.createMock(DataCollectionConfigDao.class);
        m_filterDao = m_easyMockUtils.createMock(FilterDao.class);
        m_ipInterfaceDao = m_easyMockUtils.createMock(IpInterfaceDao.class);

        FilterDaoFactory.setInstance(m_filterDao);
        
        expect(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE *.*.*.*")).andReturn(new ArrayList<InetAddress>(0)).anyTimes();
        
        m_easyMockUtils.replayAll();
        setUpCollectdConfigFactory();
        m_easyMockUtils.verifyAll();

        RrdStrategy<?, ?> rrdStrategy = new JRobinRrdStrategy();
        m_rrdFileExtension = rrdStrategy.getDefaultFileExtension();

        m_resourceStorageDao.setRrdDirectory(m_fileAnticipator.getTempDir());
        m_resourceStorageDao.setRrdStrategy(rrdStrategy);

        m_resourceDao = new DefaultResourceDao();
        m_resourceDao.setNodeDao(m_nodeDao);
        m_resourceDao.setLocationMonitorDao(m_locationMonitorDao);
        m_resourceDao.setIpInterfaceDao(m_ipInterfaceDao);
        m_resourceDao.setCollectdConfig(m_collectdConfig);
        m_resourceDao.setDataCollectionConfigDao(m_dataCollectionConfigDao);
        m_resourceDao.setResourceStorageDao(m_resourceStorageDao);

        expect(m_dataCollectionConfigDao.getConfiguredResourceTypes()).andReturn(new HashMap<String, ResourceType>());
        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(m_lastUpdateTime);

        m_easyMockUtils.replayAll();
        m_resourceDao.afterPropertiesSet();
        m_easyMockUtils.verifyAll();
    }

    @After
    public void tearDown() {
        m_fileAnticipator.tearDown();
    }
    
    private void setUpCollectdConfigFactory() throws MarshalException, ValidationException, IOException {
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/collectdconfiguration-testdata.xml");
        m_collectdConfig = new CollectdConfigFactory(stream, "localhost", false);
        stream.close();
    }

    @Test
    public void testGetResourceByIdNewEmpty() {
        m_easyMockUtils.replayAll();
        m_resourceDao.getResourceById("");
        m_easyMockUtils.verifyAll();
    }

    @Test
    public void testGetResourceByIdNewTopLevelOnly() throws Exception {
        OnmsNode node = createNode();
        expect(m_nodeDao.get(node.getId())).andReturn(node).times(1);

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById("node[1]");
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource should not be null", resource);
    }

    @Test
    public void testGetResourceByIdNewTwoLevel() throws Exception {
        OnmsIpInterface ip = createIpInterfaceOnNode();
        expect(m_nodeDao.get(ip.getNode().getId())).andReturn(ip.getNode()).times(1);
        expect(m_ipInterfaceDao.get(ip.getNode(), "192.168.1.1")).andReturn(ip).times(1);
        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(new Date(System.currentTimeMillis()-86400000l)).anyTimes();

        File response = m_fileAnticipator.tempDir("response");
        File ipDir = m_fileAnticipator.tempDir(response, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + m_rrdFileExtension);

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById("node[1].responseTime[192.168.1.1]");
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeExists() throws Exception {
        OnmsNode node = createNode();
        expect(m_nodeDao.get(node.getId())).andReturn(node).times(1);

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(String.format("node[%d]", node.getId()));
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeSourceExists() throws Exception {
        OnmsNode node = createNode();
        expect(m_nodeDao.findByForeignId("source1", "123")).andReturn(node).times(1);

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File forSrcDir = m_fileAnticipator.tempDir(responseDir, "fs");
        File sourceDir = m_fileAnticipator.tempDir(forSrcDir, "source1");
        File idDir = m_fileAnticipator.tempDir(sourceDir, "123");
        m_fileAnticipator.tempFile(idDir, "foo" + m_rrdFileExtension);

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(String.format("nodeSource[source1:123]", node.getId()));
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeDoesNotExist() {
        expect(m_nodeDao.get(2)).andReturn(null);

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(String.format("node[%d]", 2));
        m_easyMockUtils.verifyAll();

        assertNull("Resource should be null", resource);
    }

    @Test
    public void testGetTopLevelResourceNodeExistsNoChildResources() throws Exception {
        OnmsNode node = createNode(2, "Node Two");

        expect(m_nodeDao.get(node.getId())).andReturn(node).times(1);
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(String.format("node[%d]", 2));
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceDomainExists() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        File domain = m_fileAnticipator.tempDir(snmp, "example1");
        File intf = m_fileAnticipator.tempDir(domain, "server1");
        m_fileAnticipator.tempFile(intf, "ifInOctects" + m_rrdFileExtension);

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(String.format("domain[%s]", "example1"));
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetTopLevelResourceDomainDoesNotExistInCollectdConfig() {
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(String.format("domain[%s]", "bogus"));
        m_easyMockUtils.verifyAll();

        assertNull("Resource should be null", resource);
    }

    @Test
    public void testGetTopLevelResourceDomainDoesNotExistNoInterfaceDirectories() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        m_fileAnticipator.tempDir(snmp, "example1");

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(String.format("domain[%s]", "example1"));
        m_easyMockUtils.verifyAll();

        assertNull("Resource should be null", resource);
    }

    @Test
    public void testGetTopLevelResourceWithInvalidResourceType() {
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById("bogus");
        m_easyMockUtils.verifyAll();

        assertNull("Resource should be null", resource);
    }

    @Test
    public void testGetResourceDomainInterfaceExists() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        File domain = m_fileAnticipator.tempDir(snmp, "example1");
        File intf = m_fileAnticipator.tempDir(domain, "server1");
        m_fileAnticipator.tempFile(intf, "ifInOctects" + m_rrdFileExtension);
        
        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(new Date(System.currentTimeMillis()-86400000l)).anyTimes();
        String resourceId = OnmsResource.createResourceId("domain", "example1", "interfaceSnmp", "server1");
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetResourceNoNode() throws Exception {
        String resourceId = OnmsResource.createResourceId("node", "1", "nodeSnmp", "");
        
        expect(m_nodeDao.get(1)).andReturn(null);

        m_easyMockUtils.replayAll();
        m_resourceDao.getResourceById(resourceId);

        m_easyMockUtils.verifyAll();
    }

    @Test
    public void testFindNodeResourcesWithResponseTime() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);

        expect(m_nodeDao.findAll()).andReturn(nodes);

        File response = m_fileAnticipator.tempDir("response");
        File ipDir = m_fileAnticipator.tempDir(response, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + m_rrdFileExtension);

        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(m_lastUpdateTime);
        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(Collections.emptyList());

        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();

        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    @Test
    public void testFindNodeResourcesWithDistributedResponseTime() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);

        expect(m_nodeDao.findAll()).andReturn(nodes);

        File response = m_fileAnticipator.tempDir("response");
        File distributed = m_fileAnticipator.tempDir(response, "distributed");
        File monitor = m_fileAnticipator.tempDir(distributed, "1");
        File ipDir = m_fileAnticipator.tempDir(monitor, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + m_rrdFileExtension);

        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(m_lastUpdateTime);

        // Setup the status to match the path on disk
        OnmsLocationMonitor locMon = new OnmsLocationMonitor();
        locMon.setId(1);
        OnmsIpInterface ipIntf = new OnmsIpInterface();
        ipIntf.setIpAddress(InetAddress.getByName("192.168.1.1"));
        LocationMonitorIpInterface locMonIpIntf = new LocationMonitorIpInterface(locMon, ipIntf);

        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(Collections.singleton(locMonIpIntf)).anyTimes();

        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource list should not be null", resources);
        assertEquals("Resource list size", 1, resources.size());
    }

    @Test
    public void testFindNodeResourcesWithNodeSnmp() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);

        expect(m_nodeDao.findAll()).andReturn(nodes);

        File snmp = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(snmp, "1");
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);

        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(m_lastUpdateTime);
        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(Collections.emptyList());

        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();

        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    @Test
    public void testFindNodeResourcesWithNodeInterface() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);

        expect(m_nodeDao.findAll()).andReturn(nodes);

        File snmp = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(snmp, "1");
        File intfDir = m_fileAnticipator.tempDir(nodeDir, "eth0");
        m_fileAnticipator.tempFile(intfDir, "foo" + m_rrdFileExtension);

        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(m_lastUpdateTime);
        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(Collections.emptyList());

        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();

        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    @Test
    public void testGetResourceForNode() throws Exception {
        OnmsNode node = createNode();

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetResourceForNodeWithNullOnmsNode() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("node argument must not be null"));

        m_easyMockUtils.replayAll();
        try {
             m_resourceDao.getResourceForNode(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }

    @Test
    public void testGetResourceForIpInterfaceWithLocationMonitor() throws Exception {
        OnmsIpInterface ip = createIpInterfaceOnNode();

        OnmsLocationMonitor locMon = new OnmsLocationMonitor();
        locMon.setId(12345);

        // Create distributed/9850/209.61.128.9
        File response = m_fileAnticipator.tempDir("response");
        File distributed = m_fileAnticipator.tempDir(response, "distributed");
        File locMonDir = m_fileAnticipator.tempDir(distributed, locMon.getId().toString());
        File ipDir = m_fileAnticipator.tempDir(locMonDir, InetAddressUtils.str(ip.getIpAddress()));
        m_fileAnticipator.tempFile(ipDir, "http" + m_rrdFileExtension);

        ArrayList<LocationMonitorIpInterface> locationMonitorInterfaces = new ArrayList<LocationMonitorIpInterface>();
        locationMonitorInterfaces.add(new LocationMonitorIpInterface(locMon, ip));

        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(ip.getNode().getId())).andReturn(locationMonitorInterfaces);
        expect(m_dataCollectionConfigDao.getLastUpdate()).andReturn(new Date(System.currentTimeMillis()-86400000l)).anyTimes();

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForIpInterface(ip, locMon);
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource should not be null", resource);
    }

    @Test
    public void testGetResourceForNodeWithData() throws Exception {
        OnmsNode node = createNode();

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + m_rrdFileExtension);
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should exist", resource);
    }

    @Test
    public void testGetResourceForNodeNoData() {
        OnmsNode node = createNode();

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should exist", resource);
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
