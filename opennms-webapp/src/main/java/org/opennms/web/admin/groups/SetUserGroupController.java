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
package org.opennms.web.admin.groups;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.web.springframework.security.AclUtils;
import org.opennms.web.springframework.security.OnmsAuthenticationDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class SetUserGroupController extends AbstractController {

    private FilterManager m_filterManager;
    private GroupDao m_groupDao;


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        boolean shouldFilter = AclUtils.shouldFilter(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        if(httpServletRequest.getMethod().equals(METHOD_POST) && shouldFilter){

            String userGroups = httpServletRequest.getParameter("j_usergroups");
            String[] userGroupList;
            if(userGroups != null){
                String[] split = userGroups.split(",");
                userGroupList = split[0].equals("") ? null : split;
            } else {
                userGroupList = null;
            }

            OnmsAuthenticationDetails details = (OnmsAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
            details.setUserGroups(userGroupList);


            String[] groupNames;
            String user = SecurityContextHolder.getContext().getAuthentication().getName();

            List<Group> groups = m_groupDao.findGroupsForUser(user);
            groupNames  = new String[groups.size()];
            for(int i = 0; i < groups.size(); i++) {
                groupNames[i] = groups.get(i).getName();
            }

            if(details.getUserGroups() != null && details.getUserGroups().length > 0){
                Set<String> detailsSet = new HashSet<String>(Arrays.asList(details.getUserGroups()));
                Set<String> groupSet = new HashSet<String>(Arrays.asList(groupNames));

                if(groupSet.containsAll(detailsSet)){
                    groupNames = details.getUserGroups();
                }
            }

            m_filterManager.enableAuthorizationFilter(groupNames);
        }

        return null;
    }

    public void setFilterManager(FilterManager m_filterManager) {
        this.m_filterManager = m_filterManager;
    }

    public void setGroupDao(GroupDao m_groupDao) {
        this.m_groupDao = m_groupDao;
    }
}
