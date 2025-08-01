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
