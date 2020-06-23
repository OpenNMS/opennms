/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/emptyContext.xml"
})
@JUnitConfigurationEnvironment
public class DefaultSurveillanceViewServiceIT {
    private AlarmDao m_alarmDao;
    private GroupDao m_groupDao;

    private DefaultSurveillanceViewService m_service;

    @Before
    public void setUp() {
        m_alarmDao = mock(AlarmDao.class, Mockito.RETURNS_DEEP_STUBS);
        m_groupDao = mock(GroupDao.class, Mockito.RETURNS_DEEP_STUBS);

        m_service = new DefaultSurveillanceViewService();
        m_service.setAlarmDao(m_alarmDao);
        m_service.setGroupDao(m_groupDao);
    }

    @Test
    public void testGetDefaultSurveillanceView() {
        final View view = m_service.selectDefaultViewForUsername("admin");
        assertNotNull(view);
        assertEquals("default", view.getName());
    }

}
