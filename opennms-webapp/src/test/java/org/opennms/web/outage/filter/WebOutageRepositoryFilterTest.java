/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.outage.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.web.outage.DaoWebOutageRepository;
import org.opennms.web.outage.JdbcWebOutageRepository;
import org.opennms.web.outage.Outage;
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
@ContextConfiguration(locations={"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                 "classpath*:/META-INF/opennms/component-dao.xml",
                                 "classpath:/daoWebOutageRepositoryTest.xml",
                                 "classpath:/jdbcWebOutageRepositoryTest.xml"})
@JUnitTemporaryDatabase()
public class WebOutageRepositoryFilterTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    DaoWebOutageRepository m_daoOutageRepo;
    
    @Autowired
    JdbcWebOutageRepository m_jdbcOutageRepo;
    
    @BeforeClass
    public static void setupLogging(){
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
        OnmsMonitoredService svc2 = m_dbPopulator.getMonitoredServiceDao().get(2, "192.168.2.1", "ICMP");
        OnmsEvent event = m_dbPopulator.getEventDao().get(1);
        
        OnmsOutage unresolved2 = new OnmsOutage(new Date(), event, svc2);
        m_dbPopulator.getOutageDao().save(unresolved2);
        m_dbPopulator.getOutageDao().flush();
    }
    
    @Test
    public void testAutoWirred(){
        assertNotNull(m_daoOutageRepo);
        assertNotNull(m_jdbcOutageRepo);
    }
    
    @Test
    public void testOutageIdFilter(){
        OutageIdFilter filter = new OutageIdFilter(1);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    public void testLostServiceDateBeforeFilter(){
        LostServiceDateBeforeFilter filter = new LostServiceDateBeforeFilter(new Date());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
    }
    
    @Test
    public void testLostServiceDateAfterFilter(){
        LostServiceDateAfterFilter filter = new LostServiceDateAfterFilter(yesterday());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
    }
    
    @Test
    public void testRegainedServiceDateBeforeFilter(){
        RegainedServiceDateBeforeFilter filter = new RegainedServiceDateBeforeFilter(new Date());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    public void testRegainedServiceDateAfterFilter(){
        RegainedServiceDateAfterFilter filter = new RegainedServiceDateAfterFilter(yesterday());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    private Date yesterday() {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DATE, -1 );
        return cal.getTime();
    }

    @Test
    public void testRecentOutagesFilter(){
        RecentOutagesFilter filter = new RecentOutagesFilter();
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
    }
    @Test
    public void testNegativeInterfaceFilter(){
        NegativeInterfaceFilter filter = new NegativeInterfaceFilter("192.168.2.1");
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
    }
    
    @Test
    public void testNegativeNodeFilter(){
        NegativeNodeFilter filter = new NegativeNodeFilter(2);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        NegativeNodeFilter filter2 = new NegativeNodeFilter(1);
        OutageCriteria criteria2 = new OutageCriteria(filter2);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_daoOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
    }
    
    @Test
    public void testNegativeServiceFilter(){
        NegativeServiceFilter filter = new NegativeServiceFilter(2);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    public void testNodeFilter(){
        NodeFilter filter = new NodeFilter(1);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        NodeFilter filter2 = new NodeFilter(2);
        OutageCriteria criteria2 = new OutageCriteria(filter2);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_daoOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
    }
    
    @Test
    public void testInterfaceFilter(){
        InterfaceFilter filter = new InterfaceFilter("192.168.1.1");
        OutageCriteria criteria = new OutageCriteria(filter);
        
        InterfaceFilter filter2 = new InterfaceFilter("192.168.2.1");
        OutageCriteria criteria2 = new OutageCriteria(filter2);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_daoOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
    }
    
    @Test
    public void testServiceFilter(){
        ServiceFilter filter = new ServiceFilter(2);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_jdbcOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
    }
}
