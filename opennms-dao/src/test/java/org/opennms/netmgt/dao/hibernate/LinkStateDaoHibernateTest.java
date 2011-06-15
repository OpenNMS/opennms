package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.LinkStateDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkStateDaoHibernateTest {
    @Autowired
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    @Autowired
	private LinkStateDao m_linkStateDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
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
