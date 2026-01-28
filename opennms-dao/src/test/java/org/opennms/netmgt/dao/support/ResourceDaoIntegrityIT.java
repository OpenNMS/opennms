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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
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
import org.junit.After;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class ResourceDaoIntegrityIT implements InitializingBean {

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

        m_resourceTypesDao = mock(ResourceTypesDao.class);
        m_filterDao = mock(FilterDao.class);

        FilterDaoFactory.setInstance(m_filterDao);

        when(m_filterDao.getActiveIPAddressList("IPADDR IPLIKE *.*.*.*")).thenReturn(new ArrayList<InetAddress>(0));

        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/collectdconfiguration-testdata.xml");
        m_collectdConfig = new CollectdConfigFactory(stream);

        m_resourceStorageDao.setRrdDirectory(m_tempFolder.getRoot());
        // Match up with the extensions in resource-tree-files.txt
        m_resourceStorageDao.setRrdExtension(".rrd");

        m_resourceDao = new DefaultResourceDao();
        m_resourceDao.setNodeDao(m_nodeDao);
        m_resourceDao.setCollectdConfig(m_collectdConfig);
        m_resourceDao.setResourceStorageDao(m_resourceStorageDao);
        m_resourceDao.setResourceTypesDao(m_resourceTypesDao);
        m_resourceDao.setIpInterfaceDao(m_ipInterfaceDao);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_resourceTypesDao);
        verifyNoMoreInteractions(m_filterDao);
    }

    @Test
    @Transactional
    public void walkResourceTree() throws IOException {
        // Setup the file tree and the necessary objects in the DAOs
        createResourceTree();
        createNodes();
        Map<String, ResourceType> types = createResourceTypes();

        when(m_resourceTypesDao.getLastUpdate()).thenReturn(new Date(System.currentTimeMillis()));
        when(m_resourceTypesDao.getResourceTypes()).thenReturn(types);

        m_resourceDao.afterPropertiesSet();

        // Walk the tree and collect the results
        ResourceCollector visitor = new ResourceCollector();
        ResourceTreeWalker walker = new ResourceTreeWalker();
        walker.setResourceDao(m_resourceDao);
        walker.setVisitor(visitor);
        walker.walk();

        // We must have at least one resource for every known type
        for (OnmsResourceType type : m_resourceDao.getResourceTypes()) {
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

        verify(m_resourceTypesDao, atLeastOnce()).getResourceTypes();
        verify(m_resourceTypesDao, atLeastOnce()).getLastUpdate();
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
        String fileAsString = IOUtils.toString(new ClassPathResource("resource-tree-results.txt").getInputStream(), Charset.defaultCharset());
        return fileAsString.split("\\r?\\n");
    }

    private void createResourceTree() throws IOException {
        String fileAsString = IOUtils.toString(new ClassPathResource("resource-tree-files.txt").getInputStream(), Charset.defaultCharset());
        String[] resourceTreeFiles = fileAsString.split("\\r?\\n");

        // This should match the number of lines in the file
        assertEquals(31850, resourceTreeFiles.length);

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
        
        String fileAsString = IOUtils.toString(new ClassPathResource("resource-tree-ips.txt").getInputStream(), Charset.defaultCharset());
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
