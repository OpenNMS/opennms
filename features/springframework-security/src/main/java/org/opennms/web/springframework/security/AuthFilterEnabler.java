/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.web.springframework.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.web.AclUtils;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;

/**
 * <p>AuthFilterEnabler class.</p>
 */
public class AuthFilterEnabler extends SpringSecurityFilter {
    
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
    protected void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        boolean shouldFilter = AclUtils.shouldFilter();

        try {
            if (shouldFilter) {
                String user = SecurityContextHolder.getContext().getAuthentication().getName();


                List<Group> groups = m_groupDao.findGroupsForUser(user);

                String[] groupNames = new String[groups.size()];
                for(int i = 0; i < groups.size(); i++) {
                    groupNames[i] = groups.get(i).getName();
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

    /* (non-Javadoc)
     * @see org.springframework.core.Ordered#getOrder()
     */
    /**
     * <p>getOrder</p>
     *
     * @return a int.
     */
    public int getOrder() {
        return FilterChainOrder.getOrder("LAST");
    }

}
