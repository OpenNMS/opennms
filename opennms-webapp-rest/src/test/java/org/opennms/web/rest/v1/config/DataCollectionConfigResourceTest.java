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
package org.opennms.web.rest.v1.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.internal.collection.DataCollectionConfigImpl;
import org.opennms.netmgt.rrd.RrdRepository;
import org.springframework.core.io.ClassPathResource;

@RunWith(BlockJUnit4ClassRunner.class)
public class DataCollectionConfigResourceTest {
    public static class TestDataCollectionConfigDao extends AbstractJaxbConfigDao<DatacollectionConfig,DatacollectionConfig> implements DataCollectionConfigDao {

        public TestDataCollectionConfigDao() {
            super(DatacollectionConfig.class, "data-collection");
        }

        @Override
        protected DatacollectionConfig translateConfig(final DatacollectionConfig config) {
            return config;
        }

        @Override
        public String getSnmpStorageFlag(String collectionName) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public List<MibObject> getMibObjectList(String cName, String aSysoid, String anAddress, int ifType) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public List<MibObjProperty> getMibObjProperties(final String cName, final String aSysoid, final String anAddress) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public Map<String, ResourceType> getConfiguredResourceTypes() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public RrdRepository getRrdRepository(String collectionName) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public int getStep(String collectionName) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public List<String> getRRAList(String collectionName) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public String getRrdPath() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public DatacollectionConfig getRootDataCollection() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public List<String> getAvailableDataCollectionGroups() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public List<String> getAvailableSystemDefs() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public List<String> getAvailableMibGroups() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public void reload() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public Date getLastUpdate() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

    }

    private DataCollectionConfigResource m_configResource;

    @Before
    public void setUp() throws Exception {
        m_configResource = new DataCollectionConfigResource();

        final TestDataCollectionConfigDao testDao = new TestDataCollectionConfigDao();
        testDao.setConfigResource(new ClassPathResource("dc.xml"));
        testDao.afterPropertiesSet();
        m_configResource.setDataCollectionConfigDao(testDao);

        m_configResource.afterPropertiesSet();
    }

    @Test
    public void testGetConfig() throws Exception {
        final Response response = m_configResource.getDataCollectionConfiguration();
        assertNotNull(response.getEntity());
        final DataCollectionConfigImpl config = (DataCollectionConfigImpl) response.getEntity();
        assertEquals(2, config.getSnmpCollections().length);
        System.err.println(JaxbUtils.marshal(config));
        assertEquals(145, config.getSnmpCollections()[0].getDataCollectionGroups()[0].getResourceTypes().length);
        assertEquals(145, config.getSnmpCollections()[1].getDataCollectionGroups()[0].getResourceTypes().length);
    }
    
}
