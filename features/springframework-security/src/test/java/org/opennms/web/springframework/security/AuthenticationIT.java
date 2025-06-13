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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.api.Authentication;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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
public class AuthenticationIT implements InitializingBean {

    @Autowired
    private UserManager m_userManager;

    @Autowired
    private AuthenticationProvider m_provider;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        Userinfo users = JaxbUtils.unmarshal(Userinfo.class, new FileSystemResource(new File("src/test/resources/org/opennms/web/springframework/security/users.xml")));
        assertNotNull(users);
        m_userManager.saveUsers(users.getUsers());
    }

    @Test
    public void testAuthenticateAdmin() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 2, authorities.size());
        assertContainsAuthority(Authentication.ROLE_ADMIN, authorities);
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }


    @Test
    public void testAuthenticateRtc() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("rtc", "rtc");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_RTC, authorities);
    }

    @Test
    public void testAuthenticateTempUser() throws Exception {
        OnmsUser user = new OnmsUser("tempuser");
        user.setFullName("Temporary User");
        user.setPassword("18126E7BD3F84B3F3E4DF094DEF5B7DE");
        user.setDutySchedule(Arrays.asList("MoTuWeThFrSaSu800-2300"));
        m_userManager.save(user);

        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("tempuser", "mike");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }

    @Test
    public void testAuthenticateBadUsername() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("badUsername", "admin");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new BadCredentialsException("Bad credentials"));
        try {
            m_provider.authenticate(authentication);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAuthenticateBadPassword() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "badPassword");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new BadCredentialsException("Bad credentials"));
        try {
            m_provider.authenticate(authentication);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testAdditionalRoles() {
        System.setProperty("opennms.home", "src/test/resources");
        List<String> roles = Authentication.getAvailableRoles();
        Assert.assertTrue(roles.contains("ROLE_MANAGER"));
        Assert.assertTrue(roles.contains("ROLE_OPERATOR"));
        Assert.assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    public void testAuthenticateWithoutAsterisk16() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("no-asterisk-16", "AwLwLaTK3rxFezg3");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }

    @Test
    public void testAuthenticateWithoutAsterisk24() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("no-asterisk-24", "1nIzDqoRw39OSqrvSFv1cImC");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }

    @Test
    public void testAuthenticateWithoutAsterisk32() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("no-asterisk-32", "KfXH1bHM6KocQpKNYtoIDYSDl5avIACb");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }

    @Test
    public void testAuthenticateWithAsterisk16() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("with-asterisk-16", "wm=5&#0;*ME%[;y%");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }

    @Test
    public void testAuthenticateWithAsterisk24() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("with-asterisk-24", "z[+O`q@*I77=%&b,FEICJ,P&");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }

    @Test
    public void testAuthenticateWithAsterisk32() {
        org.springframework.security.core.Authentication authentication = new UsernamePasswordAuthenticationToken("with-asterisk-32", ",X-.yT`'J>=7l$=z85*91Dx_ujzHR\\Q;");
        org.springframework.security.core.Authentication authenticated = m_provider.authenticate(authentication);
        assertNotNull("authenticated Authentication object not null", authenticated);
        Collection<? extends GrantedAuthority> authorities = authenticated.getAuthorities();
        assertNotNull("GrantedAuthorities should not be null", authorities);
        assertEquals("GrantedAuthorities size", 1, authorities.size());
        assertContainsAuthority(Authentication.ROLE_USER, authorities);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

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
