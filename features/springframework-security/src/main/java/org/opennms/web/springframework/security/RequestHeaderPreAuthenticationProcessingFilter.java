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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

/**
 * <p>RequestAttributePreAuthenticationProcessingFilter class. This filter should be used
 * before the FORM_LOGIN_FILTER position in the filter chain.</p>
 * <p>If enabled, attempt to pre-authenticate as the user specified in the provided header.</p>
 * <p>Note that this can be easily spoofed if you expose the original OpenNMS instance
 * rather than only allowing this through a proxy!  Be sure your OpenNMS is proxied and
 * that the proxy is performing authentication and ALWAYS setting this header.</p>
 */
public class RequestHeaderPreAuthenticationProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestHeaderPreAuthenticationProcessingFilter.class);

    private boolean m_enabled = false;
    private String m_userHeader = null;
    private String m_credentialsHeader = null;
    private String m_authoritiesHeader = null;
    private boolean m_failOnError = false;

    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (m_enabled) {
            if (StringUtils.isBlank(m_userHeader)) {
                throw new IllegalStateException("RequestHeaderPreAuthenticationProcessingFilter is enabled but 'userHeader' is not set!");
            }
            if (StringUtils.isBlank(m_credentialsHeader)) {
                m_credentialsHeader = null;
            }
            LOG.debug("Request header pre-authentication filter is enabled.  Access will be pre-authenticated by the user (principal) in the '{}' header on each servlet request.", m_userHeader);
        } else {
            LOG.info("Request header pre-authentication filter is disabled.");
        }
        setAuthenticationDetailsSource(new AuthenticationDetailsSource());
    }

    private class AuthenticationDetailsSource extends WebAuthenticationDetailsSource  {
        @Override
        public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
            return new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(context, getGrantedAuthorities(context));
        }
    }

    protected Collection<? extends GrantedAuthority> getGrantedAuthorities(HttpServletRequest context) {
        String roles = "";
        if (m_authoritiesHeader != null) {
            roles = context.getHeader(m_authoritiesHeader);
        }
        if (StringUtils.isBlank(roles)) {
            return Collections.emptyList();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        if (m_enabled) {
            final Object user = request.getHeader(m_userHeader);
            if (user == null && m_failOnError) {
                throw new PreAuthenticatedCredentialsNotFoundException(m_userHeader + " header not found in request.");
            }
            return user;
        }
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
        if (m_credentialsHeader != null) {
            return request.getHeader(m_credentialsHeader);
        }
        return "";

    }

    /**
     * Whether or not to enable this pre-auth filter.
     * @param enabled
     */
    public void setEnabled(final boolean enabled) {
        m_enabled = enabled;
    }

    /**
     * The header (eg, X-Remote-User) to extract the authenticated user from.
     * @param userHeader
     */
    public void setUserHeader(final String userHeader) {
        m_userHeader = userHeader;
    }

    /**
     * The header to extract credentials from.
     * @param credentialsHeader
     */
    public void setCredentialsHeader(final String credentialsHeader) {
        m_credentialsHeader = credentialsHeader;
    }

    public String getAuthoritiesHeader() {
        return m_authoritiesHeader;
    }

    public void setAuthoritiesHeader(String authoritiesHeader) {
        m_authoritiesHeader = authoritiesHeader;
    }

    /**
     * Whether to fail if the user is not found, or to fall through to other authentication mechanisms.
     */
    public void setFailOnError(final boolean failOnError) {
        m_failOnError = failOnError;
    }
}
