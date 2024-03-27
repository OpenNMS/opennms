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
