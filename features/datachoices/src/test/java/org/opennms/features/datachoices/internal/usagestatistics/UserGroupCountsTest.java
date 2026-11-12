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
package org.opennms.features.datachoices.internal.usagestatistics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.opennms.netmgt.config.api.GroupConfig;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.groups.Role;

/** The counts come from the UserConfig and GroupConfig services, not from Karaf's own copies of the factories. */
public class UserGroupCountsTest {

    @Test
    public void testCountsComeFromTheServices() throws Exception {
        final UserConfig users = mock(UserConfig.class);
        when(users.getUserNames()).thenReturn(Arrays.asList("admin", "rtc", "jane"));
        final GroupConfig groups = mock(GroupConfig.class);
        when(groups.getGroupNames()).thenReturn(Arrays.asList("Admin", "Ops"));
        when(groups.getRoles()).thenReturn(Arrays.asList(new Role()));

        final UsageStatisticsReporter reporter = new UsageStatisticsReporter();
        reporter.setUserConfig(users);
        reporter.setGroupConfig(groups);

        assertEquals(3, reporter.getUserCount());
        assertEquals(2, reporter.getGroupCount());
        assertEquals(1, reporter.getOnCallRoleCount());
    }

    @Test
    public void testAFailingServiceCountsZero() throws Exception {
        final UserConfig users = mock(UserConfig.class);
        when(users.getUserNames()).thenThrow(new IllegalStateException("database down"));
        final UsageStatisticsReporter reporter = new UsageStatisticsReporter();
        reporter.setUserConfig(users);
        assertEquals(0, reporter.getUserCount());
    }
}
