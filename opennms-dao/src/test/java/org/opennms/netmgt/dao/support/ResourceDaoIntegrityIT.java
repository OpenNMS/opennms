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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourceVisitor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * This test verifies the integrity of the resource tree returned
 * by the resource dao.
 *
 * The test relies on the following files:
 *
 *   resource-tree-files.txt
 *      Structure of the rrd folder on disk. Generated with:
 *       cd /opt/opennms/share/rrd/ && find * -type f
 *
 *   resource-tree-ips.txt
 *      IP addresses that are found in the resource tree. Generated with:
 *       psql -U opennms -t -A -F"," -c "select ipaddr from ipinterface"
 *
 *   resource-tree-results.txt
 *      Ordered list of resource ids and their attributes.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class ResourceDaoIntegrityIT implements InitializingBean {

    private EasyMockUtils m_easyMockUtils;
    private FilterDao m_filterDao;
    private CollectdConfigFactory m_collectdConfig;
    private ResourceTypesDao m_resourceTypesDao;
    private DefaultResourceDao m_resourceDao;
    private FilesystemResourceStorageDao m_resourceStorageDao = new FilesystemResourceStorageDao();

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private LocationMonitorDao m_locationMonitorDao;
 
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Rule
    public TemporaryFolder m_tempFolder = new TemporaryFolder();

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        setStoreByForeignSource(false);

        m_easyMockUtils = new EasyMockUtils();
        m_resourceTypesDao = m_easyMockUtils.createMock(ResourceTypesDao.class);
        m_filterDao = m_easyMockUtils.createMock(FilterDao.class);

        FilterDaoFactory.setInstance(m_filterDao);

        expect(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE *.*.*.*")).andReturn(new ArrayList<InetAddress>(0)).anyTimes();

        m_easyMockUtils.replayAll();
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/collectdconfiguration-testdata.xml");
        m_collectdConfig = new CollectdConfigFactory(stream, "localhost", false);
        m_easyMockUtils.verifyAll();

        m_resourceStorageDao.setRrdDirectory(m_tempFolder.getRoot());
        // Match up with the extensions in resource-tree-files.txt
        m_resourceStorageDao.setRrdExtension(".rrd");

        m_resourceDao = new DefaultResourceDao();
        m_resourceDao.setNodeDao(m_nodeDao);
        m_resourceDao.setLocationMonitorDao(m_locationMonitorDao);
        m_resourceDao.setCollectdConfig(m_collectdConfig);
        m_resourceDao.setResourceStorageDao(m_resourceStorageDao);
        m_resourceDao.setResourceTypesDao(m_resourceTypesDao);
        m_resourceDao.setIpInterfaceDao(m_ipInterfaceDao);
    }

    @Test
    @Transactional
    public void walkResourceTree() throws IOException {
        // Setup the file tree and the necessary objects in the DAOs
        createResourceTree();
        createNodes();
        Map<String, ResourceType> types = createResourceTypes();

        expect(m_resourceTypesDao.getLastUpdate()).andReturn(new Date(System.currentTimeMillis())).anyTimes();
        expect(m_resourceTypesDao.getResourceTypes()).andReturn(types).anyTimes();

        m_easyMockUtils.replayAll();
        m_resourceDao.afterPropertiesSet();

        // Walk the tree and collect the results
        ResourceCollector visitor = new ResourceCollector();
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(visitor);
        walker.walk();

        // We must have at least one resource for every known type
        for (OnmsResourceType type : m_resourceDao.getResourceTypes()) {
            // Ignore this type for now #needstoomanydbojects
            if (DistributedStatusResourceType.TYPE_NAME.equals(type.getName())) {
                continue;
            }
            // Ignore the interfaceSnmpByIfIndex since it functions as a pure alias
            // and should never be returned when enumerating resources
            if (InterfaceSnmpByIfIndexResourceType.TYPE_NAME.equals(type.getName())) {
                continue;
            }
            assertTrue("No resources of type: " + type.getLabel(), visitor.resourceTypes.contains(type));
        }

        // We must be able to retrieve the same resource by id
        for (Entry<ResourceId, OnmsResource> entry : visitor.resourcesById.entrySet()) {
            OnmsResource resourceRetrievedById = m_resourceDao.getResourceById(entry.getKey());
            assertNotNull(String.format("Failed to retrieve resource with id '%s'.", entry.getKey()), resourceRetrievedById);
            assertEquals(String.format("Result mismatch for resource with id '%s'. Retrieved id is '%s'.", entry.getKey(), resourceRetrievedById.getId()),
                    entry.getValue().getName(), resourceRetrievedById.getName());
        }

        // Build a line that represent the resource for every unique id
        // and compare it to the known results
        int k = 0;
        String[] expectedResults = loadExpectedResults();
        for (Entry<ResourceId, OnmsResource> entry : visitor.resourcesById.entrySet()) {
            // Convert the attributes to strings and order them lexicographically
            Set<String> attributeNames = new TreeSet<>();
            for (OnmsAttribute attribute : entry.getValue().getAttributes()) {
                attributeNames.add(attribute.toString());
            }

            // Compare
            String actualResult = entry.getKey() + ": " + attributeNames;
            assertEquals(String.format("Result mismatch at line %d.", k+1),
                    expectedResults[k], actualResult);
            k++;
        }

        // We should have as many unique resource ids as we have results
        assertEquals(expectedResults.length, visitor.resourcesById.size());

        m_easyMockUtils.verifyAll();
    }

    private static class ResourceCollector implements ResourceVisitor {
        private Map<ResourceId, OnmsResource> resourcesById = new TreeMap<>();

        private Set<OnmsResourceType> resourceTypes = new HashSet<>();

        @Override
        public void visit(OnmsResource resource) {
            resource.getResourceType();
            resourcesById.put(resource.getId(), resource);
            resourceTypes.add(resource.getResourceType());
        }
    }

    private String[] loadExpectedResults() throws IOException {
        String fileAsString = IOUtils.toString(new ClassPathResource("resource-tree-results.txt").getInputStream());
        return fileAsString.split("\\r?\\n");
    }

    private void createResourceTree() throws IOException {
        String fileAsString = IOUtils.toString(new ClassPathResource("resource-tree-files.txt").getInputStream());
        String[] resourceTreeFiles = fileAsString.split("\\r?\\n");

        // This should match the number of lines in the file
        assertEquals(31829, resourceTreeFiles.length);

        for (String resourceTreeFile : resourceTreeFiles) {
            // Create the file and its parent directories in the temporary folder
            File entry = new File(m_tempFolder.getRoot(), resourceTreeFile);
            entry.getParentFile().mkdirs();
            assertTrue("Failed to create " + entry, entry.createNewFile());
        }
    }

    /**
     * Creates a set of nodes and assigns a single IP address from
     * the resource-tree-ips.txt file to each node.
     */
    private void createNodes() throws IOException {
        final int NUM_NODES = 250;
        
        String fileAsString = IOUtils.toString(new ClassPathResource("resource-tree-ips.txt").getInputStream());
        String[] resourceTreeIps = fileAsString.split("\\r?\\n");

        // Make sure every IP address is represented at least once
        assertTrue(resourceTreeIps.length < NUM_NODES);

        for (int i = 1; i <= NUM_NODES; i++) {
            OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node" + i);
            node.setForeignSource("NODES");
            node.setForeignId(Integer.toString(i));
            m_nodeDao.save(node);

            OnmsIpInterface intf = new OnmsIpInterface();
            intf.setIpAddress(InetAddressUtils.addr(resourceTreeIps[i % resourceTreeIps.length]));
            intf.setNode(node);
            m_ipInterfaceDao.save(intf);

            node.setIpInterfaces(Collections.singleton(intf));
            m_nodeDao.saveOrUpdate(node);
        }
    }

    /**
     * Define a resource type so that test the GenericIndexResourceType
     */
    private Map<String, ResourceType> createResourceTypes() {
        Map<String, ResourceType> types = new HashMap<String, ResourceType>();

        ResourceType hrStorageIndex = new ResourceType();
        hrStorageIndex.setName("hrStorageIndex");
        hrStorageIndex.setLabel("Storage (SNMP MIB-2 Host Resources)");
        hrStorageIndex.setResourceLabel("${hrStorageDescr}");
        hrStorageIndex.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy("org.opennms.netmgt.collectd.PersistAllSelectorStrategy"));
        StorageStrategy storageStrategy = new StorageStrategy("org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy");
        storageStrategy.addParameter(new Parameter("sibling-column-name", "hrStorageDescr"));
        storageStrategy.addParameter(new Parameter("replace-first", "s/^-$/_root_fs/"));
        storageStrategy.addParameter(new Parameter("replace-all", "s/^-//"));
        storageStrategy.addParameter(new Parameter("replace-all", "s/\\s//"));
        storageStrategy.addParameter(new Parameter("replace-all", "s/:\\\\.*//"));
        hrStorageIndex.setStorageStrategy(storageStrategy);
        types.put(hrStorageIndex.getName(), hrStorageIndex);

        return types;
    }

    private void setStoreByForeignSource(boolean storeByForeignSource) {
        System.setProperty("org.opennms.rrd.storeByForeignSource", Boolean.toString(storeByForeignSource));
    }
}
