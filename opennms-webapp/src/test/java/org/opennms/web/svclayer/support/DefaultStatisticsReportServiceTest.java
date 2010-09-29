/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Oct 22: Use new ResourceDao method names. - dj@opennms.org
 * 2007 Sep 09: Created this file. - dj@opennms.org
 * 
 * Copyright (C) 2007 Daniel J. Gregor, Jr.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.expect;

import java.util.SortedSet;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.StatisticsReportDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceReference;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.web.command.StatisticsReportCommand;
import org.opennms.web.svclayer.support.StatisticsReportModel.Datum;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;

/**
 * Test case for DefaultStatisticsReportService.
 * 
 * @see DefaultStatisticsReportService
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 */
public class DefaultStatisticsReportServiceTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    private DefaultStatisticsReportService m_service = new DefaultStatisticsReportService();
    private ResourceDao m_resourceDao = m_mocks.createMock(ResourceDao.class);
    private StatisticsReportDao m_statisticsReportDao = m_mocks.createMock(StatisticsReportDao.class);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_service.setResourceDao(m_resourceDao);
        m_service.setStatisticsReportDao(m_statisticsReportDao );
        m_service.afterPropertiesSet();
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        m_mocks.verifyAll();
    }
    
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
        report.addData(datum);

        StatisticsReportCommand command = new StatisticsReportCommand();
        command.setId(report.getId());
        BindException errors = new BindException(command, "");
        
        expect(m_statisticsReportDao.load(report.getId())).andReturn(report);
        m_statisticsReportDao.initialize(report);
        m_statisticsReportDao.initialize(report.getData());
        expect(m_resourceDao.getResourceById(resourceRef.getResourceId())).andThrow(new ObjectRetrievalFailureException(OnmsResource.class, "interfaceSnmp/en0", "Could not find child resource 'en0' with resource type 'interfaceSnmp' on resource 'en0'", null));
        
        m_mocks.replayAll();
        StatisticsReportModel model = m_service.getReport(command, errors);
        
        assertNotNull("model should not be null", model);
        assertNotNull("model.getData() should not be null", model.getData());
        
        SortedSet<Datum> data = model.getData();
        assertEquals("data size", 1, data.size());
        Datum d = data.first();
        assertNotNull("first datum should not be null", d);
        assertNull("first datum resource should be null", d.getResource());
        assertNotNull("first datum resourceThrowable should not be null", d.getResourceThrowable());
    }
}
