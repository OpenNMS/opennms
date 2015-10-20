/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.api.Authentication;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AuthenticationIntegrationTest implements InitializingBean {

    @Autowired
    private UserManager m_userManager;

    @Autowired
    private AuthenticationProvider m_provider; 

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true, "DEBUG");
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

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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
