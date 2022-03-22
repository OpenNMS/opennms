/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class RequestHeaderPreAuthenticationProcessingFilterTest {

    @Test
    public void canGetGrantedAuthoritiesFromHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestHeaderPreAuthenticationProcessingFilter filter = new RequestHeaderPreAuthenticationProcessingFilter();
        filter.setAuthoritiesHeader("x-auth");
        when(request.getHeader(filter.getAuthoritiesHeader())).thenReturn(" user, admin,");
        Collection<? extends GrantedAuthority> authorities = filter.getGrantedAuthorities(request);
        assertThat(authorities, contains(new SimpleGrantedAuthority("user"), new SimpleGrantedAuthority("admin")));
    }
}
