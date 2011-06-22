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
// 2007 Apr 05: Change the property for the logs directory. - dj@opennms.org
// 2007 Feb 01: Add new property that is needed and make testGetRangeOutages work. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.web.svclayer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.outage.OutageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-reportingCore.xml",
        "classpath:org/opennms/web/svclayer/applicationContext-svclayer.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-insertData-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultOutageServiceIntegrationTest {
    private static final int RANGE_LIMIT = 5;

    @Autowired
    OutageService m_outageService;

    @Autowired
    OutageDao m_outageDao;

    @Autowired
    DatabasePopulator m_databasePopulator;

    @Before
    public void setUp() throws Exception {
        m_databasePopulator.populateDatabase();
    }

    @Test
    @Transactional
    public void testGetRangeOutages() {
        Collection<OnmsOutage> outages = m_outageService.getOutagesByRange(1,RANGE_LIMIT,"iflostservice","asc", new OnmsCriteria(OnmsOutage.class));
        assertFalse("Collection should not be emtpy", outages.isEmpty());
        //assertEquals("Collection should be of size " + RANGE_LIMIT, RANGE_LIMIT, outages.size());
    }

    // More tests should be defined for these

    @Test
    @Transactional
    @Ignore
    public void testGetSupressedOutages() {
        Collection<OnmsOutage> outages = m_outageService.getSuppressedOutages();
        assertTrue("Collection should be emtpy ", outages.isEmpty());

    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testLoadOneOutage() {
        OnmsOutage outage = m_outageService.load(1);
        assertTrue("We loaded one outage ",outage.getId().equals(1));
    }

    @Test
    @Transactional
    @Ignore
    public void testNoOfSuppressedOutages(){
        Integer outages = m_outageService.getSuppressedOutageCount();
        assertTrue("We should find suppressed messages ", outages == 0);
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testSuppression() {
        Date time = new Date();
        //Load Outage manipulate and save it.
        OnmsOutage myOutage = m_outageService.load(new Integer(1));
        assertTrue("Loaded the outage ", myOutage.getId().equals(new Integer(1)));
        myOutage.setSuppressTime(time);
        m_outageService.update(myOutage);
        m_outageService.load(new Integer(1));
    }
}
