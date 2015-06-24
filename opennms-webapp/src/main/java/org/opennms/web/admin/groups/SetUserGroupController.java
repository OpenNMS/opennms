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
