/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
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
    private GroupDao m_groupDao;
    
    @Autowired
    private CategoryDao m_categoryDao;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public boolean groupExists(String groupName) {
        return m_groupDao.hasGroup(groupName);
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public WebGroup getGroup(String groupName) {
        
        Group group = m_groupDao.getGroup(groupName);
        
        WebGroup webGroup = new WebGroup(group, getAuthorizedCategories(groupName));
        
        return webGroup;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
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
    
    /** {@inheritDoc} */
    @Transactional
    @Override
    public void deleteGroup(String groupName) {
        
        m_groupDao.deleteGroup(groupName);
        
        setAuthorizedCategories(groupName, Collections.<String>emptyList());
        
    }
    
    /** {@inheritDoc} */
    @Transactional
    @Override
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
