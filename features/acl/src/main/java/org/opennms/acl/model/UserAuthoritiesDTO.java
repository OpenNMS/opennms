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

import java.util.ArrayList;
import java.util.List;

/**
 * Class to manage all user information including the list of authorities
 * 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public class UserAuthoritiesDTO extends UserDTO {

    public UserAuthoritiesDTO() {
        super();
        authorities = new ArrayList<AuthorityDTO>();
        groups = new ArrayList<GroupDTO>();
    }

    public List<GroupDTO> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDTO> groups) {
        this.groups = groups;
    }

    /**
     *@return List of {@link org.opennms.acl.model.AuthorityDTO} of a user
     */
    public List<AuthorityDTO> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<AuthorityDTO> authorities) {
        if (this.authorities != null) {
            this.authorities = authorities;
        }
    }

    public List<?> getItems() {
        return items;
    }

    public void setItems(List<?> items) {
        this.items = items;
    }

    private List<AuthorityDTO> authorities;
    private List<GroupDTO> groups;
    private List<?> items;
}
