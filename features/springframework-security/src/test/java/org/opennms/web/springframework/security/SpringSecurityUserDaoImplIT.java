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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.test.MockLogAppender.assertNoWarningsOrGreater;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.api.Authentication;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
        "classpath:/org/opennms/web/springframework/security/AuthenticationIntegrationTest-context.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SpringSecurityUserDaoImplIT implements InitializingBean {

    @Autowired
    SpringSecurityUserDao m_springSecurityDao;

    @Autowired
    UserManager m_userManager;

    @Autowired
    GroupManager m_groupManager;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        Userinfo users = JaxbUtils.unmarshal(Userinfo.class, new FileSystemResource(new File("src/test/resources/org/opennms/web/springframework/security/users.xml")));
        assertNotNull(users);
        m_userManager.saveUsers(users.getUsers());
    }

    @Test
    public void testGetByUsernameAdmin() {
        SpringSecurityUser user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("admin");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "admin", user.getUsername());
        assertEquals("Full name", "Administrator", user.getFullName());
        assertEquals("Comments", "Default administrator, do not delete", user.getComments());
        assertEquals("Password", "gU2wmSW7k9v1xg4/MrAsaI+VyddBAhJJt4zPX5SGG0BK+qiASGnJsqM8JOug/aEL", user.getPassword());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 2, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
        assertContainsAuthority(Authentication.ROLE_ADMIN, authorities);
        assertNoWarningsOrGreater();
    }

    @Test
    public void testGetByUsernameBogus() {
        assertNull("user object should be null", m_springSecurityDao.getByUsername("bogus"));
        assertNoWarningsOrGreater();
    }

    @Test
    public void testGetByUsernameRtc() {
        SpringSecurityUser user = m_springSecurityDao.getByUsername("rtc");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "rtc", user.getUsername());
        assertEquals("Full name", "RTC", user.getFullName());
        assertEquals("Comments", "", user.getComments());
        assertTrue("Password", m_userManager.checkSaltedPassword("rtc", user.getPassword()));

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_RTC, authorities);
        assertNoWarningsOrGreater();
    }

    @Test
    @DirtiesContext
    public void testGetByUsernameTempUser() throws Exception {
        final OnmsUser newUser = new OnmsUser("tempuser");
        newUser.setPassword("18126E7BD3F84B3F3E4DF094DEF5B7DE");
        m_userManager.save(newUser);

        final SpringSecurityUser user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("tempuser");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "tempuser", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "18126E7BD3F84B3F3E4DF094DEF5B7DE", user.getPassword());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
        assertNoWarningsOrGreater();
    }

    @Test
    @DirtiesContext
    public void testGetByUsernameDashboard() throws Exception {
        final OnmsUser newUser = new OnmsUser("dashboard");
        newUser.setPassword("DC7161BE3DBF2250C8954E560CC35060");
        newUser.getRoles().add(Authentication.ROLE_DASHBOARD);
        m_userManager.save(newUser);

        SpringSecurityUser user = ((SpringSecurityUserDao) m_springSecurityDao).getByUsername("dashboard");
        assertNotNull("user object should not be null", user);
        assertEquals("OnmsUser name", "dashboard", user.getUsername());
        assertEquals("Full name", null, user.getFullName());
        assertEquals("Comments", null, user.getComments());
        assertEquals("Password", "DC7161BE3DBF2250C8954E560CC35060", user.getPassword());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull("authorities should not be null", authorities);
        assertEquals("authorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_DASHBOARD, authorities);
        assertNoWarningsOrGreater();
    }

    private void assertContainsAuthority(final String role, final Collection<? extends GrantedAuthority> authorities) {
        for (final GrantedAuthority authority : authorities) {
            if (role.equals(authority.getAuthority())) {
                return;
            }
        }

        throw new AssertionError("role " + role + " was not found in " + authorities);
    }
}
