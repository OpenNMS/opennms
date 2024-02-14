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
