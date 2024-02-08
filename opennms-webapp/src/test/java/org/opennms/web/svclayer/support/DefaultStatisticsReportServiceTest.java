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
package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.SortedSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.StatisticsReportDao;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourceReference;
import org.opennms.netmgt.model.StatisticsReport;
import org.opennms.netmgt.model.StatisticsReportData;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.svclayer.model.StatisticsReportCommand;
import org.opennms.web.svclayer.model.StatisticsReportModel;
import org.opennms.web.svclayer.model.StatisticsReportModel.Datum;
import org.springframework.validation.BindException;

/**
 * Test case for DefaultStatisticsReportService.
 * 
 * @see DefaultStatisticsReportService
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 */
public class DefaultStatisticsReportServiceTest {
    private DefaultStatisticsReportService m_service = new DefaultStatisticsReportService();
    private ResourceDao m_resourceDao = mock(ResourceDao.class);
    private StatisticsReportDao m_statisticsReportDao = mock(StatisticsReportDao.class);

    @Before
    public void setUp() throws Exception {
        m_service.setResourceDao(m_resourceDao);
        m_service.setStatisticsReportDao(m_statisticsReportDao );
        m_service.afterPropertiesSet();
    }

    @After
    public void tearDown() throws Throwable {
        verifyNoMoreInteractions(m_resourceDao);
        verifyNoMoreInteractions(m_statisticsReportDao);
    }
    
    @Test
    public void testNullCommandObjectId() {
        StatisticsReportCommand command = new StatisticsReportCommand();
        BindException errors = new BindException(command, "");
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("id property on command object cannot be null"));

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
        
        when(m_statisticsReportDao.load(report.getId())).thenReturn(report);
        m_statisticsReportDao.initialize(report);
        m_statisticsReportDao.initialize(report.getData());
        when(m_resourceDao.getResourceById(ResourceId.fromString(resourceRef.getResourceId()))).thenReturn(null);
        
        StatisticsReportModel model = m_service.getReport(command, errors);
        
        assertNotNull("model should not be null", model);
        assertNotNull("model.getData() should not be null", model.getData());
        
        SortedSet<Datum> data = model.getData();
        assertEquals("data size", 1, data.size());
        Datum d = data.first();
        assertNotNull("first datum should not be null", d);
        assertNull("first datum resource should be null", d.getResource());

        verify(m_resourceDao, atLeastOnce()).getResourceById(any(ResourceId.class));
        verify(m_statisticsReportDao, atLeastOnce()).initialize(any());
        verify(m_statisticsReportDao, atLeastOnce()).load(anyInt());
    }
}
