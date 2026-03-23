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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.apitokens.ApiTokenService;
import org.opennms.netmgt.model.OnmsUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

public class ApiTokenAuthenticationFilterTest {
    private ApiTokenAuthenticationFilter filter;
    private ApiTokenService mockService;
    private SpringSecurityUserDao mockUserDao;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockChain;

    @Before
    public void setUp() {
        mockService = mock(ApiTokenService.class);
        mockUserDao = mock(SpringSecurityUserDao.class);
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();

        filter = new ApiTokenAuthenticationFilter();
        filter.setApiTokenService(mockService);
        filter.setUserDao(mockUserDao);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testValidBearerTokenSetsAuthentication() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer onms_abc123");
        when(mockService.authenticate("onms_abc123")).thenReturn("admin");

        OnmsUser onmsUser = new OnmsUser();
        onmsUser.setUsername("admin");
        SpringSecurityUser user = new SpringSecurityUser(onmsUser);
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(mockUserDao.getByUsername("admin")).thenReturn(user);

        filter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", ((SpringSecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testInvalidTokenPassesThrough() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer onms_invalid");
        when(mockService.authenticate("onms_invalid")).thenReturn(null);

        filter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testNoBearerHeaderPassesThrough() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testBasicAuthHeaderIgnored() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(mockChain).doFilter(mockRequest, mockResponse);
        verifyNoInteractions(mockService);
    }

    @Test
    public void testServiceUnavailablePassesThrough() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer onms_abc123");
        when(mockService.authenticate("onms_abc123")).thenThrow(new IllegalStateException("Service unavailable"));

        filter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testOsgiServiceExceptionPassesThrough() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer onms_abc123");
        when(mockService.authenticate("onms_abc123")).thenThrow(new RuntimeException("OSGi ServiceException"));

        filter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testNullServicePassesThrough() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer onms_abc123");

        ApiTokenAuthenticationFilter nullServiceFilter = new ApiTokenAuthenticationFilter();
        // apiTokenService is null — not set
        nullServiceFilter.setUserDao(mockUserDao);

        nullServiceFilter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testDeletedUserPassesThrough() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer onms_abc123");
        when(mockService.authenticate("onms_abc123")).thenReturn("deleteduser");
        when(mockUserDao.getByUsername("deleteduser")).thenReturn(null);

        filter.doFilterInternal(mockRequest, mockResponse, mockChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(mockChain).doFilter(mockRequest, mockResponse);
    }
}
