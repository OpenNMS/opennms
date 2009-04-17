//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/

package org.opennms.web.springframework.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.ui.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.Assert;

/**
 * @author Timothy Nowaczyk, tan7f@virginia.edu
 */
public class RequestAttributePreAuthenticationProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
    
    private String m_principalRequestAttribute = "REMOTE_USER"; 
    private String m_credentialsRequestAttribute;


    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getHeader(m_principalRequestAttribute);
        
        if (principal == null) {
            throw new PreAuthenticatedCredentialsNotFoundException(m_principalRequestAttribute 
                    + " attribute not found in request.");
        }

        return principal;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (m_credentialsRequestAttribute != null) {
            String credentials = request.getHeader(m_credentialsRequestAttribute);
            
            return credentials;
        }

        return "";

    }
    
    public void setPrincipalRequestHeader(String principalRequestAttribute) {
        Assert.hasText(principalRequestAttribute, "principalRequestAttribute must not be empty or null");
        m_principalRequestAttribute = principalRequestAttribute;
    }

    public void setCredentialsRequestHeader(String credentialsRequestAttribute) {
        Assert.hasText(credentialsRequestAttribute, "credentialsRequestAttribute must not be empty or null");     
        m_credentialsRequestAttribute = credentialsRequestAttribute;
    }

    public int getOrder() {
        return FilterChainOrder.PRE_AUTH_FILTER;
    }

}
