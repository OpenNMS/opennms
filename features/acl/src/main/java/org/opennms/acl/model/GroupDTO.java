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

package org.opennms.acl.model;

import java.util.List;

/**
 * <p>GroupDTO class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class GroupDTO implements GroupView {

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * <p>isNew</p>
     *
     * @return a boolean.
     */
    public boolean isNew() {
        return id == 0;
    }

    /**
     * <p>hasAuthorities</p>
     *
     * @return a boolean.
     */
    public boolean hasAuthorities() {
        return authorities != null && authorities.size() > 0;
    }

    /**
     * <p>hasGroups</p>
     *
     * @return a boolean.
     */
    public boolean hasGroups() {
        return groups != null && groups.size() > 0;
    }

    /**
     * <p>Setter for the field <code>authorities</code>.</p>
     *
     * @param items a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public void setAuthorities(List<?> items) {
        this.authorities = (List<AuthorityView>) items;
    }

    /**
     * <p>Getter for the field <code>authorities</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<?> getAuthorities() {
        return authorities;
    }

    /**
     * <p>Getter for the field <code>emptyUsers</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    @Override
    public Boolean getEmptyUsers() {
        return emptyUsers;
    }

    /**
     * <p>Setter for the field <code>emptyUsers</code>.</p>
     *
     * @param usersEmpty a {@link java.lang.Boolean} object.
     */
    public void setEmptyUsers(Boolean usersEmpty) {
        this.emptyUsers = usersEmpty;
    }

    /**
     * <p>Getter for the field <code>groups</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<GroupView> getGroups() {
        return groups;
    }

    /**
     * <p>Setter for the field <code>groups</code>.</p>
     *
     * @param groups a {@link java.util.List} object.
     */
    public void setGroups(List<GroupView> groups) {
        this.groups = groups;
    }

    private String name;
    private Integer id = 0;
    private List<AuthorityView> authorities;
    private List<GroupView> groups;
    private Boolean emptyUsers = false;
}
