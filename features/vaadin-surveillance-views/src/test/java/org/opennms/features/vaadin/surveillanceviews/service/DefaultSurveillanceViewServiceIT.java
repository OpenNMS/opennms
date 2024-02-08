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
