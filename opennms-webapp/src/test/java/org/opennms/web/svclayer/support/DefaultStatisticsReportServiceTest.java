/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.SortedSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.StatisticsReportDao;
import org.opennms.netmgt.model.ResourceReference;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.web.command.StatisticsReportCommand;
import org.opennms.web.svclayer.support.StatisticsReportModel.Datum;
import org.springframework.validation.BindException;

/**
 * Test case for DefaultStatisticsReportService.
 * 
 * @see DefaultStatisticsReportService
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 */
public class DefaultStatisticsReportServiceTest {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    private DefaultStatisticsReportService m_service = new DefaultStatisticsReportService();
    private ResourceDao m_resourceDao = m_mocks.createMock(ResourceDao.class);
    private StatisticsReportDao m_statisticsReportDao = m_mocks.createMock(StatisticsReportDao.class);

    @Before
    public void setUp() throws Exception {
        m_service.setResourceDao(m_resourceDao);
        m_service.setStatisticsReportDao(m_statisticsReportDao );
        m_service.afterPropertiesSet();
    }

    @After
    public void verify() throws Throwable {
        m_mocks.verifyAll();
    }
    
    @Test
    public void testNullCommandObjectId() {
        StatisticsReportCommand command = new StatisticsReportCommand();
        BindException errors = new BindException(command, "");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("id property on command object cannot be null"));

        m_mocks.replayAll();
        try {
            m_service.getReport(command , errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testDatumWithNonExistentResource() {
        StatisticsReport report = new StatisticsReport();
        report.setId(1);
        
        StatisticsReportData datum = new StatisticsReportData();
        ResourceReference resourceRef = new ResourceReference();
        resourceRef.setId(1);
        resourceRef.setResourceId("node[1].interfaceSnmp[en0]");
        datum.setId(1);
        datum.setResource(resourceRef);
        datum.setReport(report);
        datum.setValue(0.1d);
        report.addData(datum);

        StatisticsReportCommand command = new StatisticsReportCommand();
        command.setId(report.getId());
        
        BindException errors = new BindException(command, "");
        
        expect(m_statisticsReportDao.load(report.getId())).andReturn(report);
        m_statisticsReportDao.initialize(report);
        m_statisticsReportDao.initialize(report.getData());
        expect(m_resourceDao.getResourceById(resourceRef.getResourceId())).andReturn(null);
        
        m_mocks.replayAll();
        StatisticsReportModel model = m_service.getReport(command, errors);
        
        assertNotNull("model should not be null", model);
        assertNotNull("model.getData() should not be null", model.getData());
        
        SortedSet<Datum> data = model.getData();
        assertEquals("data size", 1, data.size());
        Datum d = data.first();
        assertNotNull("first datum should not be null", d);
        assertNull("first datum resource should be null", d.getResource());
    }
}
