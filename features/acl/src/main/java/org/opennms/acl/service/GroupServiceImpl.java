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
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.service;

import java.util.List;

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.model.Pager;
import org.opennms.acl.repository.GroupRepository;
import org.opennms.core.utils.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>GroupServiceImpl class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Service("groupService")
public class GroupServiceImpl implements GroupService, InitializingBean {

    /** {@inheritDoc} */
    @Override
    public List<GroupDTO> getUserGroupsWithAutorities(String username) {
        return repository.getUserGroupsWithAutorities(username);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean deleteUserGroups(String username) {
        return repository.deleteUserGroups(username);
    }

    /** {@inheritDoc} */
    @Override
    public List<GroupDTO> getFreeGroups(String username) {
        return repository.getFreeGroups(username);
    }

    /** {@inheritDoc} */
    @Override
    public GroupDTO getGroup(Integer id) {
        return repository.getGroup(id);
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<GroupDTO> getGroups() {
        return repository.getGroups();
    }

    /** {@inheritDoc} */
    @Override
    public List<GroupDTO> getGroups(Pager pager) {
        return repository.getGroups(pager);
    }

    /** {@inheritDoc} */
    @Override
    public List<GroupDTO> getUserGroups(String username) {
        return repository.getUserGroups(username);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean removeGroup(Integer id) {
        return repository.removeGroup(id);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean save(GroupDTO group) {
        return repository.save(group);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean saveGroups(String username, List<Integer> groups) {
        return repository.saveGroups(username, groups);
    }

    /**
     * <p>getTotalItemsNumber</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getTotalItemsNumber() {
        return repository.getGroupsNumber();
    }

    /** {@inheritDoc} */
    @Override
    public Boolean hasUsers(Integer id) {
        return repository.hasUsers(id);
    }

    @Autowired
    private GroupRepository repository;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
}
