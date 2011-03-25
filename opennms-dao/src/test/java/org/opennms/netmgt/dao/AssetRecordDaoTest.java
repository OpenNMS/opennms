//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 25: Convert to use AbstractTransactionalDaoTestCase. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
public class AssetRecordDaoTest {
    
	@Autowired
	private DistPollerDao m_distPollerDao;
	
	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private AssetRecordDao m_assetRecordDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}

	@Test
    public void testCreateAndGets() {
        OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
        onmsNode.setLabel("myNode");
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
	public void testAddUserName() {
        OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
        onmsNode.setLabel("myNode");
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
    public void testAddAutoenable() {
        OnmsNode onmsNode = new OnmsNode(m_distPollerDao.load("localhost"));
        onmsNode.setLabel("myNode");
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
    

}
