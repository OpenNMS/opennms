/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.reporting.svclayer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.utils.BeanUtils;
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
        "classpath:org/opennms/reporting/availability/svclayer/AvailabilityReportServiceTest.xml"
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

    /**
     * TODO: Write a test
     */
    @Test
    public void testMe() {
        // Write some tests
    }

}
