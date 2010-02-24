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
// 2007 Aug 23: Extend AbstractTransactionalTemporaryDatabaseSpringContextTests
//              so we get a temporary database. - dj@opennms.org
// 2007 Apr 05: Change the property for the logs directory. - dj@opennms.org
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
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath*:/META-INF/opennms/component-service.xml",
                "org/opennms/web/svclayer/applicationContext-svclayer.xml",
                "META-INF/opennms/applicationContext-reportingCore.xml",
                "classpath:/META-INF/opennms/applicationContext-insertData-enabled.xml"
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