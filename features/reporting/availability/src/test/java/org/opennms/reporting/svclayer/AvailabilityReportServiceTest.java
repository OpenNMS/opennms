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
package org.opennms.reporting.svclayer;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.OnmsReportConfigDao;
import org.opennms.reporting.availability.AvailabilityCalculator;
import org.opennms.reporting.core.svclayer.ParameterConversionService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/org/opennms/reporting/availability/svclayer/AvailabilityReportServiceTest.xml"
})
public class AvailabilityReportServiceTest implements InitializingBean {
    
    @Autowired
    @Qualifier("mockClassicCalculator")
    AvailabilityCalculator m_classicCalculator;
    @Autowired
    @Qualifier("mockCalendarCalculator")
    AvailabilityCalculator m_calendarCalculator;
    @Autowired
    OnmsReportConfigDao m_configDao;
    @Autowired
    ParameterConversionService m_parameterConversionService;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_classicCalculator);
        verifyNoMoreInteractions(m_calendarCalculator);
        verifyNoMoreInteractions(m_configDao);
        verifyNoMoreInteractions(m_parameterConversionService);
    }

    /**
     * TODO: Write a test
     */
    @Test
    public void testMe() {
        // Write some tests
    }

}
