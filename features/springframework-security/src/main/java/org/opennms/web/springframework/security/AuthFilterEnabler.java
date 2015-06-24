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

import java.io.IOException;
import java.util.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.FilterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * <p>AuthFilterEnabler class.</p>
 */
public class AuthFilterEnabler implements Filter {
    
    private final static Logger LOG = LoggerFactory.getLogger(AuthFilterEnabler.class);

    private FilterManager m_filterManager;
    
    private GroupDao m_groupDao;
    
    /**
     * <p>setFilterManager</p>
     *
     * @param filterManager a {@link org.opennms.netmgt.model.FilterManager} object.
     */
    public void setFilterManager(FilterManager filterManager) {
        m_filterManager = filterManager;
    }
    
    /**
     * <p>setGroupDao</p>
     *
     * @param groupDao a {@link org.opennms.netmgt.config.GroupDao} object.
     */
    public void setGroupDao(GroupDao groupDao) {
        m_groupDao = groupDao;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.ui.SpringSecurityFilter#doFilterHttp(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
     */
    /** {@inheritDoc} */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
            boolean shouldFilter = AclUtils.shouldFilter(SecurityContextHolder.getContext().getAuthentication().getAuthorities());

        try {
            String[] groupNames;
            if (shouldFilter) {
                String user = SecurityContextHolder.getContext().getAuthentication().getName();
                LOG.debug("Applying ACL filter for user: {}", user);
                List<Group> groups = m_groupDao.findGroupsForUser(user);
                groupNames  = new String[groups.size()];
                for(int i = 0; i < groups.size(); i++) {
                    groupNames[i] = groups.get(i).getName();
                }
                LOG.debug("Found groups for user {}: {}", user, Arrays.toString(groupNames));

                if(SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof OnmsAuthenticationDetails){
                    OnmsAuthenticationDetails details = (OnmsAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();

                    if(details.getUserGroups() != null && details.getUserGroups().length > 0){
                        Set<String> detailsSet = new HashSet<String>(Arrays.asList(details.getUserGroups()));
                        Set<String> groupSet = new HashSet<String>(Arrays.asList(groupNames));

                        if(groupSet.containsAll(detailsSet)){
                            groupNames = details.getUserGroups();
                        }
                    }
                }

                // Log the group names again, since they may have been changed in the statement above
                LOG.debug("Enabling authorization filter for user {} with groups: {}", user, Arrays.toString(groupNames));
                m_filterManager.enableAuthorizationFilter(groupNames);
            }
            chain.doFilter(request, response);

        } finally {
            if (shouldFilter) {
                m_filterManager.disableAuthorizationFilter();
            }

        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

}
