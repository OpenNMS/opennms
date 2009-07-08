/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.web.svclayer.support;

import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.WebAppTestConfigBean;
import org.opennms.web.svclayer.ProgressMonitor;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SurveillanceService;

public class DefaultSurveillanceServiceIntegrationTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    
    private SurveillanceService m_surveillanceService;
    private DatabasePopulator m_databasePopulator; 
    
    @Override
    protected void setUpConfiguration() {
        WebAppTestConfigBean webAppTestConfig = new WebAppTestConfigBean();
        webAppTestConfig.setRelativeHomeDirectory("src/test/opennms-home");
        webAppTestConfig.afterPropertiesSet();
    }

    /**
     * This parm gets autowired from the application context by TDSCT (the base class for this test)
     * pretty cool Spring Framework trickery
     * @param svc
     */
    public void setSurveillanceService(SurveillanceService svc) {
        m_surveillanceService = svc;
    }
    
    public void setDatabasePopulator(DatabasePopulator databasePopulator){
        m_databasePopulator = databasePopulator;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "META-INF/opennms/applicationContext-dao.xml",
                "META-INF/opennms/applicationContext-databasePopulator.xml",
                "META-INF/opennms/component-dao.xml",
                "org/opennms/web/svclayer/applicationContext-svclayer.xml",
        };
    }
    
    public void testCreateSurveillanceServiceTableUsingViewName() {
       assertNotNull(m_databasePopulator);
       m_databasePopulator.populateDatabase();
       
        String viewName = "default";
        SimpleWebTable table = m_surveillanceService.createSurveillanceTable(viewName, new ProgressMonitor() {

			public void beginNextPhase(String string) {
							
			}

			public void setPhaseCount(int i) {
								
			}
        	
        });
        
        
        assertEquals("default", table.getTitle());
    }

}