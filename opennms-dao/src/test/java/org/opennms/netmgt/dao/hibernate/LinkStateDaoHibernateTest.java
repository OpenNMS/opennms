/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.LinkStateDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkStateDaoHibernateTest implements InitializingBean {
    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    @Autowired
	private LinkStateDao m_linkStateDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}

    @Test
    @Transactional
	public void testSaveLinkState() {
        Collection<DataLinkInterface> dlis = m_dataLinkInterfaceDao.findAll();
        assertNotNull(dlis);
        assertFalse(dlis.isEmpty());
        
        DataLinkInterface dli = dlis.iterator().next();
        assertNotNull(dli);
        OnmsLinkState linkState1 = new OnmsLinkState();
        linkState1.setDataLinkInterface(dli);
        
        m_linkStateDao.save(linkState1);
        m_linkStateDao.flush();
        
        Integer id = linkState1.getId();
        assertNotNull(id);
        
        OnmsLinkState linkState2 = m_linkStateDao.get(id);
        
        assertNotNull(linkState2);
        assertEquals(LinkState.LINK_UP, linkState2.getLinkState());
        
        linkState2.setLinkState(LinkState.LINK_NODE_DOWN);
        m_linkStateDao.save(linkState2);
        m_linkStateDao.flush();
        
        OnmsLinkState linkState3 = m_linkStateDao.get(id);
        assertNotNull(linkState3);
        assertEquals(LinkState.LINK_NODE_DOWN, linkState3.getLinkState());
        
    }
    
    @Test
    @Transactional
	public void testSaveThenRead() {
        Collection<DataLinkInterface> dlis = m_dataLinkInterfaceDao.findAll();
        assertNotNull(dlis);
        assertFalse(dlis.isEmpty());
        
        DataLinkInterface dli = dlis.iterator().next();
        assertNotNull(dli);
        OnmsLinkState linkState1 = new OnmsLinkState();
        linkState1.setDataLinkInterface(dli);
        
        m_linkStateDao.save(linkState1);
        m_linkStateDao.flush();

        Integer id = linkState1.getId();
        assertNotNull(id);
        
        OnmsLinkState linkState2 = m_linkStateDao.get(id);
        
        assertNotNull(linkState2);
        assertEquals(LinkState.LINK_UP, linkState2.getLinkState());
    }
    
    @Test
    @Transactional
    public void testFindByDataLinkInterfaceId() {
        int dataLinkId;
        Collection<DataLinkInterface> dlis = m_dataLinkInterfaceDao.findAll();
        assertNotNull(dlis);
        assertFalse(dlis.isEmpty());
        
        DataLinkInterface dli = dlis.iterator().next();
        dataLinkId = dli.getId();
        assertNotNull(dli);
        OnmsLinkState linkState1 = new OnmsLinkState();
        linkState1.setDataLinkInterface(dli);
        
        m_linkStateDao.save(linkState1);
        m_linkStateDao.flush();
        
        OnmsLinkState linkState2 = m_linkStateDao.findByDataLinkInterfaceId(dataLinkId);
        assertNotNull(linkState2);
        
    }

}
