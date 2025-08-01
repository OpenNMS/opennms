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