/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

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

    /**
     * Whether to fail if the user is not found, or to fall through to other authentication mechanisms.
     */
    public void setFailOnError(final boolean failOnError) {
        m_failOnError = failOnError;
    }
}
