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

package org.opennms.web.group;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.web.svclayer.api.GroupService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * DefaultWebGroupRepository
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultWebGroupRepository implements WebGroupRepository, InitializingBean {
    
    @Autowired
    GroupService groupService;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public boolean groupExists(String groupName) {
        return groupService.existsGroup(groupName);
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public WebGroup getGroup(String groupName) {
        Group group = groupService.getGroup(groupName);
        return new WebGroup(group, groupService.getAuthorizedCategoriesAsString(groupName));
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public void saveGroup(WebGroup webGroup) {
        Group group = groupService.getGroup(webGroup.getName());
        if (group == null) {
            group = new Group();
            group.setName(webGroup.getName());
        }
        group.setComments(webGroup.getComments());
        group.setDutySchedule(webGroup.getDutySchedules());
        group.setUser(webGroup.getUsers());
        if (!webGroup.getDefaultMap().equals(""))
            group.setDefaultMap(webGroup.getDefaultMap());

        groupService.saveGroup(group, webGroup.getAuthorizedCategories());
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void deleteGroup(String groupName) {
        groupService.deleteGroup(groupName);
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void renameGroup(String oldName, String newName) {
        groupService.renameGroup(oldName, newName);
    }

}
