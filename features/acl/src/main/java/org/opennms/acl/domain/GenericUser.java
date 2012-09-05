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

import java.io.Serializable;
import java.util.List;

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.model.UserView;
import org.opennms.acl.service.GroupService;
import org.opennms.acl.service.UserService;
import org.springframework.util.Assert;

/**
 * This entity represent a user managed by Acl application.
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class GenericUser implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2527391420135516265L;
    /**
     * Constructor
     *
     * @param user a {@link org.opennms.acl.model.UserAuthoritiesDTO} object.
     * @param userService a {@link org.opennms.acl.service.UserService} object.
     * @param groupService a {@link org.opennms.acl.service.GroupService} object.
     */
    public GenericUser(UserAuthoritiesDTO user, UserService userService, GroupService groupService) {
        Assert.notNull(user);
        this.user = user;
        this.userService = userService;
        this.groupService = groupService;
        this.user.setGroups(groupService.getUserGroupsWithAutorities(this.user.getUsername()));
    }

    /**
     * Save the user
     */
    public void save() {
        userService.save(user);
    }

    /**
     * Add a list of groups to this GenericUser
     *
     * @param groups a {@link java.util.List} object.
     */
    public void setNewGroups(List<Integer> groups) {
        user.setItems(groups);
    }

    /**
     * Return a list of groups that this GenericUser don't have
     *
     * @return free groups
     */
    public List<GroupDTO> getFreeGroups() {
        return groupService.getFreeGroups(user.getUsername());
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<GroupDTO> getGroups() {
        return groupService.getUserGroups(user.getUsername());
    }

    /**
     * Return a read only GenericUser
     *
     * @return a {@link org.opennms.acl.model.UserView} object.
     */
    public UserView getUserView() {
        return user;
    }

    /**
     * Return the GenericUser unique identifier
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Return the username of this GenericUser
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Return a list of authorities of this GenericUser
     *
     * @return a {@link java.util.List} object.
     */
    public List<?> getAuthorities() {
        return user.getAuthorities();
    }

    private UserAuthoritiesDTO user;
    private GroupService groupService;
    private UserService userService;
}
