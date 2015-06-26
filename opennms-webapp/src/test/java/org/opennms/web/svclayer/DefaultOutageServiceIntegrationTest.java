/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.svclayer.outage.OutageService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-reportingCore.xml",
        "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-insertData-enabled.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
@DirtiesContext
public class DefaultOutageServiceIntegrationTest implements InitializingBean {
    private static final int RANGE_LIMIT = 5;

    @Autowired
    OutageService m_outageService;

    @Autowired
    OutageDao m_outageDao;

    @Autowired
    DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_databasePopulator.populateDatabase();
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
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
    @JUnitTemporaryDatabase
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
    @JUnitTemporaryDatabase
    public void testSuppression() {
        Date time = new Date();
        //Load Outage manipulate and save it.
        OnmsOutage myOutage = m_outageService.load(Integer.valueOf(1));
        assertTrue("Loaded the outage ", myOutage.getId().equals(Integer.valueOf(1)));
        myOutage.setSuppressTime(time);
        m_outageService.update(myOutage);
        m_outageService.load(Integer.valueOf(1));
    }
}
