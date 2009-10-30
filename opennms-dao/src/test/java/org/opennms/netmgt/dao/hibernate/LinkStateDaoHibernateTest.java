package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState;
import org.opennms.netmgt.model.OnmsLinkState.LinkState;


public class LinkStateDaoHibernateTest extends AbstractTransactionalDaoTestCase {

    
    public void testSaveLinkState() {
        Collection<DataLinkInterface> dlis = getDataLinkInterfaceDao().findAll();
        assertNotNull(dlis);
        assertFalse(dlis.isEmpty());
        
        DataLinkInterface dli = dlis.iterator().next();
        assertNotNull(dli);
        OnmsLinkState linkState1 = new OnmsLinkState();
        linkState1.setDataLinkInterface(dli);
        
        getLinkStateDao().save(linkState1);
        getLinkStateDao().flush();
        
        Integer id = linkState1.getId();
        assertNotNull(id);
        
        OnmsLinkState linkState2 = getLinkStateDao().get(id);
        
        assertNotNull(linkState2);
        assertEquals(LinkState.LINK_UP, linkState2.getLinkState());
        
        linkState2.setLinkState(LinkState.LINK_NODE_DOWN);
        getLinkStateDao().save(linkState2);
        getLinkStateDao().flush();
        
        OnmsLinkState linkState3 = getLinkStateDao().get(id);
        assertNotNull(linkState3);
        assertEquals(LinkState.LINK_NODE_DOWN, linkState3.getLinkState());
        
    }
    
    public void testSaveThenRead() {
        Collection<DataLinkInterface> dlis = getDataLinkInterfaceDao().findAll();
        assertNotNull(dlis);
        assertFalse(dlis.isEmpty());
        
        DataLinkInterface dli = dlis.iterator().next();
        assertNotNull(dli);
        OnmsLinkState linkState1 = new OnmsLinkState();
        linkState1.setDataLinkInterface(dli);
        
        getLinkStateDao().save(linkState1);
        
        setComplete();
        
        endTransaction();
        
        startNewTransaction();
        
        Integer id = linkState1.getId();
        assertNotNull(id);
        
        OnmsLinkState linkState2 = getLinkStateDao().get(id);
        
        assertNotNull(linkState2);
        assertEquals(LinkState.LINK_UP, linkState2.getLinkState());
    }
    
    public void testFindByDataLinkInterfaceId() {
        int dataLinkId;
        Collection<DataLinkInterface> dlis = getDataLinkInterfaceDao().findAll();
        assertNotNull(dlis);
        assertFalse(dlis.isEmpty());
        
        DataLinkInterface dli = dlis.iterator().next();
        dataLinkId = dli.getId();
        assertNotNull(dli);
        OnmsLinkState linkState1 = new OnmsLinkState();
        linkState1.setDataLinkInterface(dli);
        
        getLinkStateDao().save(linkState1);
        
        setComplete();
        
        endTransaction();
        
        startNewTransaction();
        
        OnmsLinkState linkState2 = getLinkStateDao().findByDataLinkInterfaceId(dataLinkId);
        assertNotNull(linkState2);
        
    }
    
}
