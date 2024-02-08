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
package org.opennms.features.reporting.rest.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.ReportParameterBuilder;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.reporting.core.svclayer.ReportStoreService;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.web.svclayer.DatabaseReportListService;
import org.opennms.web.svclayer.SchedulerService;
import org.opennms.web.svclayer.dao.CategoryConfigDao;

public class ReportRestServiceImplTest {
    private ReportRestServiceImpl m_service;

    private DatabaseReportListService m_databaseReportListService;
    private ReportWrapperService m_reportWrapperService;
    private CategoryDao m_categoryDao;
    private CategoryConfigDao m_categoryConfigDao;
    private ReportStoreService m_reportStoreService;
    private SchedulerService m_schedulerService;
    private ReportCatalogDao m_reportCatalogDao;


    @Before
    public void setup() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

        m_databaseReportListService = mock(DatabaseReportListService.class);
        m_reportWrapperService = mock(ReportWrapperService.class);
        m_categoryDao = mock(CategoryDao.class);
        m_categoryConfigDao = mock(CategoryConfigDao.class);
        m_reportStoreService = mock(ReportStoreService.class);
        m_schedulerService = mock(SchedulerService.class);
        m_reportCatalogDao = mock(ReportCatalogDao.class);
        m_service = new ReportRestServiceImpl(m_databaseReportListService, m_reportWrapperService, m_categoryDao, m_categoryConfigDao, m_reportStoreService, m_schedulerService, m_reportCatalogDao);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_databaseReportListService);
        verifyNoMoreInteractions(m_reportWrapperService);
        verifyNoMoreInteractions(m_categoryDao);
        verifyNoMoreInteractions(m_categoryConfigDao);
        verifyNoMoreInteractions(m_reportStoreService);
        verifyNoMoreInteractions(m_schedulerService);
        verifyNoMoreInteractions(m_reportCatalogDao);
    }

    @Test
    public void testParseParametersDefaultZone() throws Exception {
        final ReportParameters actualParameters = new ReportParameterBuilder()
            .withDate("startDate", new Date(0))
            .build();
        when(m_reportWrapperService.getParameters(anyString())).thenReturn(actualParameters);

        Map<String,Object> reportParameters = new HashMap<>();
        reportParameters.put("id", "12345");
        reportParameters.put("format", "PDF");

        final Map<String,Object> startDate = buildParameter("date", "startDate");
        startDate.put("date", "2020-11-09");
        startDate.put("hours", 2);
        startDate.put("minutes", 0);

        reportParameters.put("parameters", Arrays.asList(startDate));

        final ReportParameters parameters = m_service.parseParameters(reportParameters, ReportMode.IMMEDIATE);
        assertNotNull(parameters);
        assertEquals(1, parameters.getDateParms().size());

        final ReportDateParm reportDateParm = parameters.getDateParms().get(0);
		assertEquals(new Date(1604905200000l), reportDateParm.getValue(ReportMode.IMMEDIATE));

	verify(m_reportWrapperService, atLeastOnce()).getParameters(anyString());
    }

    @Test
    public void testParseParametersDifferentZone() throws Exception {
        final ReportParameters actualParameters = new ReportParameterBuilder()
            .withDate("startDate", new Date(0))
            .withTimezone("timezone",  ZoneId.systemDefault())
            .build();
        when(m_reportWrapperService.getParameters(anyString())).thenReturn(actualParameters);

        Map<String,Object> reportParameters = new HashMap<>();
        reportParameters.put("id", "12345");
        reportParameters.put("format", "PDF");

        final Map<String,Object> startDate = buildParameter("date", "startDate");
        startDate.put("date", "2020-11-09");
        startDate.put("hours", 2);
        startDate.put("minutes", 0);

        final Map<String,Object> timezone = buildParameter("timezone", "timezone");
        timezone.put("value", "America/Los_Angeles");

        reportParameters.put("parameters", Arrays.asList(startDate, timezone));

        final ReportParameters parameters = m_service.parseParameters(reportParameters, ReportMode.IMMEDIATE);
        assertNotNull(parameters);
        assertEquals(1, parameters.getDateParms().size());
        final ReportDateParm reportDateParm = parameters.getDateParms().get(0);
        assertEquals(Integer.valueOf(2), reportDateParm.getHours());
        assertEquals(Integer.valueOf(0), reportDateParm.getMinutes());
        assertEquals(new Date(1604916000000l), reportDateParm.getDate());
        assertEquals(new Date(1604916000000l), reportDateParm.getValue(ReportMode.IMMEDIATE));

        verify(m_reportWrapperService, atLeastOnce()).getParameters(anyString());
    }

    private Map<String,Object> buildParameter(final String type, final String name) {
        final Map<String,Object> ret = new HashMap<>();
        ret.put("type", type);
        ret.put("name", name);
        return ret;
    }
}
