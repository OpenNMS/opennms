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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

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
public class AssetRecordDaoIT implements InitializingBean {
    
	@Autowired
	private DistPollerDao m_distPollerDao;
	
	@Autowired
	private MonitoringLocationDao m_locationDao;

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private AssetRecordDao m_assetRecordDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private static boolean m_populated = false;
    
    @BeforeTransaction
    public void setUp() {
       if (!m_populated) {
           m_databasePopulator.populateDatabase();
           m_populated = true;
       }
    }

	@Test
	@Transactional
    public void testCreateAndGets() {
        OnmsNode onmsNode = new OnmsNode(m_locationDao.getDefaultLocation(), "myNode");
        m_nodeDao.save(onmsNode);
        OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
        assetRecord.setAssetNumber("imported-id: 7");
        m_assetRecordDao.update(assetRecord);
        m_assetRecordDao.flush();

        //Test findAll method
        Collection<OnmsAssetRecord> assetRecords = m_assetRecordDao.findAll();
        assertEquals(7, assetRecords.size());
        
        //Test countAll method
        assertEquals(7, m_assetRecordDao.countAll());

    }

    @Test
    @Transactional
	public void testAddUserName() {
        OnmsNode onmsNode = new OnmsNode(m_locationDao.getDefaultLocation(), "myNode");
        m_nodeDao.save(onmsNode);
        OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
        assetRecord.setAssetNumber("imported-id: 7");
        assetRecord.setUsername("antonio");
        assetRecord.setPassword("password");
        assetRecord.setEnable("cisco");
        assetRecord.setConnection(OnmsAssetRecord.TELNET_CONNECTION);
        m_assetRecordDao.update(assetRecord);
        m_assetRecordDao.flush();

        //Test findAll method
        int id = assetRecord.getId();
        OnmsAssetRecord assetRecordFromDb = m_assetRecordDao.get(id);
        assertEquals(assetRecord.getUsername(), assetRecordFromDb.getUsername());
        assertEquals(assetRecord.getPassword(), assetRecordFromDb.getPassword());
        assertEquals(assetRecord.getEnable(), assetRecordFromDb.getEnable());
        assertEquals(assetRecord.getConnection(), assetRecordFromDb.getConnection());

    }
    
	@Test
    @Transactional
    public void testAddAutoenable() {
        OnmsNode onmsNode = new OnmsNode(m_locationDao.getDefaultLocation(), "myNode");
        m_nodeDao.save(onmsNode);
        OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
        assetRecord.setAssetNumber("imported-id: 7");
        assetRecord.setUsername("antonio");
        assetRecord.setPassword("password");
        assetRecord.setAutoenable(OnmsAssetRecord.AUTOENABLED);
        assetRecord.setConnection(OnmsAssetRecord.TELNET_CONNECTION);
        m_assetRecordDao.update(assetRecord);
        m_assetRecordDao.flush();

        //Test findAll method
        int id = assetRecord.getId();
        OnmsAssetRecord assetRecordFromDb = m_assetRecordDao.get(id);
        assertEquals(assetRecord.getUsername(), assetRecordFromDb.getUsername());
        assertEquals(assetRecord.getPassword(), assetRecordFromDb.getPassword());
        assertEquals(assetRecord.getAutoenable(), assetRecordFromDb.getAutoenable());
        assertEquals(assetRecord.getConnection(), assetRecordFromDb.getConnection());

    }

        @Test
        @Transactional
    public void testFindByNodeId() {
        OnmsNode onmsNode = new OnmsNode(m_locationDao.getDefaultLocation(), "myNode");
        m_nodeDao.save(onmsNode);
        OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
        assetRecord.setAssetNumber("imported-id: 7");
        m_assetRecordDao.update(assetRecord);
        m_assetRecordDao.flush();

        //Test findByNodeId method
        OnmsAssetRecord a = m_assetRecordDao.findByNodeId(onmsNode.getId());
        assertTrue(a.equals(assetRecord));
    }

        @Test
        @Transactional
        public void testGeolocation() {
            OnmsNode onmsNode = new OnmsNode(m_locationDao.getDefaultLocation(), "myNode");
            m_nodeDao.save(onmsNode);
            OnmsAssetRecord assetRecord = onmsNode.getAssetRecord();
            OnmsGeolocation geo = assetRecord.getGeolocation();
            if (geo == null) {
                geo = new OnmsGeolocation();
                assetRecord.setGeolocation(geo);
            }
            geo.setAddress1("220 Chatham Business Drive");
            geo.setCity("Pittsboro");
            geo.setState("NC");
            geo.setZip("27312");
            geo.setCountry("US");
            m_assetRecordDao.update(assetRecord);
            m_assetRecordDao.flush();

            //Test findAll method
            int id = assetRecord.getId();
            OnmsAssetRecord assetRecordFromDb = m_assetRecordDao.get(id);
            assertNotNull(assetRecordFromDb.getGeolocation());
            assertEquals(geo.getAddress1(), assetRecordFromDb.getGeolocation().getAddress1());
            assertEquals(geo.getCity(), assetRecordFromDb.getGeolocation().getCity());
            assertEquals(geo.getState(), assetRecordFromDb.getGeolocation().getState());
            assertEquals(geo.getZip(), assetRecordFromDb.getGeolocation().getZip());
            assertEquals(geo.getCountry(), assetRecordFromDb.getGeolocation().getCountry());
        }

}
