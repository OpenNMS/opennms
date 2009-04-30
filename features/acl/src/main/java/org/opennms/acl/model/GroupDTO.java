//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.model;

import java.util.List;

/**
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public class GroupDTO implements GroupView {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == 0;
    }

    public boolean hasAuthorities() {
        return authorities != null && authorities.size() > 0;
    }

    public boolean hasGroups() {
        return groups != null && groups.size() > 0;
    }

    @SuppressWarnings("unchecked")
    public void setAuthorities(List<?> items) {
        this.authorities = (List<AuthorityView>) items;
    }

    public List<?> getAuthorities() {
        return authorities;
    }

    public Boolean getEmptyUsers() {
        return emptyUsers;
    }

    public void setEmptyUsers(Boolean usersEmpty) {
        this.emptyUsers = usersEmpty;
    }

    public List<GroupView> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupView> groups) {
        this.groups = groups;
    }

    private String name;
    private Integer id = 0;
    private List<AuthorityView> authorities;
    private List<GroupView> groups;
    private Boolean emptyUsers = false;
}
