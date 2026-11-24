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
package org.opennms.web.springframework.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;

public class SpringSecurityUserDaoImplTest {

    @After
    public void tearDown() {
        UserFactory.setInstance(null);
    }

    @Test
    public void testOldUsersConfigurationFilePropertyIsIgnored() {
        // a customized applicationContext-spring-security.xml from an older release (a kept conffile) still sets
        // the property: the webapp must start, and the manager in UserFactory must stay
        final UserManager manager = mock(UserManager.class);
        UserFactory.setInstance(manager);
        new SpringSecurityUserDaoImpl().setUsersConfigurationFile("/opt/opennms/etc/users.xml");
        assertSame(manager, UserFactory.getInstance());
    }

    @Test
    public void testAReloadDuringParsingIsNotMissed() throws Exception {
        // another thread reloads the user manager while this one reads the list: the cache must keep the version
        // of the data it holds, or it never parses again (old passwords keep working, new users cannot log in)
        final AtomicLong version = new AtomicLong(5);
        final AtomicInteger parses = new AtomicInteger();
        final UserManager manager = mock(UserManager.class);
        when(manager.getLastModified()).thenAnswer(invocation -> version.get());
        when(manager.getOnmsUserList()).thenAnswer(invocation -> {
            final OnmsUserList list = new OnmsUserList();
            if (parses.incrementAndGet() == 1) {
                list.add(new OnmsUser("old"));
                version.set(6); // the concurrent reload, after this thread read version 5's data
            } else {
                list.add(new OnmsUser("old"));
                list.add(new OnmsUser("new"));
            }
            return list;
        });
        final SpringSecurityUserDaoImpl dao = new SpringSecurityUserDaoImpl();
        dao.setUserManager(manager);

        assertNotNull(dao.getByUsername("old"));
        assertNotNull(dao.getByUsername("new"));
    }
}
