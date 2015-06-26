/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.xmlgraphics.util.ClasspathResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
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
        assertEquals(164, config.getSnmpCollections()[0].getDataCollectionGroups()[0].getResourceTypes().length);
        assertEquals(164, config.getSnmpCollections()[1].getDataCollectionGroups()[0].getResourceTypes().length);
    }
    
}
