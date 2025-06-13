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
        group.setDutySchedules(webGroup.getDutySchedules());
        group.setUsers(webGroup.getUsers());

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
