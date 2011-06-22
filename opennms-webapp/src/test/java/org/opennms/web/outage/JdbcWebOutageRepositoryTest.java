package org.opennms.web.outage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.OutageIdFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/jdbcWebRepositoryTestContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JdbcWebOutageRepositoryTest{
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebOutageRepository m_outageRepo;
    
    @Before
    public void setUp(){
        assertNotNull(m_outageRepo);
        m_dbPopulator.populateDatabase();
    }
    
    @After
    public void tearDown(){
        
    }
   
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testCountMatchingOutages(){
        OutageCriteria criteria = new OutageCriteria(new OutageIdFilter(1));
        int outages = m_outageRepo.countMatchingOutages(criteria);
        
        assertEquals(1, outages);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetOutage(){
        Outage[] outages = m_outageRepo.getMatchingOutages(new OutageCriteria(new OutageIdFilter(1)));
        assertNotNull(outages);
        assertEquals(1, outages.length);
        
        Outage outage = m_outageRepo.getOutage(1);
        assertNotNull(outage);
    }
    
    @Test
    @Transactional
    public void testGetOutages() {
        Outage[] outages = m_outageRepo.getMatchingOutages(new OutageCriteria());
        assertNotNull(outages);
        assertEquals(2, outages.length);
        
        assertNotNull(outages[0].getRegainedServiceTime());
        assertNull(outages[1].getRegainedServiceTime());
    }
    
    @Test
    @Transactional
    public void testGetOutageSummaries() {
        OutageSummary[] summaries = m_outageRepo.getMatchingOutageSummaries(new OutageCriteria());
        assertEquals("there should be 1 outage summary in the default (current) outage criteria match", 1, summaries.length);
    }
}
