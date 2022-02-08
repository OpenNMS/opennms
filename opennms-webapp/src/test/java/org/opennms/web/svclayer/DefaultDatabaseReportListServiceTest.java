/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;
import org.opennms.web.svclayer.model.DatabaseReportDescription;
import org.opennms.web.svclayer.support.DefaultDatabaseReportListService;

//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
// TODO indigo: Improve tests and refactor for spring injection
public class DefaultDatabaseReportListServiceTest {

    private DefaultDatabaseReportListService m_defaultDatabaseReportListService;

    private GlobalReportRepository m_globalReportRepository;

    @Before
    public void setupDao() throws Exception {
        m_globalReportRepository = mock(GlobalReportRepository.class);
        when(m_globalReportRepository.getAllOnlineReports()).thenReturn(new ArrayList<BasicReportDefinition>());
        when(m_globalReportRepository.getAllOnlineReports()).thenReturn(new ArrayList<BasicReportDefinition>());
        m_defaultDatabaseReportListService = new DefaultDatabaseReportListService();
        m_defaultDatabaseReportListService.setGlobalReportRepository(m_globalReportRepository);

    }

    @Ignore
    @Test
    public void testGetAllOnline() throws Exception {
        List<DatabaseReportDescription> description = m_defaultDatabaseReportListService.getAll();
        assertEquals(1, description.size());
    }
}
