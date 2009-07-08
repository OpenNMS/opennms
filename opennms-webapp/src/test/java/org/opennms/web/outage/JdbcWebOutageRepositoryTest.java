/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.outage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.OutageIdFilter;
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
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/applicationContext-dao.xml",
                                  "classpath:/jdbcWebOutageRepositoryTest.xml"})
@JUnitTemporaryDatabase()
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
    public void testCountMatchingOutages(){
        OutageCriteria criteria = new OutageCriteria(new OutageIdFilter(1));
        int outages = m_outageRepo.countMatchingOutages(criteria);
        
        assertEquals(1, outages);
    }
    
    @Test
    public void testGetOutage(){
        Outage[] outages = m_outageRepo.getMatchingOutages(new OutageCriteria(new OutageIdFilter(1)));
        assertNotNull(outages);
        assertEquals(1, outages.length);
        
        Outage outage = m_outageRepo.getOutage(1);
        assertNotNull(outage);
    }
    
    @Test
    public void testGetOutages() {
        Outage[] outages = m_outageRepo.getMatchingOutages(new OutageCriteria());
        assertNotNull(outages);
        assertEquals(2, outages.length);
        
        assertNotNull(outages[0].getRegainedServiceTime());
        assertNull(outages[1].getRegainedServiceTime());
    }
    
    @Test
    public void testGetOutageSummaries() {
        OutageSummary[] summaries = m_outageRepo.getMatchingOutageSummaries(new OutageCriteria());
        assertEquals("there should be 1 outage summary in the default (current) outage criteria match", 1, summaries.length);
    }
}
