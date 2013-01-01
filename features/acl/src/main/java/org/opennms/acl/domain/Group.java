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

package org.opennms.acl.domain;

import java.util.List;

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.model.GroupView;
import org.opennms.acl.model.Pager;
import org.opennms.acl.service.AuthorityService;
import org.opennms.acl.service.GroupService;

/**
 * This entity is a ACL group.
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class Group {

    /**
     * <p>Constructor for Group.</p>
     *
     * @param group a {@link org.opennms.acl.model.GroupDTO} object.
     * @param authorityService a {@link org.opennms.acl.service.AuthorityService} object.
     * @param groupService a {@link org.opennms.acl.service.GroupService} object.
     */
    public Group(GroupDTO group, AuthorityService authorityService, GroupService groupService) {
        super();
        this.group = group;
        this.authorityService = authorityService;
        this.groupService = groupService;
        this.group.setEmptyUsers(groupService.hasUsers(group.getId()));
    }

    /**
     * <p>getAuthorities</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<?> getAuthorities() {
        return authorityService.getGroupAuthorities(group.getId());
    }

    /**
     * Return a list of all items manageable by authorities
     *
     * @return all items
     */
    public List<?> getAllGroups() {
        return groupService.getGroups();
    }

    /**
     * Return a paginated list of anemic group
     *
     * @param pager a {@link org.opennms.acl.model.Pager} object.
     * @return a {@link java.util.List} object.
     */
    public List<GroupDTO> getGroups(Pager pager) {
        return groupService.getGroups(pager);
    }

    /**
     * Return a read only Group
     *
     * @return authority
     */
    public GroupView getGroupView() {
        return group;
    }

    /**
     * <p>hasAuthorities</p>
     *
     * @return hasAuthorities
     */
    public boolean hasAuthorities() {
        return group.getAuthorities().size() > 0;
    }

    /**
     * <p>hasUser</p>
     *
     * @return hasUsers
     */
    public boolean hasUser() {
        return group.getEmptyUsers();
    }

    /**
     * Save the internal state of the Group
     */
    public void save() {
        groupService.save(group);
    }

    /**
     * Overwrite the authorities assigned to this Group
     *
     * @param items a {@link java.util.List} object.
     */
    public void setNewAuthorities(List<?> items) {
        group.setAuthorities(items);
    }

    /**
     * Remove this Group
     *
     * @return a boolean.
     */
    public boolean remove() {
        return groupService.removeGroup(group.getId());
    }

    /**
     * Return the human readable description of this Group
     *
     * @return description
     */
    public String getName() {
        return group.getName();
    }

    /**
     * Group unique identifier
     *
     * @return group's identifier
     */
    public Integer getId() {
        return group.getId();
    }

    /**
     * <p>getFreeAuthorities</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<?> getFreeAuthorities() {
        return authorityService.getFreeAuthoritiesForGroup();
    }

    private GroupDTO group;
    private AuthorityService authorityService;
    private GroupService groupService;
}
