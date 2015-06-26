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

package org.opennms.web.outage.filter;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.WebOutageRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class WebOutageRepositoryFilterTest implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    @Qualifier("dao")
    WebOutageRepository m_daoOutageRepo;
    
    @Autowired
    ApplicationContext m_appContext;
    
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
        OnmsMonitoredService svc2 = m_dbPopulator.getMonitoredServiceDao().get(m_dbPopulator.getNode2().getId(), InetAddressUtils.addr("192.168.2.1"), "ICMP");
        // This requires every test method to have a new database instance :/
        OnmsEvent event = m_dbPopulator.getEventDao().get(1);
        
        OnmsOutage unresolved2 = new OnmsOutage(new Date(), event, svc2);
        m_dbPopulator.getOutageDao().save(unresolved2);
        m_dbPopulator.getOutageDao().flush();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testOutageIdFilter(){
        OutageIdFilter filter = new OutageIdFilter(1);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testLostServiceDateBeforeFilter(){
        LostServiceDateBeforeFilter filter = new LostServiceDateBeforeFilter(new Date());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testLostServiceDateAfterFilter(){
        LostServiceDateAfterFilter filter = new LostServiceDateAfterFilter(yesterday());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testRegainedServiceDateBeforeFilter(){
        RegainedServiceDateBeforeFilter filter = new RegainedServiceDateBeforeFilter(new Date());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testRegainedServiceDateAfterFilter(){
        RegainedServiceDateAfterFilter filter = new RegainedServiceDateAfterFilter(yesterday());
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    private static Date yesterday() {
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DATE, -1 );
        return cal.getTime();
    }

    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testRecentOutagesFilter(){
        RecentOutagesFilter filter = new RecentOutagesFilter();
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(3, outages.length);
    }

    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testNegativeInterfaceFilter(){
        NegativeInterfaceFilter filter = new NegativeInterfaceFilter("192.168.2.1");
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @Ignore
    public void testNegativeNodeFilter(){
//        NegativeNodeFilter filter = new NegativeNodeFilter(2);
//        OutageCriteria criteria = new OutageCriteria(filter);
//        
//        NegativeNodeFilter filter2 = new NegativeNodeFilter(1);
//        OutageCriteria criteria2 = new OutageCriteria(filter2);
//        
//        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
//        assertEquals(2, outages.length);
//        
//        outages = m_daoOutageRepo.getMatchingOutages(criteria2);
//        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNegativeServiceFilter(){
        NegativeServiceFilter filter = new NegativeServiceFilter(2, null);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    @Ignore
    public void testNodeFilter(){
//        NodeFilter filter = new NodeFilter(1, m_appContext);
//        OutageCriteria criteria = new OutageCriteria(filter);
//        
//        NodeFilter filter2 = new NodeFilter(2, m_appContext);
//        OutageCriteria criteria2 = new OutageCriteria(filter2);
//        
//        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
//        assertEquals(2, outages.length);
//        
//        outages = m_daoOutageRepo.getMatchingOutages(criteria2);
//        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testInterfaceFilter(){
        InterfaceFilter filter = new InterfaceFilter("192.168.1.1");
        OutageCriteria criteria = new OutageCriteria(filter);
        
        InterfaceFilter filter2 = new InterfaceFilter("192.168.2.1");
        OutageCriteria criteria2 = new OutageCriteria(filter2);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
        
        outages = m_daoOutageRepo.getMatchingOutages(criteria2);
        assertEquals(1, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testServiceFilter(){
        ServiceFilter filter = new ServiceFilter(2, null);
        OutageCriteria criteria = new OutageCriteria(filter);
        
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(criteria);
        assertEquals(2, outages.length);
    }
}
