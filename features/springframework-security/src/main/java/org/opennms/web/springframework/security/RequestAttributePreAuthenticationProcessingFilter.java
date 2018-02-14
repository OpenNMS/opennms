/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
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
 * PRE_AUTH_FILTER position in the filter chain.</p>
 * 
 * @see http://static.springsource.org/spring-security/site/docs/3.1.x/reference/springsecurity-single.html
 * @author Timothy Nowaczyk, tan7f@virginia.edu
 */
public class RequestAttributePreAuthenticationProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestAttributePreAuthenticationProcessingFilter.class);

    private boolean m_enabled = false;
    private String m_principalRequestAttribute = null;
    private String m_credentialsRequestAttribute = null;
    private boolean m_failOnError = false;

    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (m_enabled) {
            if (StringUtils.isBlank(m_principalRequestAttribute)) {
                throw new IllegalStateException("RequestAttributePreAuthenticationProcessingFilter is enabled but 'principalRequestHeader' is not set!");
            }
            if (StringUtils.isBlank(m_credentialsRequestAttribute)) {
                m_credentialsRequestAttribute = null;
            }
            LOG.debug("Request attribute pre-authentication filter is enabled.  Access will be pre-authenticated by the user (principal) in the '{}' attribute on each servlet request.", m_principalRequestAttribute);
        } else {
            LOG.info("Request attribute pre-authentication filter is disabled.");
        }
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        if (m_enabled) {
            final Object user = request.getAttribute(m_principalRequestAttribute);
            if (user == null && m_failOnError) {
                throw new PreAuthenticatedCredentialsNotFoundException(m_principalRequestAttribute + " attribute not found in request.");
            }
            return user;
        }
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
        if (m_credentialsRequestAttribute != null) {
            return request.getAttribute(m_credentialsRequestAttribute);
        }
        return "";

    }

    /**
     * Whether or not to enable this pre-auth filter.
     */
    public void setEnabled(final boolean enabled) {
        m_enabled = enabled;
    }

    /**
     * The {@link ServletRequest#getAttribute attribute} to extract the authenticated user from.
     */
    public void setPrincipalRequestHeader(final String principleRequestAttribute) {
        m_principalRequestAttribute = principleRequestAttribute;
    }

    /**
     * The {@link ServletRequest#getAttribute attribute} to extract the user's credentials from.
     */
    public void setCredentialsRequestHeader(final String credentialsRequestAttribute) {
        m_credentialsRequestAttribute = credentialsRequestAttribute;
    }

    /**
     * Whether to fail if the user is not found, or to fall through to other authentication mechanisms.
     */
    public void setFailOnError(final boolean failOnError) {
        m_failOnError = failOnError;
    }
}
