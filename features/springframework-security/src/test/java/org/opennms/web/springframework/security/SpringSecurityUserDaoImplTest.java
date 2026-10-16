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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;

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
}
