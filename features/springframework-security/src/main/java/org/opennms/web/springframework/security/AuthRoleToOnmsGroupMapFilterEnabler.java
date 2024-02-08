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
