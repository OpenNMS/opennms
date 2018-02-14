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

package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DefaultDatabaseReportConfigDaoTest {
    
    private static final String NAME = "defaultCalendarReport";
    private static final String REPORT_SERVICE = "availabilityReportService";
    private static DefaultDatabaseReportConfigDao m_dao;
    
    @BeforeClass
    public static void setUpDao() {
        
        m_dao = new DefaultDatabaseReportConfigDao();
        Resource resource = new ClassPathResource("/database-reports-testdata.xml");
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
        
    }
    
    @Test
    public void testGetReports() throws Exception {
        
        assertEquals(2,m_dao.getReports().size());
        
    }
    
    @Test
    public void testGetOnlineReports() throws Exception {
        
        assertEquals(1,m_dao.getOnlineReports().size());
        
    }

    @Test
    public void testGetReportService() throws Exception {
        
        assertEquals(REPORT_SERVICE,m_dao.getReportService(NAME));
        
    }

}
