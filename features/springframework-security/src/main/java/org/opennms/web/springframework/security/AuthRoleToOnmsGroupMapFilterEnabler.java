/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.web.springframework.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.opennms.netmgt.model.FilterManager;

import org.opennms.web.api.SecurityContextService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * <p>AuthRoleToOnmsGroupMapFilterEnabler class.</p>
 *
 * @author rssntn67
 * @version $Id: $
 */
public class AuthRoleToOnmsGroupMapFilterEnabler implements Filter {
    
    private FilterManager m_filterManager;
    
    private Map<String, List<String>> roleToOnmsGroupMap = new HashMap<String, List<String>>();
    
    private SecurityContextService m_contextService;

    public SecurityContextService getContextService() {
		return m_contextService;
	}

    @Autowired
	public void setContextService(SecurityContextService contextService) {
		m_contextService = contextService;
	}
   
    public void setRoleToOnmsGroupMap(Map<String, List<String>> roleToOnmsGroupMap) {
        this.roleToOnmsGroupMap = roleToOnmsGroupMap;
    }


    /**
     * <p>setFilterManager</p>
     *
     * @param filterManager a {@link org.opennms.netmgt.model.FilterManager} object.
     */
    public void setFilterManager(FilterManager filterManager) {
        m_filterManager = filterManager;
    }
    

    /* (non-Javadoc)
     * @see org.springframework.security.ui.SpringSecurityFilter#doFilterHttp(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
     */
    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        boolean shouldFilter = AclUtils.shouldFilter(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        List<String> groups = new ArrayList<String>(); 

        if (shouldFilter) {
    
            for (String role: roleToOnmsGroupMap.keySet()) {            
               if (userHasAuthority(role))
                   groups.addAll(roleToOnmsGroupMap.get(role));
            }
            if (groups.isEmpty())
                shouldFilter=false;
        }

        try {
            if (shouldFilter) {
                
                String[] groupNames = new String[groups.size()];
                for(int i = 0; i < groups.size(); i++) {
                    groupNames[i] = groups.get(i);
                }

                m_filterManager.enableAuthorizationFilter(groupNames);
            }

            chain.doFilter(request, response);
        } finally {
            if (shouldFilter) {
                m_filterManager.disableAuthorizationFilter();
            }
        }

        
    }

    private boolean userHasAuthority(String role) {
    	return getContextService().hasRole(role);
    }
    
    
	@Override
	public void destroy() {
	}


	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
