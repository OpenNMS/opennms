/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.api;

import java.util.List;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.netmgt.model.OnmsUserList;

public interface GroupService {

    boolean existsGroup(String groupName);

    Group getGroup(String groupName);

    List<Group> getGroups();

    List<OnmsCategory> getAuthorizedCategories(String groupName);

    List<String> getAuthorizedCategoriesAsString(String groupName);

    void saveGroup(OnmsGroup group);

    void saveGroup(Group group);

    void saveGroup(Group group, List<String> authorizedCategories);

    void deleteGroup(String groupName);

    void renameGroup(String oldName, String newName);

    boolean addCategory(String groupName, String categoryName);

    boolean removeCategory(String groupName, String categoryName);

    OnmsGroup getOnmsGroup(String groupName);

    OnmsGroupList getOnmsGroupList();

    OnmsUserList getUsersOfGroup(String groupName);

    OnmsUser getUserForGroup(String groupName, String userName);

    boolean addUser(String groupName, String userName);

    void afterPropertiesSet() throws Exception;

}