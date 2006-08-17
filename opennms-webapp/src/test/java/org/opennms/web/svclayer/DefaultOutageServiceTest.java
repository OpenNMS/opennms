package org.opennms.web.svclayer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.outage.DefaultOutageService;

public class DefaultOutageServiceTest extends TestCase {

    DefaultOutageService outageService = new DefaultOutageService();

    private OutageDao outageDao;

    public void setUp() throws Exception {
        super.setUp();
        outageDao = createMock(OutageDao.class);
        outageService.setDao(outageDao);
    }

    public void testGetCurrentOutageCount() {

        Integer expectedCount = new Integer(1);

        expect(outageDao.currentOutageCount()).andReturn(expectedCount);
        replay(outageDao);
        Integer count = outageService.getCurrentOutageCount();
        verify(outageDao);
        assertTrue("A good system should have outages ", count
                .equals(new Integer(1)));

    }
    

    public void testSuppressedOutageCount() {

        Integer expectedCount = new Integer(1);

        expect(outageDao.currentSuppressedOutageCount()).andReturn(
                expectedCount);
        replay(outageDao);
        Integer count = outageService.getSuppressedOutageCount();
        verify(outageDao);
        assertTrue("All is suppressed ", count.equals(1));

    }
    
    public void testCurrentOutages () {
        
        Collection<OnmsOutage> expectedOutages = new JdbcSet();
        OnmsOutage expectedCurrent =  new OnmsOutage();
        expectedCurrent.setId(1);
        expectedOutages.add(expectedCurrent);
        
        expect(outageDao.currentOutages()).andReturn(expectedOutages);
        replay(outageDao);
        
        Set current = (Set) outageService.getCurrentOutages();
        verify(outageDao);
        assertTrue("Current Outages", current.equals(expectedOutages));
    }
    
  public void testSuppressedOutages () {
        
        Collection<OnmsOutage> expectedOutages = new JdbcSet();
        OnmsOutage expectedCurrent =  new OnmsOutage();
        expectedCurrent.setId(1);
        expectedOutages.add(expectedCurrent);
        
        expect(outageDao.suppressedOutages()).andReturn(expectedOutages);
        replay(outageDao);
        
        Set suppressed = (Set) outageService.getSuppressedOutages();
        verify(outageDao);
        assertTrue("Current Outages", suppressed.equals(expectedOutages));
    }
  
  public void testOpenAndResolved () {
      
      Collection<OnmsOutage> expectedOutages = new JdbcSet();
      OnmsOutage expectedCurrent =  new OnmsOutage();
      expectedCurrent.setId(1);
      expectedOutages.add(expectedCurrent);
      
      expect(outageDao.openAndResolvedOutages()).andReturn(expectedOutages);
      replay(outageDao);
      
      Set suppressed = (Set) outageService.getOpenAndResolved();
      verify(outageDao);
      assertTrue("Current Outages", suppressed.equals(expectedOutages));
      
  }
  
public void testCurrentByRange () {
      
      Collection<OnmsOutage> expectedOutages = new JdbcSet();
      OnmsOutage expectedCurrent =  new OnmsOutage();
      expectedCurrent.setId(1);
    
      expectedOutages.add(expectedCurrent);
      
      expect(outageDao.currentOutages(1,1,"iflostservice","asc")).andReturn(expectedOutages);
      replay(outageDao);
      
      Set suppressed = (Set) outageService.getCurrentOutagesByRange(1, 1, "iflostservice","asc");
      verify(outageDao);
      assertTrue("Current Outages", suppressed.equals(expectedOutages));
      
  }

public void testSuppressedByRange () {
    
    Collection<OnmsOutage> expectedOutages = new JdbcSet();
    OnmsOutage expectedCurrent =  new OnmsOutage();
    expectedCurrent.setId(1);
    expectedOutages.add(expectedCurrent);
    
    expect(outageDao.suppressedOutages(1,1)).andReturn(expectedOutages);
    replay(outageDao);
    
    Set suppressed = (Set) outageService.getSuppressedOutagesByRange(1, 1);
    verify(outageDao);
    assertTrue("Current Outages", suppressed.equals(expectedOutages));
    
}

public void testGetOpenAndResolvedByRange () {
    
    Collection<OnmsOutage> expectedOutages = new JdbcSet();
    OnmsOutage expectedCurrent =  new OnmsOutage();
    expectedCurrent.setId(1);
    expectedOutages.add(expectedCurrent);
    
    expect(outageDao.findAll(1,1)).andReturn(expectedOutages);
    replay(outageDao);
    
    Set suppressed = (Set) outageService.getOpenAndResolved(1,1);
    verify(outageDao);
    assertTrue("Current Outages", suppressed.equals(expectedOutages));
    
}

}
