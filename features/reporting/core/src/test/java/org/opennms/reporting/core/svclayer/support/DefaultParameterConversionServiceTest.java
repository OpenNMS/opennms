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
package org.opennms.reporting.core.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.netmgt.dao.jaxb.DefaultOnmsReportConfigDao;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DefaultParameterConversionServiceTest {
    
    private static DefaultOnmsReportConfigDao m_dao;
    private static DefaultParameterConversionService m_conversionService;
    
    private static final String ID = "defaultCalendarReport";
    
    @BeforeClass
    public static void setUp() throws Exception {
        Resource resource = new ClassPathResource("/opennms-reports-testdata.xml");
        m_dao = new DefaultOnmsReportConfigDao();
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
        m_conversionService = new DefaultParameterConversionService();
    }
    
    @Test
    public void testGetDateParms() {
        assertNotNull(m_dao.getDateParms(ID));
        assertEquals(1,m_dao.getDateParms(ID).size());
    }
    
    @Test
    public void testDefaultDateConversion() {
        ReportParameters parameters = m_conversionService.convert(m_dao.getParameters(ID));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        System.out.println("test date " + cal.getTime().toString());
        cal.add(Calendar.DATE, -1);
        Date configDate = parameters.getDateParms().get(0).getDate();
        assertEquals(0, configDate.compareTo(cal.getTime()));
    }
    
    @Test
    public void testModifiedDateConversion() {
        ReportParameters parameters = m_conversionService.convert(m_dao.getParameters(ID));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        ReportDateParm dateParm = parameters.getDateParms().get(0);
        dateParm.setCount(1);
        dateParm.setInterval("month");
        dateParm.setHours(0);
        dateParm.setMinutes(0);
        Map <String, Object> parmMap = parameters.getReportParms(ReportMode.SCHEDULED);
        Date storedDate = (Date) parmMap.get("endDate");
        assertEquals(0, storedDate.compareTo(cal.getTime()));
    }
    

}
