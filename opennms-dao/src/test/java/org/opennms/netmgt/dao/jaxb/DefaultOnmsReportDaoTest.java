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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.config.reporting.DateParm;
import org.opennms.netmgt.config.reporting.IntParm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DefaultOnmsReportDaoTest {
    
    private static final String ID = "defaultCalendarReport";
    private static final String ALTERNATE_ID = "defaultClassicReport";
    private static final String TYPE = "calendar";
    private static final String SVG_TEMPLATE = "SVGAvailReport.xsl";
    private static final String PDF_TEMPLATE = "PDFAvailReport.xsl";
    private static final String HTML_TEMPLATE = "HTMLAvailReport.xsl";
    private static final String LOGO = "logo.png";
    private static final String DATE_DISPLAY_NAME = "end date";
    private static final String DATE_NAME = "endDate";
    private static final String STRING_NAME = "offenderCount";
    private static final String STRING_DISPLAY_NAME = "top offender count";
    private static DefaultOnmsReportConfigDao m_dao;
    

    @BeforeClass
    public static void setUp() throws Exception {
        Resource resource = new ClassPathResource("/opennms-reports-testdata.xml");
        m_dao = new DefaultOnmsReportConfigDao();
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
    }
    
    @Test
    public void testGetRenderParms() throws Exception {

        assertEquals(m_dao.getType(ID),TYPE);
        assertEquals(m_dao.getSvgStylesheetLocation(ID), SVG_TEMPLATE);
        assertEquals(m_dao.getPdfStylesheetLocation(ID), PDF_TEMPLATE);
        assertEquals(m_dao.getHtmlStylesheetLocation(ID), HTML_TEMPLATE);
        assertEquals(m_dao.getLogo(ID), LOGO);
        // test to see if missing parameters return null
        assertNull(m_dao.getSvgStylesheetLocation(ALTERNATE_ID));
    }
    
    @Test
    public void testGetReportParms() throws Exception {
        
        List<DateParm> dates = m_dao.getDateParms(ID);
        assertEquals(1, dates.size());
        final DateParm firstDate = dates.get(0);
        assertEquals(DATE_NAME,firstDate.getName());
        assertEquals(DATE_DISPLAY_NAME,firstDate.getDisplayName());
        assertEquals(false,firstDate.getUseAbsoluteDate().get());
        assertEquals(Integer.valueOf(1),firstDate.getDefaultCount());
        assertEquals("day",firstDate.getDefaultInterval());
        assertEquals(Integer.valueOf(23),firstDate.getDefaultTime().get().getHours());
        assertEquals(Integer.valueOf(59),firstDate.getDefaultTime().get().getMinutes());
        
        List<IntParm> integers = m_dao.getIntParms(ID);
        assertEquals(1,integers.size());
        final IntParm firstInt = integers.get(0);
        assertEquals(STRING_NAME,firstInt.getName());
        assertEquals(STRING_DISPLAY_NAME,firstInt.getDisplayName());
        assertEquals(Integer.valueOf(20),firstInt.getDefault());
        
    }

}
