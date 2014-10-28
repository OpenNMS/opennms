/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.Assert;

/**
 * <p>RequestAttributePreAuthenticationProcessingFilter class. This filter should be used
 * PRE_AUTH_FILTER position in the filter chain.</p>
 * 
 * @see http://static.springsource.org/spring-security/site/docs/3.1.x/reference/springsecurity-single.html
 * @author Timothy Nowaczyk, tan7f@virginia.edu
 */
public class RequestAttributePreAuthenticationProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
    
    private String m_principalRequestAttribute = "REMOTE_USER"; 
    private String m_credentialsRequestAttribute;


    /** {@inheritDoc} */
    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        Object principal = request.getAttribute(m_principalRequestAttribute);
        
        if (principal == null) {
            throw new PreAuthenticatedCredentialsNotFoundException(m_principalRequestAttribute 
                    + " attribute not found in request.");
        }

        return principal;
    }

    /** {@inheritDoc} */
    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (m_credentialsRequestAttribute != null) {
            Object credentials = request.getAttribute(m_credentialsRequestAttribute);
            
            return credentials;
        }

        return "";

    }
    
    /**
     * <p>setPrincipalRequestHeader</p>
     *
     * @param principalRequestAttribute a {@link java.lang.String} object.
     */
    public void setPrincipalRequestHeader(String principalRequestAttribute) {
        Assert.hasText(principalRequestAttribute, "principalRequestAttribute must not be empty or null");
        m_principalRequestAttribute = principalRequestAttribute;
    }

    /**
     * <p>setCredentialsRequestHeader</p>
     *
     * @param credentialsRequestAttribute a {@link java.lang.String} object.
     */
    public void setCredentialsRequestHeader(String credentialsRequestAttribute) {
        Assert.hasText(credentialsRequestAttribute, "credentialsRequestAttribute must not be empty or null");     
        m_credentialsRequestAttribute = credentialsRequestAttribute;
    }
}
