/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * <p>AuthRoleToOnmsGroupMapFilterEnabler class.</p>
 *
 * @author rssntn67
 * @version $Id: $
 */
public class AuthRoleToOnmsGroupMapFilterEnabler implements Filter {
    
	private static final Logger LOG = LoggerFactory.getLogger(AuthRoleToOnmsGroupMapFilterEnabler.class);

    private FilterManager m_filterManager;
    
    private Map<String, List<String>> roleToOnmsGroupMap = new HashMap<String, List<String>>();
    
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
            	LOG.debug("found role: {}, associated with user group: {}", role, roleToOnmsGroupMap.get(role));
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
    	for (GrantedAuthority authority: SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
    		LOG.debug("checking role: {}, with granted authority: {}", role,authority.getAuthority());
    		if (authority.getAuthority().equals(role))
    			return true;
    	}
    	return false;
    }
    
    
	@Override
	public void destroy() {
	}


	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
