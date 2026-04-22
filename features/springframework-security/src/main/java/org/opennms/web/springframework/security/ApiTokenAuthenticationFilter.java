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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.features.apitokens.ApiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter that authenticates requests bearing an API token
 * in the Authorization header (Bearer scheme). If the token is valid,
 * sets the SecurityContext; otherwise passes through to the next filter.
 */
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ApiTokenAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_PREFIX = "onms_";

    private ApiTokenService apiTokenService;
    private SpringSecurityUserDao userDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length()).trim();
                if (token.startsWith(TOKEN_PREFIX)) {
                    if (apiTokenService == null) {
                        LOG.debug("API token service not injected, skipping token auth");
                    } else {
                        try {
                            String username = apiTokenService.authenticate(token);
                            if (username != null) {
                                SpringSecurityUser user = userDao.getByUsername(username);
                                if (user != null) {
                                    if (user.getAuthorities().isEmpty()) {
                                        user.addAuthority(SpringSecurityUserDao.ROLE_USER);
                                    }
                                    UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                                    SecurityContextHolder.getContext().setAuthentication(auth);
                                    LOG.debug("API token authentication successful for user: {}", username);
                                }
                            }
                        } catch (Exception e) {
                            // Service unavailable (OSGi proxy, network, etc.) — fall through to basic auth
                            LOG.debug("API token authentication failed, skipping token auth", e);
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    public void setApiTokenService(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    public void setUserDao(SpringSecurityUserDao userDao) {
        this.userDao = userDao;
    }
}
