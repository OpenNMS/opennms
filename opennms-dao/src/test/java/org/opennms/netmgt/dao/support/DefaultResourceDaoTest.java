/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

public class DefaultResourceDaoTest extends TestCase {
    private EasyMockUtils m_easyMockUtils;
    
    private NodeDao m_nodeDao;
    private LocationMonitorDao m_locationMonitorDao;
    private CollectdConfigFactory m_collectdConfig;
    private DataCollectionConfigDao m_dataCollectionConfigDao;
    private DefaultResourceDao m_resourceDao;
    
    private FileAnticipator m_fileAnticipator;

    private FilterDao m_filterDao;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_fileAnticipator = new FileAnticipator();
        
        m_easyMockUtils = new EasyMockUtils();
        m_nodeDao = m_easyMockUtils.createMock(NodeDao.class);
        m_locationMonitorDao = m_easyMockUtils.createMock(LocationMonitorDao.class);
        m_dataCollectionConfigDao = m_easyMockUtils.createMock(DataCollectionConfigDao.class);
        m_filterDao = m_easyMockUtils.createMock(FilterDao.class);
        
        FilterDaoFactory.setInstance(m_filterDao);
        
        expect(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE *.*.*.*")).andReturn(new ArrayList<InetAddress>(0)).anyTimes();
        
        m_easyMockUtils.replayAll();
        setUpCollectdConfigFactory();
        m_easyMockUtils.verifyAll();

        m_resourceDao = new DefaultResourceDao();
        m_resourceDao.setNodeDao(m_nodeDao);
        m_resourceDao.setLocationMonitorDao(m_locationMonitorDao);
        m_resourceDao.setCollectdConfig(m_collectdConfig);
        m_resourceDao.setRrdDirectory(m_fileAnticipator.getTempDir());
        m_resourceDao.setDataCollectionConfigDao(m_dataCollectionConfigDao);
        
        RrdUtils.setStrategy(new JRobinRrdStrategy());
        
        expect(m_dataCollectionConfigDao.getConfiguredResourceTypes()).andReturn(new HashMap<String, ResourceType>());
        
        m_easyMockUtils.replayAll();
        m_resourceDao.afterPropertiesSet();
        m_easyMockUtils.verifyAll();
    }
    
    @Override
    protected void tearDown() {
        m_fileAnticipator.tearDown();
    }
    
    private void setUpCollectdConfigFactory() throws MarshalException, ValidationException, IOException {
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/collectdconfiguration-testdata.xml");
        m_collectdConfig = new CollectdConfigFactory(stream, "localhost", false);
        stream.close();
    }

    public void testGetResourceByIdNewEmpty() {
        m_easyMockUtils.replayAll();
        m_resourceDao.getResourceById("");
        m_easyMockUtils.verifyAll();
    }
   
    
    public void testGetResourceByIdNewTopLevelOnly() throws Exception {
        OnmsNode node = createNode();
        expect(m_nodeDao.get(node.getId())).andReturn(node).times(1);
        //expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(new ArrayList<LocationMonitorIpInterface>(0));
        
        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + RrdUtils.getExtension());
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById("node[1]");
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource should not be null", resource);
    }

    public void testGetResourceByIdNewTwoLevel() throws Exception {
        OnmsIpInterface ip = createIpInterfaceOnNode();
        expect(m_nodeDao.get(ip.getNode().getId())).andReturn(ip.getNode()).times(3);

        Collection<LocationMonitorIpInterface> locMons = new HashSet<LocationMonitorIpInterface>();
        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(1)).andReturn(locMons).times(1);
        
        File response = m_fileAnticipator.tempDir("response");
        File ipDir = m_fileAnticipator.tempDir(response, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + RrdUtils.getExtension());
                
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById("node[1].responseTime[192.168.1.1]");
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource should not be null", resource);
    }
    
    public void testGetTopLevelResourceNodeExists() throws Exception {
        OnmsNode node = createNode();
        expect(m_nodeDao.get(node.getId())).andReturn(node).times(1);
        //expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(new ArrayList<LocationMonitorIpInterface>(0));
        
        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + RrdUtils.getExtension());

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getTopLevelResource("node", node.getId().toString());
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should not be null", resource);
    }
    
    public void testGetTopLevelResourceNodeSourceExists() throws Exception {
        OnmsNode node = createNode();
        expect(m_nodeDao.findByForeignId("source1", "123")).andReturn(node).times(1);

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File forSrcDir = m_fileAnticipator.tempDir(responseDir, "fs");
        File sourceDir = m_fileAnticipator.tempDir(forSrcDir, "source1");
        File idDir = m_fileAnticipator.tempDir(sourceDir, "123");
        m_fileAnticipator.tempFile(idDir, "foo" + RrdUtils.getExtension());

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getTopLevelResource("nodeSource", "source1:123");
        m_easyMockUtils.verifyAll();

        assertNotNull("Resource should not be null", resource);
    }
    
    public void testGetTopLevelResourceNodeDoesNotExist() {
        expect(m_nodeDao.get(2)).andReturn(null);
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new ObjectRetrievalFailureException(OnmsNode.class, "2", "Top-level resource of resource type node could not be found: 2", null));

        m_easyMockUtils.replayAll();
        try {
            m_resourceDao.getTopLevelResource("node", "2");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }
    
    public void testGetTopLevelResourceNodeExistsNoChildResources() throws Exception {
        OnmsNode node = createNode(2, "Node Two");

        expect(m_nodeDao.get(node.getId())).andReturn(node).times(1);
        //expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(new ArrayList<LocationMonitorIpInterface>(0));

        /*
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new ObjectRetrievalFailureException(OnmsNode.class, node.getId().toString(), "Top-level resource was found but has no child resources", null));

        m_easyMockUtils.replayAll();
        try {
            m_resourceDao.getTopLevelResource("node", node.getId().toString());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
        */
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getTopLevelResource("node", node.getId().toString());
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource should not be null", resource);

    }
    
    public void testGetTopLevelResourceDomainExists() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        File domain = m_fileAnticipator.tempDir(snmp, "example1");
        File intf = m_fileAnticipator.tempDir(domain, "server1");
        m_fileAnticipator.tempFile(intf, "ifInOctects" + RrdUtils.getExtension());
        
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getTopLevelResource("domain", "example1");
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should not be null", resource);
    }
    
    public void testGetTopLevelResourceDomainDoesNotExistInCollectdConfig() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new ObjectRetrievalFailureException(OnmsResource.class, "bogus", "Domain not found due to domain RRD directory not existing or not a directory: " + m_fileAnticipator.getTempDir() + File.separator + "snmp" + File.separator + "bogus", null));
        
        m_easyMockUtils.replayAll();
        try {
            m_resourceDao.getTopLevelResource("domain", "bogus");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }

    // We don't need to test everything that could cause the filter to fail... that's the job of a filter test case
    public void testGetTopLevelResourceDomainDoesNotExistNoInterfaceDirectories() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        m_fileAnticipator.tempDir(snmp, "example1");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        File dir = new File(new File(m_fileAnticipator.getTempDir(), "snmp"), "example1");
        ta.anticipate(new ObjectRetrievalFailureException(OnmsResource.class, "example1", "Domain not found due to domain RRD directory not matching the domain directory filter: " + dir.getAbsolutePath(), null));

        m_easyMockUtils.replayAll();
        try {
            m_resourceDao.getTopLevelResource("domain", "example1");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }

    public void testGetTopLevelResourceWithInvalidResourceType() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new ObjectRetrievalFailureException("Top-level resource type of 'bogus' is unknown", "bogus"));

        m_easyMockUtils.replayAll();
        try {
            m_resourceDao.getTopLevelResource("bogus", "");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }
    
    public void testGetResourceDomainInterfaceExists() throws IOException {
        File snmp = m_fileAnticipator.tempDir("snmp");
        File domain = m_fileAnticipator.tempDir(snmp, "example1");
        File intf = m_fileAnticipator.tempDir(domain, "server1");
        m_fileAnticipator.tempFile(intf, "ifInOctects" + RrdUtils.getExtension());
        
        String resourceId = OnmsResource.createResourceId("domain", "example1", "interfaceSnmp", "server1");
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should not be null", resource);
    }
    
    public void testGetResourceNoNode() throws Exception {
        String resourceId = OnmsResource.createResourceId("node", "1", "nodeSnmp", "");
        
        expect(m_nodeDao.get(1)).andReturn(null);

        m_easyMockUtils.replayAll();
        m_resourceDao.getResourceById(resourceId);

        m_easyMockUtils.verifyAll();
    }

    public void testFindNodeResourcesWithResponseTime() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);
        List<Integer> nodeIds = new ArrayList<Integer>();
        nodeIds.add(node.getId());
        
        expect(m_nodeDao.getNodeIds()).andReturn(nodeIds);
        expect(m_nodeDao.get(1)).andReturn(node).times(2);
        

        File response = m_fileAnticipator.tempDir("response");
        File ipDir = m_fileAnticipator.tempDir(response, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + RrdUtils.getExtension());
        
        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    // XXX this is a false positive match because there isn't an entry in the DB for this distributed data
    public void testFindNodeResourcesWithDistributedResponseTime() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);
        List<Integer> nodeIds = new ArrayList<Integer>();
        nodeIds.add(node.getId());
        
        expect(m_nodeDao.getNodeIds()).andReturn(nodeIds);
        expect(m_nodeDao.get(1)).andReturn(node).times(2);

        File response = m_fileAnticipator.tempDir("response");
        File distributed = m_fileAnticipator.tempDir(response, "distributed");
        File monitor = m_fileAnticipator.tempDir(distributed, "1");
        File ipDir = m_fileAnticipator.tempDir(monitor, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + RrdUtils.getExtension());
        
        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }

    public void testFindNodeResourcesWithNodeSnmp() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);
        List<Integer> nodeIds = new ArrayList<Integer>();
        nodeIds.add(node.getId());
        
        expect(m_nodeDao.getNodeIds()).andReturn(nodeIds);
        expect(m_nodeDao.get(1)).andReturn(node).times(1);

        File snmp = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(snmp, "1");
        m_fileAnticipator.tempFile(nodeDir, "foo" + RrdUtils.getExtension());
        
        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }


    public void testFindNodeResourcesWithNodeInterface() throws Exception {
        List<OnmsNode> nodes = new LinkedList<OnmsNode>();
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        nodes.add(node);
        List<Integer> nodeIds = new ArrayList<Integer>();
        nodeIds.add(node.getId());
        
        expect(m_nodeDao.getNodeIds()).andReturn(nodeIds);
        expect(m_nodeDao.get(1)).andReturn(node).times(1);

        File snmp = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(snmp, "1");
        File intfDir = m_fileAnticipator.tempDir(nodeDir, "eth0");
        m_fileAnticipator.tempFile(intfDir, "foo" + RrdUtils.getExtension());
        
        m_easyMockUtils.replayAll();
        List<OnmsResource> resources = m_resourceDao.findNodeResources();
        m_easyMockUtils.verifyAll();
        
        assertNotNull("resource list should not be null", resources);
        assertEquals("resource list size", 1, resources.size());
    }
    
    public void testGetResourceForNode() throws Exception {
        OnmsNode node = createNode();
        
//        expect(m_nodeDao.get(node.getId())).andReturn(node);
//        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(new ArrayList<LocationMonitorIpInterface>(0));
        
        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + RrdUtils.getExtension());

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should not be null", resource);
    }
    
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

    public void testGetResourceForIpInterface() throws Exception {
        OnmsNode node = createNode();
        OnmsIpInterface ip = createIpInterface();
        node.addIpInterface(ip);
        
        File response = m_fileAnticipator.tempDir("response");
        File ipDir = m_fileAnticipator.tempDir(response, "192.168.1.1");
        m_fileAnticipator.tempFile(ipDir, "icmp" + RrdUtils.getExtension());

        expect(m_nodeDao.get(1)).andReturn(ip.getNode()).times(2);
        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(1)).andReturn(new ArrayList<LocationMonitorIpInterface>());

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForIpInterface(ip);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should not be null", resource);
    }
    
    public void testGetResourceForIpInterfaceWithNullOnmsIpInterface() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("ipInterface argument must not be null"));
        
        m_easyMockUtils.replayAll();
        try {
             m_resourceDao.getResourceForIpInterface(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }
    

    public void testGetResourceForIpInterfaceWithNullNodeOnOnmsIpInterface() throws UnknownHostException {
        OnmsIpInterface ip = createIpInterface();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("getNode() on ipInterface must not return null"));
        
        m_easyMockUtils.replayAll();
        try {
             m_resourceDao.getResourceForIpInterface(ip);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }
    
    public void testGetResourceForIpInterfaceWithLocationMonitor() throws Exception {
        OnmsIpInterface ip = createIpInterfaceOnNode();

        OnmsLocationMonitor locMon = new OnmsLocationMonitor();
        locMon.setId(12345);

        // Create distributed/9850/209.61.128.9
        File response = m_fileAnticipator.tempDir("response");
        File distributed = m_fileAnticipator.tempDir(response, "distributed");
        File locMonDir = m_fileAnticipator.tempDir(distributed, locMon.getId().toString());
        File ipDir = m_fileAnticipator.tempDir(locMonDir, InetAddressUtils.str(ip.getIpAddress()));
        m_fileAnticipator.tempFile(ipDir, "http" + RrdUtils.getExtension());
        
        ArrayList<LocationMonitorIpInterface> locationMonitorInterfaces = new ArrayList<LocationMonitorIpInterface>();
        locationMonitorInterfaces.add(new LocationMonitorIpInterface(locMon, ip));

        expect(m_nodeDao.get(ip.getNode().getId())).andReturn(ip.getNode()).times(1);
        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(ip.getNode().getId())).andReturn(locationMonitorInterfaces).times(2);

        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForIpInterface(ip, locMon);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should not be null", resource);
    }

    public void testGetResourceForNodeWithData() throws Exception {
        OnmsNode node = createNode();
        
//        expect(m_nodeDao.get(node.getId())).andReturn(node);
//        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(new ArrayList<LocationMonitorIpInterface>(0));

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File nodeDir = m_fileAnticipator.tempDir(responseDir, node.getId().toString());
        m_fileAnticipator.tempFile(nodeDir, "foo" + RrdUtils.getExtension());
        
        m_easyMockUtils.replayAll();
        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("Resource should exist", resource);
    }
    
    public void testFindNodeSourceDirectoriesExist() throws Exception {

        File responseDir = m_fileAnticipator.tempDir("snmp");
        File forSrcDir = m_fileAnticipator.tempDir(responseDir, "fs");
        File sourceDir = m_fileAnticipator.tempDir(forSrcDir, "source1");
        File idDir = m_fileAnticipator.tempDir(sourceDir, "123");
        m_fileAnticipator.tempFile(idDir, "foo" + RrdUtils.getExtension());

        m_easyMockUtils.replayAll();
        Set<String> directories = m_resourceDao.findNodeSourceDirectories();
        m_easyMockUtils.verifyAll();

        assertNotNull("Directories should not be null", directories);
        assertEquals("Directories set size is 1", 1, directories.size());
    }
    
    public void testFindNodeSourceDirectoriesNoRrdFiles() throws Exception {
        File responseDir = m_fileAnticipator.tempDir("snmp");
        File forSrcDir = m_fileAnticipator.tempDir(responseDir, "fs");
        File sourceDir = m_fileAnticipator.tempDir(forSrcDir, "source1");
        File idDir = m_fileAnticipator.tempDir(sourceDir, "123");
        m_fileAnticipator.tempFile(idDir, "foo");

        m_easyMockUtils.replayAll();
        Set<String> directories = m_resourceDao.findNodeSourceDirectories();
        m_easyMockUtils.verifyAll();

        assertNotNull("Directories should not be null", directories);
        assertEquals("Directories set size is 0", 0, directories.size());
    }

    public void testGetResourceForNodeNoData() {
        OnmsNode node = createNode();
        
//        expect(m_nodeDao.get(node.getId())).andReturn(node);
//        expect(m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(node.getId())).andReturn(new ArrayList<LocationMonitorIpInterface>(0));

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
