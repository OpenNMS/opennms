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

package org.opennms.web.svclayer.support;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;
import org.opennms.web.svclayer.api.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultGroupService implements InitializingBean, GroupService {

    private final Logger Log = LoggerFactory.getLogger(getClass());
    
    private final GroupManager.OnmsGroupListMapper onmsGroupListMapper = new GroupManager.OnmsGroupListMapper();

    private final GroupManager.OnmsGroupMapper onmsGroupMapper = new GroupManager.OnmsGroupMapper();

    @Autowired
    private GroupDao m_groupDao;

    @Autowired
    private CategoryDao m_categoryDao;
    
    @Autowired
    private UserManager m_userDao;

    public DefaultGroupService() {
        System.out.println("init...");
    }

    @Override
    public boolean existsGroup(String groupName) {
        return m_groupDao.hasGroup(groupName);
    }

    @Override
    public Group getGroup(String groupName) {
        return m_groupDao.getGroup(groupName);
    }

    @Override
    public List<Group> getGroups() {
        Collection<Group> groups = m_groupDao.getGroups().values();
        return groups == null ? new ArrayList<Group>() : new ArrayList<Group>(groups);
    }

    @Override
    public List<OnmsCategory> getAuthorizedCategories(String groupName) {
        return new ArrayList<OnmsCategory>(m_categoryDao.getCategoriesWithAuthorizedGroup(groupName));
    }

    @Override
    public List<String> getAuthorizedCategoriesAsString(String groupName) {
        List<OnmsCategory> categories = getAuthorizedCategories(groupName);
        List<String> categoryNames = new ArrayList<String>(categories.size());
        for(OnmsCategory category : categories) {
            categoryNames.add(category.getName());
        }
        return categoryNames;
    }

    @Override
    public void saveGroup(OnmsGroup group) {
        saveGroup(onmsGroupMapper.map(group));
    }

    @Override
    public void saveGroup(Group group) {
        m_groupDao.saveGroup(group.getName(), group);
    }

    @Override
    public void saveGroup(Group group, List<String> authorizedCategories) {
        setAuthorizedCategories(group.getName(), authorizedCategories);
        m_groupDao.saveGroup(group.getName(), group);
    }

    @Override
    public void deleteGroup(String groupName) {
        m_groupDao.deleteGroup(groupName);
        setAuthorizedCategories(groupName, Collections.<String>emptyList());
    }

    @Override
    public void renameGroup(String oldName, String newName) {
        m_groupDao.renameGroup(oldName, newName);
        List<String> categories = getAuthorizedCategoriesAsString(oldName);
        setAuthorizedCategories(oldName, Collections.<String>emptyList());
        setAuthorizedCategories(newName, categories);
    }

    @Override
    public boolean addCategory(String groupName, String categoryName) {
        if (m_categoryDao.findByName(categoryName) == null) return false; // category does not exist
        List<String> categoryNames = getAuthorizedCategoriesAsString(groupName);
        if (!categoryNames.contains(categoryName)) {
            categoryNames.add(categoryName);
            saveGroup(getGroup(groupName), categoryNames);
            return true; // added successfully
        }
        return false; // can't be added, already added
    }   

    @Override
    public boolean removeCategory(String groupName, String categoryName) {
        List<String> categoryNames = getAuthorizedCategoriesAsString(groupName);
        if (categoryNames.contains(categoryName)) {
            categoryNames.remove(categoryName);
            saveGroup(getGroup(groupName), categoryNames);
            return true; // removed categoryName from categories successfully
        }
        return false; // categoryName does not exist, removing not possible
    }

    @Override
    public OnmsGroup getOnmsGroup(String groupName) {
        return onmsGroupMapper.map(getGroup(groupName));
    }

    @Override
    public OnmsGroupList getOnmsGroupList() {
        return onmsGroupListMapper.map(
                onmsGroupMapper.map(getGroups()));
    }

    @Override
    public OnmsUserList getUsersOfGroup(String groupName) {
        Group group = getGroup(groupName);
        OnmsUserList userCollection = new OnmsUserList();
        if (group != null) {
            for (String eachUser : group.getUserCollection()) {
                OnmsUser onmsUser;
                try {
                    onmsUser = m_userDao.getOnmsUser(eachUser);
                    if (onmsUser == null) continue;
                    userCollection.add(onmsUser);
                } catch (MarshalException e) {
                    Log.error("could not load user", e); //ignore
                } catch (ValidationException e) {
                    Log.error("could not load user", e); //ignore
                } catch (IOException e) {
                    Log.error("could not load user", e); //ignore
                }
            }
        }
        return userCollection;
    }

    @Override
    public OnmsUser getUserForGroup(String groupName, String userName) {
        Group group = getGroup(groupName);
        if (group != null && group.getUserCollection().contains(userName)) {
            try {
                return m_userDao.getOnmsUser(userName);
            } catch (MarshalException e) {
                Log.error("could not load user", e); //ignore
            } catch (ValidationException e) {
                Log.error("could not load user", e); //ignore
            } catch (IOException e) {
                Log.error("could not load user", e); //ignore
            }
        }
        return null; // group or user name does not exist
    }

    @Override
    public boolean addUser(String groupName, String userName) {
        Group group = getGroup(groupName);
        if (group != null && hasUser(userName)) {
            if (getUserForGroup(groupName, userName) != null) {
                return false; // user is already added
            }
            // user is not added to group, add
            group.addUser(userName);
            saveGroup(group);
            return true;
        }
        return false; // group or user does not exist.
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private boolean hasUser(final String userName) {
        if (userName != null) {
            try {
                return m_userDao.hasUser(userName);
            } catch (MarshalException e) {
                Log.error("could not load user", e); //ignore
            } catch (ValidationException e) {
                Log.error("could not load user", e); //ignore
            } catch (IOException e) {
                Log.error("could not load user", e); //ignore
            }
        }
        return false;
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
