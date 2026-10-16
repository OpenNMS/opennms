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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Test;

public class UserMgmtFactoryRegistrationTest {

    @After
    public void tearDown() {
        UserFactory.setInstance(null);
        GroupFactory.setInstance(null);
    }

    @Test
    public void testInstallsAndClearsTheManagers() throws Exception {
        final UserManager users = mock(UserManager.class);
        final GroupManager groups = mock(GroupManager.class);
        final UserMgmtFactoryRegistration registration = new UserMgmtFactoryRegistration(users, groups);
        registration.afterPropertiesSet();
        assertSame(users, UserFactory.getInstance());
        assertSame(groups, GroupFactory.getInstance());

        // when the context closes, a later context in the same JVM must not get managers bound to a closed SessionFactory
        registration.destroy();
        assertNull(UserFactory.getInstance());
        assertNull(GroupFactory.getInstance());
    }

    @Test
    public void testDoesNotClearAnotherContextsManagers() throws Exception {
        final UserMgmtFactoryRegistration registration = new UserMgmtFactoryRegistration(mock(UserManager.class), mock(GroupManager.class));
        registration.afterPropertiesSet();
        final UserManager laterUsers = mock(UserManager.class);
        final GroupManager laterGroups = mock(GroupManager.class);
        UserFactory.setInstance(laterUsers);
        GroupFactory.setInstance(laterGroups);

        registration.destroy();
        assertSame(laterUsers, UserFactory.getInstance());
        assertSame(laterGroups, GroupFactory.getInstance());
    }
}
