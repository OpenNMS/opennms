/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.core.svclayer.support;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.api.DatabaseReportConfigDao;
import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

// TODO: We have replaced the databaseConfigDao by a GlobalReportRepository. We have to rewrite the whole test set with a mockup GlobalReportRepository
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:org/opennms/reporting/core/svclayer/support/DefaultReportStoreServiceTest.xml"
})
public class DefaultReportStoreServiceTest implements InitializingBean {

    @Autowired
    ReportStoreService m_reportStoreService;
    
    @Autowired
    ReportCatalogDao m_reportCatalogDao;
    
    @Autowired
    ReportServiceLocator m_reportServiceLocator;
    
    @Autowired
    DatabaseReportConfigDao m_databaseReportConfigDao;

    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", "src/test/resources");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void testSave(){
        
        ReportCatalogEntry reportCatalogEntry = new ReportCatalogEntry();
        m_reportCatalogDao.save(reportCatalogEntry);
        m_reportCatalogDao.flush();
        replay(m_reportCatalogDao);
        
        m_reportStoreService.save(reportCatalogEntry);
        verify(m_reportCatalogDao);
        
    }
    
    @Test
    public void testReder(){
        // TODO something useful here
    }
    
}
