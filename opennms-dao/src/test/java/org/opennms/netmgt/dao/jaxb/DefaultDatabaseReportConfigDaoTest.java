/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
