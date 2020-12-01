/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.rest.internal;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.web.svclayer.DatabaseReportListService;
import org.opennms.web.svclayer.SchedulerService;
import org.opennms.web.svclayer.dao.CategoryConfigDao;

public class ReportRestServiceImplTest {
    private EasyMockUtils m_easyMock;

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

        m_easyMock = new EasyMockUtils();
        m_databaseReportListService = m_easyMock.createMock(DatabaseReportListService.class);
        m_reportWrapperService = m_easyMock.createMock(ReportWrapperService.class);
        m_categoryDao = m_easyMock.createMock(CategoryDao.class);
        m_categoryConfigDao = m_easyMock.createMock(CategoryConfigDao.class);
        m_reportStoreService = m_easyMock.createMock(ReportStoreService.class);
        m_schedulerService = m_easyMock.createMock(SchedulerService.class);
        m_reportCatalogDao = m_easyMock.createMock(ReportCatalogDao.class);
        m_service = new ReportRestServiceImpl(m_databaseReportListService, m_reportWrapperService, m_categoryDao, m_categoryConfigDao, m_reportStoreService, m_schedulerService, m_reportCatalogDao);
    }

    @Test
    public void testParseParametersDefaultZone() throws Exception {
        final ReportParameters actualParameters = new ReportParameterBuilder()
            .withDate("startDate", new Date(0))
            .build();
        expect(m_reportWrapperService.getParameters(anyString())).andReturn(actualParameters);
        m_easyMock.replayAll();

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
    }

    @Test
    public void testParseParametersDifferentZone() throws Exception {
        final ReportParameters actualParameters = new ReportParameterBuilder()
            .withDate("startDate", new Date(0))
            .withTimezone("timezone",  ZoneId.systemDefault())
            .build();
        expect(m_reportWrapperService.getParameters(anyString())).andReturn(actualParameters);
        m_easyMock.replayAll();

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
    }

    private Map<String,Object> buildParameter(final String type, final String name) {
        final Map<String,Object> ret = new HashMap<>();
        ret.put("type", type);
        ret.put("name", name);
        return ret;
    }
}
