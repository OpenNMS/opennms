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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * DefaultWebGroupRepository
 *
 * @author brozow
 */
public class DefaultWebGroupRepository implements WebGroupRepository {
    
    @Autowired
    private GroupDao m_groupDao;
    
    @Autowired
    private CategoryDao m_categoryDao;
    
    
    
    @Transactional
    public boolean groupExists(String groupName) {
        return m_groupDao.hasGroup(groupName);
    }

    @Transactional
    public WebGroup getGroup(String groupName) {
        
        Group group = m_groupDao.getGroup(groupName);
        
        WebGroup webGroup = new WebGroup(group, getAuthorizedCategories(groupName));
        
        return webGroup;
    }

    @Transactional
    public void saveGroup(WebGroup webGroup) {

        Group group = m_groupDao.getGroup(webGroup.getName());
        
        if (group == null) {
            group = new Group();
            group.setName(webGroup.getName());
        }
        
        group.setComments(webGroup.getComments());
        group.setDutySchedule(webGroup.getDutySchedules());
        group.setUser(webGroup.getUsers());
        if (!webGroup.getDefaultMap().equals(""))
            group.setDefaultMap(webGroup.getDefaultMap());
        
        
        setAuthorizedCategories(webGroup.getName(), webGroup.getAuthorizedCategories());
        
        m_groupDao.saveGroup(group.getName(), group);
        
        
    }
    
    @Transactional
    public void deleteGroup(String groupName) {
        
        m_groupDao.deleteGroup(groupName);
        
        setAuthorizedCategories(groupName, Collections.<String>emptyList());
        
    }
    
    @Transactional
    public void renameGroup(String oldName, String newName) {
        
        m_groupDao.renameGroup(oldName, newName);
        
        List<String> categories = getAuthorizedCategories(oldName);
        
        setAuthorizedCategories(oldName, Collections.<String>emptyList());
        
        setAuthorizedCategories(newName, categories);
    }
    
    private List<String> getAuthorizedCategories(String groupName) {
        List<OnmsCategory> categories = m_categoryDao.getCategoriesWithAuthorizedGroup(groupName);
        
        List<String> categoryNames = new ArrayList<String>(categories.size());
        
        for(OnmsCategory category : categories) {
            categoryNames.add(category.getName());
        }
        
        return categoryNames;
    }
    
    private void setAuthorizedCategories(String groupName, List<String> categoryNames) {
        
        List<OnmsCategory> categories = m_categoryDao.getCategoriesWithAuthorizedGroup(groupName);
        
        for(OnmsCategory category : categories) {
            category.getAuthorizedGroups().remove(groupName);
        }
        
        
        for(String categoryName : categoryNames) {
            OnmsCategory category = m_categoryDao.findByName(categoryName, false);
            if (category != null) {
                category.getAuthorizedGroups().add(groupName);
            }
        }
        
    }
     

}
