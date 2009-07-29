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
package org.opennms.web.outage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
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
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.OutageIdFilter;
import org.opennms.web.outage.filter.RegainedServiceDateBeforeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

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
                                 "classpath:/daoWebOutageRepositoryTest.xml"})
@JUnitTemporaryDatabase()
public class DaoWebOutageRepositoryTest {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebOutageRepository m_daoOutageRepo;
    
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
    public void testCountMatchingOutages(){
        int count = m_daoOutageRepo.countMatchingOutages(new OutageCriteria());
        assertEquals(3, count);
        
        count = m_daoOutageRepo.countMatchingOutages(new OutageCriteria(new RegainedServiceDateBeforeFilter(new Date())));
        assertEquals(1, count);
    }
    
    @Test
    public void testGetMatchingOutages(){
        Outage[] outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria());
        assertEquals(3, outage.length);
        
        outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new RegainedServiceDateBeforeFilter(new Date())));
        assertEquals(1, outage.length);
        
        outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new OutageIdFilter(1)));
        assertEquals(1, outage.length);
        assertEquals(1, outage[0].getId());
        
        outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new OutageIdFilter(2)));
        assertEquals(1, outage.length);
        assertEquals(2, outage[0].getId());
    }
    
    @Test
    @Transactional
    public void testGetOutage(){
        Outage outage = m_daoOutageRepo.getOutage(1);
        assertNotNull(outage);
    }
    
    @Test
    public void testGetOutageSummaries() {
        OutageSummary[] summaries = m_daoOutageRepo.getMatchingOutageSummaries(new OutageCriteria());
        assertEquals("there should be 2 outage summary in the default (current) outage criteria match", 2, summaries.length);
    }
    
    @Test
    public void testCountMatchingSummaries(){
        
        int count = m_daoOutageRepo.countMatchingOutageSummaries(new OutageCriteria());
        assertEquals(2, count);
    }
}
